# Step 11 小程序页面与接口验证

## 构建验证

在 `pet-miniapp` 执行 `npm ci`、`npm run type-check`、`npm run build:h5`、`npm run build:mp-weixin`。在 `pet-server` 执行 `mvn test`。后端将 bigint 业务 ID 作为 JSON 字符串发送，小程序按字符串处理 ID，避免浏览器整数精度损失。

## API 与页面流程

| 页面 | API 示例 | 页面操作与预期 |
| --- | --- | --- |
| 首页 | `GET /providers?page=1&size=6` | 展示审核通过的真实商家；无数据展示空状态 |
| 寄养列表 | `GET /services?petType=dog&maxPrice=100` | 选择“狗狗”和价格筛选，列表更新 |
| 商家详情 | `GET /providers/{id}`、`GET /providers/{id}/services`、`GET /providers/{id}/reviews` | 展示商家、服务与评价，点击服务进入预约 |
| 宠物档案 | `GET /pets`、`POST /pets`、`PUT /pets/{id}`、`DELETE /pets/{id}` | 登录后新增、编辑、删除宠物并返回列表核对 |
| 预约与订单 | `POST /orders`、`GET /orders/{id}` | 选择宠物与日期，下单后检查金额、天数和状态 |
| 演示支付 | `POST /demo/payments/orders/{orderId}`、`POST /demo/payments/{outTradeNo}/complete` | 双端显式开启演示开关后付款按钮出现，付款后状态变更 |
| 商家中心 | `POST /provider/apply`、`GET /provider/services`、`POST /provider/services/{id}/status` | 申请并审核商家后管理服务，上下架与公开列表一致 |
| 商家订单 | `GET /provider/orders`、`POST /provider/orders/{id}/confirm` 等 | 确认、开始、完成后状态逐步变化 |
| 评价 | `POST /orders/{id}/review`、`GET /providers/{id}/reviews` | 完成订单后评分，商家详情可见真实评价 |

后端统一返回 `{ "code": 200, "message": "success", "data": ... }`；401 时前端清理过期会话。图片表单目前接受 HTTPS 链接，未内置 COS 直传控件。

## 浏览器视觉检查

H5 本地开发环境在 390×844 视口验证了：首页 → “狗狗寄养” → 仅狗狗服务商家列表 → 商家详情 → 未登录点击预约跳转登录页。该检查使用浏览器请求拦截提供临时响应，仅验证渲染、跳转和数据绑定；浏览器截图位于系统临时目录，测试数据没有写入项目。页面未出现水平溢出或控制台异常。真实小程序登录、真机请求域名和微信授权仍需自己的 AppID 与微信开发者工具验证。
