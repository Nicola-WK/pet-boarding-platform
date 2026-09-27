# 在另一台设备运行宠爱管家

## 另一台 Windows 电脑：微信开发者工具演示版

这套方式不需要小程序 AppID，使用微信开发者工具的模拟器。已在 Windows 11、Java 17.0.19、Maven 3.9.16、MySQL 8.4、Node.js 24.16 与 npm 11.13 上验证过构建；其他兼容版本也可尝试。

1. 安装 Git、Java 17、Maven 3.9+、MySQL 8、Node.js/npm 和微信开发者工具。确认 `git`、`java`、`mvn`、`mysql`、`node`、`npm` 可从 PowerShell 调用。如果 `mysql` 未加入 PATH，后面的命令请换成其 `mysql.exe` 完整路径。
2. 克隆代码：

   ```powershell
   git clone https://github.com/Nicola-WK/pet-boarding-platform.git
   cd pet-boarding-platform
   ```

3. 启动 MySQL，创建空库。命令会在本机提示输入 MySQL root 密码；不要把密码写进仓库：

   ```powershell
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pet_boarding CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
   ```

   不必手工导入 `V1__init.sql`，Spring Boot 首次启动时由 Flyway 建表。

4. 在仓库根目录设置 Java 17 路径并构建后端。将示例路径替换为新电脑上的实际 JDK 安装目录：

   ```powershell
   $env:JAVA_HOME = 'C:\path\to\jdk-17'
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   java -version
   mvn -f .\pet-server\pom.xml package
   powershell -ExecutionPolicy Bypass -File .\scripts\start-local-backend.ps1 -DemoMode
   ```

   在脚本窗口输入同一 MySQL root 密码，等待 `Started PetBoardingApplication`。**保持这个窗口打开**；关闭后端窗口，模拟器便无法请求 API。另开 PowerShell 访问 `http://127.0.0.1:8080/health` 应看到 `UP`。

5. 再开一个 PowerShell，在仓库根目录构建小程序：

   ```powershell
   cd .\pet-miniapp
   npm ci
   npm run build:mp-weixin:demo
   ```

6. 在微信开发者工具中导入 `pet-miniapp/dist/build/mp-weixin`，选择游客模式并点击“编译”。进入“我的”，点击“体验宠物主人”或“体验寄养商家”。第一次演示登录会在新电脑的数据库中生成示例商家、服务和宠物。

## 数据与设备边界

- GitHub 仓库存放代码与建表脚本；你原电脑 MySQL 中的个人修改、宠物、订单不会自动同步到新电脑。若要完全复制原数据，需要另行备份并导入 `pet_boarding` 数据库。
- 当前演示后端只监听 `127.0.0.1:8080`，因此开发者工具和后端应在**同一台电脑**运行。另一台设备上的 `127.0.0.1` 指向那台设备自身。
- 没有小程序 AppID 时，本项目可在微信开发者工具模拟器中演示，不能直接在手机微信里预览或发布。手机小程序需要正式 AppID，以及手机可访问的 HTTPS API 和微信平台要求的合法域名配置；本机演示脚本不提供这种部署。
- 演示支付只改变本地订单状态，不发生真实交易。
