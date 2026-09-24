# Step 10 评价 API

用户只能评价自己已完成的寄养订单，每个订单最多一条评价。公开评价不返回用户 ID、昵称或联系方式。所有接口沿用统一响应格式。

| 方法 | 路径 | 身份 | 说明 |
| --- | --- | --- | --- |
| POST | `/orders/{orderId}/review` | 订单用户 | 为已完成订单创建评价 |
| GET | `/orders/{orderId}/review` | 订单用户 | 查看自己对该订单的评价 |
| GET | `/providers/{id}/reviews?page=1&size=20` | 公开 | 查看已审核商家的评价 |
| GET | `/services/{id}/reviews?page=1&size=20` | 公开 | 查看上架服务的评价 |

创建请求：

```json
{
  "score": 5,
  "content": "照顾得很细心",
  "images": ["https://cos.example.com/reviews/pet.jpg"]
}
```

`score` 为 1–5 分，文字最多 2000 字，图片最多 10 张且必须是 HTTPS URL。当前接收已上传到 COS 的图片地址；上传授权接口尚未实现。商家及服务 ID 从订单读取，客户端不能指定。重复评价返回 409；非本人订单或不存在的评价返回 404；订单未完成返回 409。

## API 测试

1. 准备一笔当前用户的状态 3（已完成）订单。完整业务路径为下单→本地模拟支付→商家确认→入店→离店后完成；时间限制也由服务端检查。
2. 使用用户 JWT 调用 `POST /orders/{orderId}/review`，确认评分、内容和图片返回；重复提交应返回 409。
3. 未登录提交应返回 401；另一用户提交同一订单应返回 404；未完成订单提交应返回 409。
4. 不带 JWT 查询商家和服务评价列表；未审核商家或下架服务的对应公开接口应返回 404。

PowerShell 示例（`$token` 是订单用户的 JWT，`$orderId` 是已完成订单 ID）：

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{ score = 5; content = '照顾得很细心'; images = @() } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/orders/$orderId/review" -Headers $headers -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri 'http://localhost:8080/providers/31/reviews?page=1&size=20'
```

## 页面测试流程

小程序页面属于 Step 11。页面接入后检查：已完成订单显示评价入口，提交后显示已评价状态，商家详情与服务详情显示公开评价；待付款、待确认、寄养中或已取消订单没有评价入口。
