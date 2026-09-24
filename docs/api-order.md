# Step 8 订单 API

订单日期按中国标准时间解释，`startDate` 为入店日，`endDate` 为离店日；订单区间为 `[startDate, endDate)`。每天单价由服务当前价格生成快照，订单创建后不会随服务价格修改而变化。所有接口使用统一响应格式。

## 用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/orders` | 创建待付款订单 |
| GET | `/orders?status=0&page=1&size=20` | 查看自己的订单，可按状态筛选 |
| GET | `/orders/{id}` | 查看自己的订单详情 |
| POST | `/orders/{id}/cancel` | 取消自己的待付款订单 |

下单请求：

```json
{
  "petId": 11,
  "serviceId": 21,
  "startDate": "2026-10-01",
  "endDate": "2026-10-04",
  "remark": "请按时喂药"
}
```

服务必须已上架且商家审核通过，宠物必须属于当前用户且未删除，宠物类型必须与服务类型一致。入住日不得早于中国标准时间的今天，寄养期为 1–90 天。每一天的有效预订数不得超过服务容量，同一宠物的日期也不得与其他有效订单重叠。待付款订单预留容量 15 分钟；过期后不再占用容量。Step 9 支付接入时必须拒绝过期订单，安全处理付款超时与支付回调。

## 商家接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/provider/orders?status=1&page=1&size=20` | 查看自己商家的订单 |
| GET | `/provider/orders/{id}` | 查看自己商家的订单详情 |
| POST | `/provider/orders/{id}/confirm` | 确认已付款订单，记录 `confirmedAt` |
| POST | `/provider/orders/{id}/start` | 入店日开始寄养，状态转为 2 |
| POST | `/provider/orders/{id}/complete` | 离店日结束后完成，状态转为 3 |

状态：0 待付款、1 待确认、2 寄养中、3 已完成、4 已取消。商家确认后仍为状态 1，以 `confirmedAt` 区分是否已确认。支付成功后由 Step 9 的可信支付回调将状态从 0 改为 1；本阶段没有客户端直接标记已付款的接口。已付款订单不能通过待付款取消接口取消，退款将在支付模块处理。跨用户或跨商家读取返回 404；非法状态流转返回 409。

## API 测试

1. 按 [商家 API](api-provider.md) 审核商家，并按 [服务 API](api-service.md) 创建上架服务；用户创建同类型宠物。
2. 用户调用 `POST /orders`，检查价格快照、天数、总额、状态 0 和 15 分钟付款截止时间。
3. 用户查自己的列表与详情，另一个用户查该订单应返回 404；未登录请求返回 401。
4. 对容量为 1 的服务创建重叠日期订单，第二次下单返回 409；取消第一单后可重新下单。不同日期的预订应分别计算容量；同一宠物跨服务的重叠订单也应被拒绝。
5. 用户取消待付款订单，状态变为 4；重复取消返回 409。支付成功后的商家确认、开始和完成流程须在 Step 9 接入后进行真实联调。

PowerShell 示例（`$token` 是用户 JWT，日期请按当前日期调整）：

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{ petId = 11; serviceId = 21; startDate = '2026-10-01'; endDate = '2026-10-04'; remark = '请按时喂药' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/orders' -Headers $headers -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri 'http://localhost:8080/orders?page=1&size=20' -Headers $headers
```

## 页面测试流程

小程序订单页面属于 Step 11。页面接入后检查：选宠物和日期下单、订单列表与详情、待付款取消、容量不足提示、商家待确认列表和确认/入店/完成按钮的状态限制。
