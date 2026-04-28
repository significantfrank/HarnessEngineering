# 挖掘领域概念
```
LegacyOrderService中有哪些核心的领域概念，以及有哪些隐士的领域模型可以显性化的
```

# 局部重构单元测试防护
```
为com.harness.crm.app.order.LegacyOrderService#calculateFinancialDiscount，
在src/test/java/com/harness/crm/domain中创建单元测试，测试各种折扣场景
```

# 重构：用领域服务封装业务逻辑
```
为了可扩展，重构com.harness.crm.app.order.LegacyOrderService#calculateFinancialDiscount，
将其逻辑封装在PricingPolicy领域服务，可以用扩展的方式支持新需求：高净值客户专属理财折扣
```