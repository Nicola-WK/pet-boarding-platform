# 宠爱管家（Pet Boarding Platform）

宠物寄养微信小程序 V1.0。已完成 Step 1–8、可关闭的 Step 9 本地模拟支付框架和 Step 10 评价系统。真实微信支付暂缓；小程序页面按 [开发计划](task_plan.md) 后续阶段实现。

## 目录

| 路径 | 用途 |
| --- | --- |
| `pet-server/` | Java 17、Spring Boot 3、MyBatis-Plus 后端 |
| `pet-miniapp/` | uni-app 小程序目录骨架，页面阶段再接入依赖 |
| `database/create_database.sql` | 创建 MySQL 8 数据库 |
| `pet-server/src/main/resources/db/migration/V1__init.sql` | Flyway 首版九表迁移 |
| `docs/database-design.md` | 表关系、字段及业务约束 |
| `docs/api-auth-pet.md` | 用户认证与宠物 API、测试流程和错误码 |
| `docs/api-provider.md` | 商家申请、公开列表、审核 API 和测试流程 |
| `docs/api-service.md` | 寄养服务管理、公开浏览 API 和测试流程 |
| `docs/api-order.md` | 下单、订单查询、商家履约和测试流程 |
| `docs/api-payment-demo.md` | 本地模拟支付开关、接口与测试流程 |
| `docs/api-review.md` | 订单评价、公开列表与测试流程 |

## 运行后端

需要 Java 17、Maven 3.9+、MySQL 8。先在项目根目录使用 MySQL 客户端创建空库：

```sql
SOURCE database/create_database.sql;
```

由 Flyway 在首次启动时执行建表脚本。此前手动导入过 `V1__init.sql` 的临时库应重建为空库，再由 Flyway 接管。

参考 [环境变量模板](.env.example) 在运行进程中设置变量。长期运行时应使用仅有 `pet_boarding` 库权限的应用账号；不要提交真实数据库密码、微信 AppSecret 或 JWT 密钥。JWT 密钥必须由安全随机数生成，Base64 解码后至少 32 字节；JWT_ISSUER 必须是当前服务的 URL。PowerShell 可生成密钥：

```powershell
$env:JWT_SECRET_BASE64 = python -c "import base64,secrets; print(base64.b64encode(secrets.token_bytes(32)).decode())"
```

其余必需变量：`DB_USERNAME`、`DB_PASSWORD`、`JWT_ISSUER`、`WECHAT_APP_ID`、`WECHAT_APP_SECRET`。商家审核可选配置 `ADMIN_OPENIDS`（审核员微信 openid，逗号分隔）；为空时无人可审核。按实际环境设置 `DB_URL`，必要时设置 Redis 连接变量。然后运行：

```powershell
cd pet-server
mvn spring-boot:run
```

`GET /health` 返回统一格式的健康响应。首次启动需要 MySQL 可连接，Flyway 会创建九张业务表。本阶段尚未使用 Redis 缓存，但已接入 Redis 依赖并预留连接配置。

## API 验证

微信小程序调用 `wx.login()` 获取一次性 code，发送给 `POST /auth/wechat/login`。服务端换取 openid，创建或复用用户并返回 Bearer JWT。使用该 JWT 调用 `GET /users/profile`、宠物接口和商家申请接口。请求与响应示例见 [认证与宠物 API](docs/api-auth-pet.md) 、[商家 API](docs/api-provider.md) 、[寄养服务 API](docs/api-service.md) 、[订单 API](docs/api-order.md) 、[模拟支付 API](docs/api-payment-demo.md) 和 [评价 API](docs/api-review.md)。

```powershell
cd pet-server
mvn test
mvn package
```

本地演示支付可设置 `PAYMENT_DEMO_ENABLED=true`，默认关闭；详见 [模拟支付 API](docs/api-payment-demo.md)。页面测试将在小程序页面开发阶段进行。当前没有小程序运行脚本或页面；真实微信登录联调仍需小程序 AppID 和 AppSecret。

## 开发顺序

项目初始化 → 数据库设计 → 后端基础框架 → 用户认证 → 宠物档案 → 商家 → 寄养服务 → 订单 → 支付 → 评价 → 小程序页面 → 测试。
