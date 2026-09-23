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
