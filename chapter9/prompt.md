# wiremock重构
```
重构
com.harness.crm.adapter.web.Customer360ControllerTest#getCustomer360_shouldReturn360View  
用wiremock代替
@MockitoBean
private CustomerCenterGatewayI customerCenterGateway;
使用两个资源文件放在chapter7/crm-backend/src/test/resources/fixture/下：
1. customer_center_success.json 包含详细的客户信息
2. customer_center_fail.json 模拟出错的情况
```
