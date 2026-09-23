# 发现与决策

## 现状
- 工作区 `D:\codex\pet` 为空，尚未初始化 Git。
- 本机 Maven 3.9.16 使用 JDK 17.0.19；默认 `java` 命令指向 Java 8，因此后续运行需设置 Java 17。
- Node 24.16.0、npm 11.13.0 可用。
- Docker 与 MySQL 命令均不可用，无法在本机实际执行 DDL。
- 用户提到的 PRD、TDD、既有 SQL 和 API 设计文件没有出现在工作区。

## 约束与设计
- 按所给步骤顺序，本轮首先建立仓库结构、基础说明与数据库迁移；业务代码在对应后续步骤实现。
- 所有表使用 BIGINT 主键、DATETIME 时间、InnoDB 与 utf8mb4；金额用 DECIMAL，支付回调需要唯一交易标识与幂等处理基础。
- 已核对官方文档：Spring Boot 3.5 分支支持 Java 17，MyBatis-Plus 官方提供 Spring Boot 3 专用 starter；uni-app 官方提供 Vue3/Vite TypeScript 模板。

## 范围确认
- 用户确认本轮只做项目初始化和数据库设计。

## Step 3–5 技术核对
- Spring Security 官方支持 Resource Server 验证自定义 JWT，并提供 NimbusJwtEncoder；将使用 HS256 和配置提供的密钥。
- MyBatis-Plus 官方提供 Spring Boot 3 starter 与 ASSIGN_ID；与现有 BIGINT 主键匹配。
- Flyway MySQL 需要单独的 flyway-mysql 模块。
- 微信 code2Session 使用后台请求，返回 openid；session_key 不下发客户端。

## Step 3–5 结果
- 后端使用 Spring Boot 3.5.16、MyBatis-Plus 3.5.17、Flyway MySQL 模块、Spring Security Resource Server、Redis 依赖。
- JWT issuer 必须是 HTTP(S) URL，HS256 密钥至少 32 字节；错误 HTTP 响应同样采用统一 JSON。
- 宠物读写必须含 user_id 条件，删除为软删除；PUT 明确支持清空可选字段。
- MySQL 和 Docker 仍未提供，因此无法验证 Flyway 实际建表或真实微信接口调用。
