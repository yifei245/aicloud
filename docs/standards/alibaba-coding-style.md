# AICloud 阿里 Java 编码规约落地说明

项目 Java 代码默认遵循《阿里巴巴 Java 开发手册》与 P3C 静态检查规则。

## 必须遵守

- Controller 只做参数接收、权限入口和响应封装，不写 SQL、不写业务编排细节。
- Service 承载业务逻辑；数据库访问通过 Mapper/MyBatis Plus 完成。
- 新业务对象必须具备清晰分层：`Controller -> Service -> Mapper -> Entity/DO`。
- 禁止在 Controller/Service 中拼接未受控 SQL；确需自定义 SQL 时写入 Mapper XML 或 Mapper 注解，并确保参数绑定。
- 类名、方法名、常量名、包名遵循 P3C 命名规则。
- 异常不能吞掉；禁止 `catch (Exception e) {}` 这类空处理。
- 集合、并发、日期时间、BigDecimal 使用遵循 P3C 规则。
- 新增接口必须补 Swagger 注解、权限/租户上下文、基础验收用例。

## 本项目检查命令

```bash
./scripts/coding-check.sh
```

该脚本会执行 Maven `coding-check` Profile，加载 Alibaba P3C PMD 规则集。

## IDE 建议

- 安装 Alibaba Java Coding Guidelines 插件。
- 开启保存时格式化，但不要使用和 `.editorconfig` 冲突的缩进设置。
- Java 行宽建议 120，SQL 行宽建议 160。
