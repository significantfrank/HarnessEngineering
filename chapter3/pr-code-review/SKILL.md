---
name: pr-code-review
description: PR代码审查技能，从github获取PR变更并按语言最佳实践进行自动化审查
---

# PR代码审查技能

## 触发条件

用户提到"代码审查"、"review PR"、"代码检视"等关键词，或提供 PR URL。

## 执行流程

### 1. 解析 PR 信息

从用户输入提取 PR URL 或 ID，识别托管平台类型，确认目标/源分支。

### 2. 获取 PR 变更

```bash
python3 scripts/github_pr.py --owner {owner} --repo {repo} --pr {pr_number}
```

需配置环境变量 `GITHUB_TOKEN`（具有 `repo` 权限）。脚本获取变更文件列表、diff 及行数统计。

### 3. 语言识别与审查

根据文件扩展名识别语言，从 `references/` 加载对应审查指南：

| 文件 | 语言 |
|------|------|
| `references/python.md` | Python |
| `references/golang.md` | Go |
| `references/java.md` | Java |
| `references/c_cpp.md` | C/C++ |
| `references/shell.md` | Shell |

审查维度：正确性、线程安全、API 设计、性能、安全、风格。

严重等级：🔴 Critical（Bug/安全漏洞）→ 🟠 Major（逻辑/线程问题）→ 🟡 Minor（风格/命名）→ 🔵 Suggestion（改进建议）。

### 4. 提交审查结果

```bash
python3 scripts/github_pr.py \
  --owner {owner} --repo {repo} --pr {pr_number} \
  --submit \
  --comments '<JSON comments array>' \
  --body '<review summary markdown>'
```

comments 格式：
```json
[{"path": "src/Foo.java", "line": 42, "body": "🟠 **Major**: 问题描述..."}]
```

### 5. 汇报结果

向用户报告：评论数量、Review URL、关键发现摘要。

## 注意事项

1. 先读完整 diff 再评论，确保理解上下文
2. 建设性意见——给出修复方案，而非仅指出问题
3. 正确性和安全优先于风格
4. 不评论自动生成代码（除非有 Bug）
5. 尊重仓库现有风格，不标记与现有模式一致的风格问题
6. 关注破坏性变更，标记可能影响向后兼容的 API 变更
7. 确保行号准确，相关问题合并为一条评论
