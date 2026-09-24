# Step 7 寄养服务 API

所有接口返回 `{ "code": 200, "message": "success", "data": ... }`。价格单位为人民币元，表示每天的寄养单价；订单阶段将按天数计算总价。

## 商家管理

已审核通过的商家可以创建服务。商家资料重新提交审核期间，现有服务可编辑和下架，不能新建或重新上架；公开查询会隐藏该商家的全部服务。服务的 `providerId` 始终从 JWT 所属用户的商家档案取得，客户端无需提供。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/provider/services` | 创建并上架服务 |
| GET | `/provider/services?page=1&size=20` | 查看自己的全部服务，包括下架服务 |
| PUT | `/provider/services/{id}` | 修改自己的服务 |
| PATCH | `/provider/services/{id}/status` | `{"active":false}` 下架，`true` 上架 |

创建及修改请求：

```json
{
  "name": "小型犬家庭寄养",
  "price": 88.00,
  "petType": "dog",
  "capacity": 3,
  "description": "每天两次散步",
  "images": ["https://cos.example.com/services/dog-room.jpg"]
}
```

`petType` 为 `dog`、`cat` 或 `other`；价格大于 0、最多两位小数，容量至少 1。最多 10 张图片，均须为 HTTPS URL。图片上传授权接口尚未开发，本阶段接收已上传到 COS 的 URL。

## 公开浏览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/services?petType=dog&keyword=家庭&minPrice=50&maxPrice=150&page=1&size=20` | 按宠物类型、名称和价格筛选上架服务 |
| GET | `/services/{id}` | 查看上架服务详情 |
| GET | `/providers/{id}/services?page=1&size=20` | 查看已审核商家的上架服务 |

公开接口无需 JWT。仅当服务已上架且所属商家审核通过时才可见；否则详情返回 404。分页 `size` 范围为 1–50，价格范围下限不得大于上限。

## API 测试

1. 按 [商家 API](api-provider.md) 申请并审核商家，取得该商家账号的 JWT。
2. 用 JWT 创建服务；检查返回的 `providerId` 等于当前商家 ID。未审核商家创建返回 403，未登录创建返回 401。
3. 不带 JWT 查询 `/services` 和 `/providers/{id}/services`，应看到上架服务；按 `petType`、价格筛选并检查结果。
4. 商家下架后，自己的列表仍可见，公开列表和详情不可见；重新上架后恢复。
5. 用另一个商家账号修改该服务应返回 404。商家资料重新提交审核后，公开列表隐藏该商家的服务。

PowerShell 示例（`$token` 为微信登录返回的 JWT）：

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{ name = '小型犬家庭寄养'; price = 88; petType = 'dog'; capacity = 3; images = @() } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/provider/services' -Headers $headers -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri 'http://localhost:8080/services?petType=dog&minPrice=50&maxPrice=150'
```

## 页面测试流程

小程序页面属于 Step 11，当前没有可运行的服务管理或浏览页面。页面接入后检查：商家创建及下架服务、用户按类型和价格筛选、商家详情中的服务列表、服务详情、商家重新审核时服务隐藏。
