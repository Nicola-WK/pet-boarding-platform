# Step 9 本地模拟支付框架

本项目用于作品集展示。本阶段没有接入微信支付，也不会产生真实扣款、预支付凭证或微信交易号。`PaymentService` 定义 `createPayment` 和 `notify`；当前实现是仅供本地演示的 `DemoPaymentService`。后续如接入真实支付，必须先在可信适配层验证平台回调，再调用 `notify`。

## 启用

演示接口默认关闭。仅在本地运行后端时设置：

```powershell
$env:PAYMENT_DEMO_ENABLED = 'true'
cd pet-server
mvn spring-boot:run
```

不设置该变量时，`/demo/payments/**` 路由不存在。演示接口需要用户 JWT，且只能操作自己的订单。

## 演示流程

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/demo/payments/orders/{orderId}` | 为自己的待付款订单创建模拟支付记录；重复调用返回同一待处理记录 |
| POST | `/demo/payments/{outTradeNo}/complete` | 模拟支付完成，将订单 0→1、支付记录 0→1 |

订单必须仍处于待付款状态，且付款截止时间未过。完成时核对支付记录金额与订单价格快照，并在一个数据库事务内更新订单和支付记录。重复完成同一笔支付返回原结果；取消、过期或金额不一致的订单不能完成支付。

示例（`$token` 为微信登录返回的 JWT，`$orderId` 为下单返回的 ID）：

```powershell
$headers = @{ Authorization = "Bearer $token" }
$payment = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/demo/payments/orders/$orderId" -Headers $headers
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/demo/payments/$($payment.data.outTradeNo)/complete" -Headers $headers
Invoke-RestMethod -Uri "http://localhost:8080/orders/$orderId" -Headers $headers
```

最后一个请求应显示订单状态 1。此后商家可调用 `POST /provider/orders/{id}/confirm` 演示确认流程。

## 测试与页面流程

后端执行 `mvn test` 和 `mvn package`；测试覆盖默认关闭、登录要求、归属校验、截止时间、金额核验和重复通知。小程序页面属于 Step 11：届时可提供明确标注为“模拟支付”的按钮，完成后刷新订单详情；关闭演示开关时隐藏该按钮。

简历描述建议：**“设计订单与支付记录状态流转，提供可关闭的本地模拟支付用于完整业务流程演示。”** 不应描述为“已接入微信支付”。
