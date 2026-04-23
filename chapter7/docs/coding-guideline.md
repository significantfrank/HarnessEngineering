# 编码规范务必遵守
1. 核心业务代码必须添加中文注释
2. 默认不要在DomainService写代码，让app直接操作gateway去完成CRUD，除非很有必要，征求我同意后可添加
3. 原子的业务逻辑，要沉淀封装在领域对象中。 例如`OrderService`上的方法`generateOrderNo()`和`isTerminal()`
   就应该被封装在`LeadEntity`领域对象内，而不是在app层。
4. 显式约束： “请确保每个函数的逻辑行数（LLOC）严格控制在 50 行以内。”
5. 重构指令： “如果逻辑过于复杂，请务必使用CMP（Composed Method Pattern）将其拆分为多个方法。”
6. 风格指南： “遵循单一职责原则，每个方法只做一件事。”