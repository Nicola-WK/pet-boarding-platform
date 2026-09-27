# 宠爱管家小程序

uni-app（Vue 3 + TypeScript）前端，使用 Pinia 管理登录状态，Axios 通过 `uni.request` 适配器调用 Spring Boot API。页面和接口使用真实业务数据；没有商家时展示空状态。

## 本地运行

```powershell
cd pet-miniapp
npm ci
npm run type-check
npm run dev:h5
```

H5 开发地址默认为 `http://localhost:5173`。不创建 `.env` 时，API 基址为 `/api`，Vite 会代理到 `http://127.0.0.1:8080`。也可复制 `.env.example` 为 `.env.local` 后修改 `VITE_API_BASE_URL`。H5 只供页面与公开浏览接口调试；微信登录要在微信开发者工具中验证。

微信小程序构建：

```powershell
npm run build:mp-weixin
```

在微信开发者工具中导入 `pet-miniapp/dist/build/mp-weixin`。将 `src/manifest.json` 的 `mp-weixin.appid` 改为自己的小程序 AppID，并在后端设置匹配的 `WECHAT_APP_ID` 和 `WECHAT_APP_SECRET`。微信小程序模拟器默认请求 `http://127.0.0.1:8080`，需先启动后端。若后端不在本机，在 `.env.local` 中设置 `VITE_API_BASE_URL` 为开发者工具可访问的完整地址；真机需改为 HTTPS 和微信公众平台配置的合法 request 域名。修改环境变量后重新构建。开发者工具本地调试 HTTP 地址时，在「详情 → 本地设置」开启不校验合法域名。

订单详情的“模拟付款”仅在前端 `VITE_DEMO_PAYMENT_ENABLED=true` 且后端 `PAYMENT_DEMO_ENABLED=true` 时可用。默认关闭，**不接入真实微信支付，也不发生资金交易**。

## 没有 AppID 时的本机演示

此模式供简历展示，使用本机 MySQL，登录与支付均为演示操作。后端默认关闭演示接口；使用启动脚本的 `-DemoMode` 后只监听 `127.0.0.1`。

```powershell
cd D:\codex\pet\pet-server
mvn package
cd D:\codex\pet
$env:JAVA_HOME = 'D:\Java\jdk-17.0.19+10'
powershell -ExecutionPolicy Bypass -File scripts/start-local-backend.ps1 -DemoMode
```

在脚本窗口输入本机 MySQL 密码并保持窗口打开。另开 PowerShell 构建小程序：

```powershell
cd D:\codex\pet\pet-miniapp
npm ci
npm run build:mp-weixin:demo
```

微信开发者工具导入 `D:\codex\pet\pet-miniapp\dist\build\mp-weixin`，使用游客模式即可。进入“我的”，点“体验宠物主人”新增宠物，再回首页浏览“演示寄养屋”、创建订单并在订单详情点“模拟付款”；回“我的”点“体验寄养商家”，进商家中心处理订单。两个按钮可以随时切换演示角色。订单开始寄养需到入住日期，完成服务需到离店日期；这是订单业务时间规则。首次演示登录会准备 4 家示例商家、覆盖狗狗、猫咪和小宠的服务、3 只宠物及有历史订单关联的演示评价；重复登录不会重复创建已有数据。

游客模式下开发者工具可能输出自身的 `webapi_getwxaasyncsecinfo` 或 `timeout` 信息；以页面和本机 API 是否可用为准。真实微信登录、真机预览和上线仍需要正式 AppID、AppSecret 和合法域名。

## 页面检查

1. 首页搜索或选择宠物类型 → 寄养列表显示筛选结果；没有商家时显示空状态。
2. 打开商家 → 查看介绍、可预约服务和真实评价；服务卡片进入预约页。
3. 微信登录 → 新增宠物 → 选择入住和离店日期 → 创建订单 → 在“我的订单”查看状态。
4. 开启双方模拟支付开关后，可完成“模拟付款 → 商家确认 → 开始寄养 → 完成服务 → 评价”的演示流程。
5. 商家中心申请商家，审核通过后新建、编辑、上下架服务并处理订单。审核接口与操作说明见根目录 `docs/api-provider.md`。

`npm run build:h5`、`npm run build:mp-weixin` 和 `npm run type-check` 用于提交前校验。需要真实微信 AppID/AppSecret 才能验证登录与授权页面；仅有 H5 构建成功不能代表微信登录已联通。

## 本机后端快捷启动

确认 MySQL84 正在运行并已构建 `pet-server/target/pet-server-1.0.0-SNAPSHOT.jar` 后，在项目根目录执行 `powershell -ExecutionPolicy Bypass -File scripts/start-local-backend.ps1`。脚本仅在本机窗口提示输入 MySQL 密码，不保存密码。若在 `src/manifest.json` 的 `mp-weixin.appid` 填入自己的 AppID，脚本还会安全提示输入 AppSecret；不在仓库存储 AppSecret。未设置时仅供公开页面预览。后端启动后访问 `http://127.0.0.1:8080/health` 检查。
