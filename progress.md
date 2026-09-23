# 进度记录

## 2026-09-23
- 检查空工作区、本机开发工具及官方框架文档。
- 向用户询问本轮交付范围，在答复前先推进首次迭代所建议的项目初始化与数据库设计。
- 用户确认只交付 Step 1–2。
- 完成九张表的 MySQL 8 DDL 初稿；本机没有 Docker 和 MySQL 客户端。

- Step 1–2 完成：目录骨架、Maven 与 npm 元数据、九表 DDL、关系及业务约束文档。
- 验证：mvn validate 成功，npm pkg get 成功；数据库实际导入因本机无 MySQL 未执行。
- SQL 静态检查通过：九张表、BIGINT 主键、DATETIME 字段、外键引用建表顺序。

## 2026-09-23 继续
- 开始 Step 3–5；沙箱执行器仍有环境刷新错误，文件操作通过获批的提升权限命令完成。
- 首轮 6 项单元测试中 3 项失败：issuer 非 URL、独立测试未初始化 MyBatis-Plus Lambda 元数据；已针对性修正，待复测。
- HTTP 切片测试首次失败：主应用 @MapperScan 把 Mapper 注入切片上下文但没有 SqlSessionFactory；已将 Mapper 替换为测试 mock，待复测。
- Step 3–5 代码与 API 文档完成；HTTP 层、JWT、登录、宠物服务测试通过，JAR 打包成功。
- 测试期间修复 issuer 非 URL、MyBatis Lambda 单测元数据、HTTP 切片 Mapper 注入，以及未知路径/错误 ID 的统一响应。

## 2026-09-23 MySQL 实机联调
- 找到 MySQL 8.4.9 服务和微信开发者工具 CLI；真实 MySQL84 登录配置仍返回 1045。
- 在工作区建立隔离的 127.0.0.1:33307 测试实例；Flyway V1 实际执行成功并创建九张表。
- 实际宠物创建成功，但列表查询遇到 SQL 语法错误；根因是 character 列名未引用，被 MySQL 解析为保留字。已在实体字段映射加反引号，待复测。
- 修复 character 列映射后重建并重启：真实 HTTP 校验通过，未登录 401、用户资料 200、宠物新增/列表/更新/删除正常，第二用户读取和删除均为 404，数据库 is_deleted=1。
- 隔离 MySQL 测试实例、后端测试进程和临时数据均已停止/清理；真实 MySQL84 的 pet_boarding login-path 仍返回 1045，未做更改。

- 用户授权重置 MySQL84 root 密码；按官方 init-file 流程完成，临时文件已删除，服务恢复 Running。pet_boarding login-path 验证 root@localhost / 3306 / MySQL 8.4.9 成功。真实实例尚无 pet_boarding 数据库，待项目部署阶段创建并执行 Flyway。
