# wiremock重构
```
重构
com.harness.crm.adapter.web.Customer360ControllerTest#getCustomer360_shouldReturn360View  
用wiremock代替
@MockitoBean
private CustomerCenterGatewayI customerCenterGateway;
使用两个资源文件放在chapter8/crm-backend/src/test/resources/fixture/下：
1. customer_center_success.json 包含详细的客户信息
2. customer_center_fail.json 模拟出错的情况
```

# 裸机脚本端到端测试命令

## windows命令`
```
---
description: "运行从页面开始的端到端测试"
---

0. **存活与冲突校验**：
   禁止盲目启动。首先利用 `netstat -ano | findstr "LISTENING"` 精准探测CRM后端(8080)、Customer-center后端(8081)与前端(5173)端口。

1. 链接本地MySQL数据库(默认用户名密码都是root），如果不对，让用户提供正确的用户名密码。检查是否存在customer_center和crm两个库，如果不存创建它们

2. **后端防御性启动**：
   若 8080 端口在线，kill原进程。重新拉起：
   `powershell -Command "Start-Process cmd -ArgumentList '/k cd chapter8/crm-backend && mvn spring-boot:run -Dserver.port=8080'"`
   若 8081 端口在线，kill原进程。重新拉起：
   `powershell -Command "Start-Process cmd -ArgumentList '/k cd chapter7/customer-center && mvn spring-boot:run -Dserver.port=8081'"`

3. **前端动态挂载**：
   若 5173 端口离线，自动进入前端开发目录执行热更新模式：
   `powershell -Command "Start-Process cmd -ArgumentList '/k chapter8/cd crm-frontend && npm run dev'"`

4. **浏览器自动化执行**：
   在确认双端“心跳”正常后（`powershell -Command "Start-Sleep -Seconds 5"`），方可驱动 Chrome DevTools 协议，开启无头浏览器进行全链路回归。
```

## MacOS命令
```
---
description: "运行从页面开始的端到端测试"
---

0. **存活与冲突校验**：
   禁止盲目启动。首先利用 `lsof -i :8080 -i :8081 -i :5173` 精准探测CRM后端(8080)、Customer-center后端(8081)与前端(5173)端口。

1. 链接本地MySQL数据库(默认用户名密码都是root），如果不对，让用户提供正确的用户名密码。检查是否存在customer_center和crm两个库，如果不存创建它们

2. **后端防御性启动**：
   若 8080 端口在线，kill原进程（`kill -9 $(lsof -ti:8080)`）。重新拉起：
   `osascript -e 'tell application "Terminal" to do script "cd chapter8/crm-backend && mvn spring-boot:run -Dserver.port=8080"'`
   
   若 8081 端口在线，kill原进程（`kill -9 $(lsof -ti:8081)`）。重新拉起：
   `osascript -e 'tell application "Terminal" to do script "cd chapter7/customer-center && mvn spring-boot:run -Dserver.port=8081"'`

3. **前端动态挂载**：
   若 5173 端口离线，自动进入前端开发目录执行热更新模式：
   `osascript -e 'tell application "Terminal" to do script "cd chapter8/crm-frontend && npm run dev"'`

4. **浏览器自动化执行**：
   在确认双端“心跳”正常后（`sleep 5`），方可驱动 Chrome DevTools 协议，开启无头浏览器进行全链路回归。
```

# 容器化沙箱端到端测试命令
```
---
description: "运行从页面开始端到端测试"
---

1. 先进行环境清理，运行命令：docker-compose down -v
2. 在执行容器化的端到端测试：docker-compose --profile test up
```