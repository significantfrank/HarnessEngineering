## Context

当前客户域只有枚举维度筛选（status/source/level），无法按业务特征灵活分类。客户实体通过 JPA + `JpaSpecificationExecutor` 实现动态查询，无 JPA 关联关系（FK 均为 Long 字段）。前端使用 Ant Design Vue + Pinia。

现有筛选模式：`CustomerGatewayImpl` 用 Specification 动态拼 Predicate，每个筛选条件为独立的 `AND` 分支。

## Goals / Non-Goals

**Goals:**
- 支持为客户打多个标签，标签包含名称和颜色
- 支持预定义标签 + 用户自由创建（混合模式）
- 客户列表支持多标签 AND 组合筛选
- 标签管理独立页面，位于客户管理子菜单下
- 标签删除保护：使用中不可删除

**Non-Goals:**
- 不做全局标签体系（标签仅属于客户域，未来扩展时再抽象）
- 不做标签分组/分类
- 不做标签统计报表
- 不做标签批量操作（如"给所有VIP客户打某标签"）

## Decisions

### D1: M:N 关联表方案，非 JSON 字段

**选择**: 独立 `tag` 表 + `customer_tag` 关联表

**备选**: CustomerEntity 增加 `tags` JSON/VARCHAR 字段

**理由**: 关联表支持高效的 AND 筛选（EXISTS 子查询），与现有 `JpaSpecificationExecutor` 模式天然契合。JSON 字段需数据库特定函数，且违背关系模型规范。标签需要独立管理（名称修改、颜色变更），独立表更合适。

### D2: 无 JPA @ManyToMany，遵循项目约定

**选择**: `CustomerTagRelEntity` 作为独立实体，FK 为 Long 字段，手动管理关联

**备选**: CustomerEntity 上 `@ManyToMany @JoinTable` 让 JPA 自动管理

**理由**: 项目明确约定无 JPA 关联关系（CustomerNote 也是如此），避免懒加载问题和实体间耦合。全删再建的策略简单可靠。

### D3: AND 筛选用 EXISTS 子查询

**选择**: 每个 tagId 生成一个 `EXISTS (SELECT 1 FROM customer_tag WHERE customer_id=c.id AND tag_id=?)` Predicate

**备选**: `IN (SELECT customer_id FROM customer_tag WHERE tag_id=? GROUP BY customer_id HAVING COUNT(DISTINCT tag_id)=N)`

**理由**: EXISTS 语义清晰，每个 Predicate 独立，和现有 Specification 模式一致。HAVING 方案需额外计算 count，且与现有动态拼 Predicate 模式不兼容。

### D4: 更新客户标签采用"全删再建"策略

**选择**: 更新客户时先 `deleteByCustomerId`，再批量插入新关联

**备选**: Diff 对比，只增删变化的关联

**理由**: 标签数量通常 < 10，全删再建简单可靠，无需 diff 逻辑。在 `@Transactional` 内执行，保证一致性。

### D5: 删除标签时校验使用数量，拒绝删除

**选择**: `countByTagId > 0` 时抛异常，提示使用数量

**备选**: 级联删除关联 / 软删除

**理由**: 级联删除会导致客户丢失标签，不可接受。软删除增加复杂度。拒绝删除最安全，且前端可展示使用数量辅助决策。

### D6: 前端自由创建标签的交互流程

**选择**: TagSelect 组件内输入不匹配时显示"创建 'xxx'"，点击后弹出颜色选择弹窗，POST 创建标签后自动选上

**备选**: 跳转到标签管理页创建

**理由**: 内联创建体验流畅，颜色选择弹窗保证每个标签都有颜色。跳转打断操作流。

## Risks / Trade-offs

- **[全删再建可能在并发场景丢失标签]** → 客户更新本身已在 `@Transactional` 内，且标签操作频率低，风险可接受
- **[标签名无去重校验可能产生同义词]** → Tag 表 name 字段 UNIQUE 约束，同名标签无法重复创建；但"大客户"/"重要客户"等同义词问题需靠团队约定
- **[EXISTS 子查询在标签数量极多时性能下降]** → 实际 AND 筛选通常 2-3 个标签，且 customer_tag 表有索引，性能无忧
