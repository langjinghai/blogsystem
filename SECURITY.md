# Security Policy

## 报告问题

请不要在公开 Issue 中披露安全漏洞。请通过 GitHub 仓库的 **Security → Advisories → Report a vulnerability** 私下报告，并附上复现步骤和影响范围。

## 仓库约定

- 不要提交 `.env`、`application-local.yaml`、数据库 dump、OSS AccessKey、邮箱授权码、服务器私钥等敏感信息。
- 生产环境通过环境变量或服务器本地配置文件注入密钥。
- 公开演示环境不要导入真实用户数据；使用 `deploy/sql/seed.sql` 中的通用演示分类即可。
- 手机号验证码当前只打印在后端日志中，仅适合本地/受控演示环境；公网部署请接入真实短信服务。

## 支持范围

安全修复会优先合入 `main` 分支。历史版本不单独维护。
