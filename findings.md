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
