# 宠爱管家（Pet Boarding Platform）

宠物寄养微信小程序 V1.0。当前仓库完成了 **Step 1 项目初始化** 和 **Step 2 数据库设计**。业务 API、微信支付和小程序页面将在后续步骤实现。

## 目录

| 路径 | 用途 |
| --- | --- |
| `pet-server/` | Java 17、Spring Boot 3 后端工程骨架 |
| `pet-miniapp/` | uni-app、Vue 3、TypeScript 小程序工程骨架 |
| `database/create_database.sql` | 创建 MySQL 8 数据库 |
| `pet-server/src/main/resources/db/migration/V1__init.sql` | 九张业务表的首版迁移 |
| `docs/database-design.md` | 表关系、字段及业务约束 |

## 数据库初始化

准备 MySQL 8。在项目根目录打开 MySQL 客户端（`mysql -u root -p`），使用有建库权限的账号执行：

```sql
SOURCE database/create_database.sql;
USE pet_boarding;
SOURCE pet-server/src/main/resources/db/migration/V1__init.sql;
SHOW TABLES;
SHOW CREATE TABLE boarding_order;
```

迁移脚本是一次性脚本。本阶段可在临时开发数据库手动验证；后续后端基础框架阶段接入 Flyway 时，应在全新数据库由 Flyway 执行，避免重复建表。数据库账号、微信密钥、COS 密钥和 JWT 密钥应通过环境变量提供，不提交到仓库。

## 当前阶段的校验

```powershell
cd pet-server
mvn validate
cd ../pet-miniapp
npm pkg get name version private
```

目前没有可运行的后端服务或小程序页面，因此 `mvn spring-boot:run` 和小程序开发命令要等对应开发步骤完成后使用。

## 开发顺序

项目初始化 → 数据库设计 → 后端基础框架 → 用户认证 → 宠物档案 → 商家 → 寄养服务 → 订单 → 支付 → 评价 → 小程序页面 → 测试。

## 设计边界

数据库保存商家、宠物、服务、订单、支付和评价数据。照片字段只保存腾讯云 COS URL；文件上传与访问控制在后续业务步骤实现。订单金额使用 `DECIMAL(10,2)`，订单表保存下单时的服务单价快照，避免后续改价影响历史订单。
