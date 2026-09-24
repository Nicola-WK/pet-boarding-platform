# 发现与决策

## 初始状态
- 工作区 `D:\codex\pet` 为空，尚未初始化 Git。
- 本机 Maven 3.9.16 使用 JDK 17.0.19；默认 `java` 命令指向 Java 8，因此后续运行需设置 Java 17。
- Node 24.16.0、npm 11.13.0 可用。
- 初始 PATH 中未找到 Docker 或 mysql 命令；后续定位到 MySQL Server 8.4.9 的安装目录。
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
- 已在隔离 MySQL 8.4.9 实例验证 Flyway V1 建表；真实微信 code 兑换仍需小程序凭据。

## 本机环境与实测
- MySQL84 服务正在运行，客户端位于 C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe；用户创建的 pet_boarding 登录配置仍返回 1045，未更改真实服务或数据。
- 微信开发者工具及 cli.bat 位于 D:\xunleidownload\wechat\微信web开发者工具；当前小程序只有目录骨架，暂无可导入页面。
- 隔离 MySQL 8.4.9 实例上 Flyway V1 迁移成功，九张业务表与 flyway_schema_history 均存在；后端真实启动、JWT 身份、宠物 CRUD 和跨用户隔离均通过。
- MySQL 8.4 把 character 视为需引用的标识符；Pet 实体映射已加反引号。隔离实例与临时数据验证后已停止并清理。

- 真实 MySQL84 root 密码已按官方 init-file 流程重置；pet_boarding login-path 可认证。现有服务无 pet_boarding 数据库，未对业务库执行迁移。

- 真实 MySQL84 的 pet_boarding 已由 Flyway V1 迁移，版本 1 success=1，九张业务表齐全；当前无真实用户数据。
- 自动审批拒绝新增持久应用账号及凭据存储，因此本轮只通过本机临时进程使用现有 root 凭据完成迁移，未改动 MySQL 账号/授权。
- 商家模块 27 项单元及 Web 切片测试通过，后端 JAR 可打包；实际商家 HTTP 联调尚未执行。
