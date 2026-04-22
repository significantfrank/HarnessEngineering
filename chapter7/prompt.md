# 1 搭建框架
```
/opsx-explore 搭建一个前后端分离的CRM项目框架，前端放在crm-frontend用Vue+antdesign。
后端放在crm-backend用java，jdk21，Springboot3，maven，mysql，并采用COLA应用架构。
本次迭代以搭建框架为主，只实现customer的简单CRUD
```

# 2 补充测试防护
```
补充后端继承测试，使用SpringBootTest从controller开始测试，测试用h2内存数据库，每个API一个测试用例，保证测试都通过。
```

# 3 业务功能迭代一：完善CRM业务功能
```
/opsx-explore 完善CRM业务功能，增加线索（Lead），机会（Opportunity），订单（Order）管理
```

## 3.1 重构DomainService
```
不要在DomainService写代码，让app直接操作gateway去完成CRUD，除非很有必要，征求我同意后可添加
```
## 3.2 原子业务逻辑封装重构
```
原子的业务逻辑，要沉淀封装在领域对象中。 例如`OrderService`上的方法`generateOrderNo()`和`isTerminal()`
就应该被封装在`LeadEntity`领域对象内，而不是在app层。
```
## 3.3 单元测试不符合标准重构
```
1. 后端单元测试参考`CustomerControllerIntegrationTest`, 不要在App层和DomainService上做测试
2. 每一个业务功能最多写2个TestCase，一个Happy Case，一个Bad Case
```

# 4 业务功能迭代二：增加客户小记功能
```
/opsx-explore 增加客户小记功能
```

