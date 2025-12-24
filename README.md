<div align="center">
  <h1>HowillSHOP 小昊商城</h2>
  <p>基于 Java Web 的多角色电商项目（消费者 / 商家）</p>
  <p>
    <img src="https://img.shields.io/badge/JAVA-11-000000?style=flat-square&labelColor=000000&color=000000" alt="Java 11" />
    <img src="https://img.shields.io/badge/TOMCAT-9.x-f8c04d?style=flat-square&labelColor=f8c04d&color=f8c04d" alt="Tomcat 9" />
    <img src="https://img.shields.io/badge/SERVLET-4.0.1-2f6db3?style=flat-square&labelColor=2f6db3&color=2f6db3" alt="Servlet" />
    <img src="https://img.shields.io/badge/JSP-2.3.3-2f6db3?style=flat-square&labelColor=2f6db3&color=2f6db3" alt="JSP" />
    <img src="https://img.shields.io/badge/MYSQL-8.x-1d6f42?style=flat-square&labelColor=1d6f42&color=1d6f42" alt="MySQL" />
  </p>
  <p>
    <img src="https://img.shields.io/badge/MAVEN-3.9+-c71a36?style=flat-square&labelColor=c71a36&color=c71a36" alt="Maven" />
    <img src="https://img.shields.io/badge/HIKARICP-5.x-4a4a4a?style=flat-square&labelColor=4a4a4a&color=4a4a4a" alt="HikariCP" />
    <img src="https://img.shields.io/badge/JAVAMAIL-2.0.1-4a4a4a?style=flat-square&labelColor=4a4a4a&color=4a4a4a" alt="JavaMail" />
    <img src="https://img.shields.io/badge/ALIYUN%20OSS-3.17.4-ff6a00?style=flat-square&labelColor=ff6a00&color=ff6a00" alt="Aliyun OSS" />
  </p>
</div>

<p align="center">
  <img src="assets/mainpage.png" alt="HowillSHOP 首页" width="860" />
</p>

---

<p align="center" style="margin-top: -8px;">
  <a href="#功能特性">功能特性</a>
  · <a href="#技术栈">技术栈</a>
  · <a href="#快速开始">快速开始</a>
  · <a href="#目录结构">目录结构</a>
  · <a href="#常见问题">常见问题</a>
</p>

## 功能特性

- 用户/商家注册、登录、注销
- 商品展示、分类筛选、搜索、详情页
- 购物车增删改查、总价计算
- 结算与支付（模拟）
- 多商家拆单：同一购物车按商家分成多笔订单
- 订单列表与订单详情
- 商家商品管理（仅管理自己的商品）
- 商家发货 + 邮件通知（含商品明细）
- OSS 图片上传、私有读签名 URL
- 销售统计报表

## 技术栈

- Java 11 + Servlet/JSP
- Tomcat 9
- MySQL 8 + HikariCP
- Maven
- JavaMail（SMTP 发货通知）
- 阿里云 OSS（商品图片）

## 快速开始

### 1) 环境准备

- JDK 11
- Maven 3.9+
- Tomcat 9
- MySQL 8

### 2) 初始化数据库

```
mysql -u root -p < db/schema.sql
mysql -u root -p javaweb_shop < db/seed.sql
```

### 3) 配置 .env

支持 `.env` 覆盖 `db.properties` / `mail.properties` / `oss.properties`。

```
DB_URL=jdbc:mysql://localhost:3306/javaweb_shop?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_password

MAIL_SMTP_HOST=smtp.qq.com
MAIL_SMTP_PORT=587
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_USERNAME=xxx@qq.com
MAIL_PASSWORD=app_password
MAIL_FROM=xxx@qq.com

OSS_ENDPOINT=oss-cn-xxx.aliyuncs.com
OSS_BUCKET=your-bucket
OSS_ACCESS_KEY_ID=your-key-id
OSS_ACCESS_KEY_SECRET=your-key-secret
OSS_BASE_URL=https://your-bucket.oss-cn-xxx.aliyuncs.com/
OSS_PREFIX=products/
OSS_SIGN_EXPIRE_SECONDS=3600
```

### 4) 开发模式（Tomcat 热重载）

先编译：
```
mvn -DskipTests compile
```

在 `TOMCAT_HOME/conf/Catalina/localhost/shop.xml` 新建：
```xml
<Context docBase="G:/project/javaweb/src/main/webapp" reloadable="true">
  <Resources className="org.apache.catalina.webresources.StandardRoot">
    <PreResources className="org.apache.catalina.webresources.DirResourceSet"
                  base="G:/project/javaweb/target/classes"
                  webAppMount="/WEB-INF/classes" />
  </Resources>
</Context>
```

启动 Tomcat：
```
%CATALINA_HOME%\bin\startup.bat
```

访问：
```
http://localhost:8080/shop/products
```

### 5) 打包部署（WAR）

```
mvn -DskipTests package
```

将 `target/javaweb-shop-1.0-SNAPSHOT.war` 放入 Tomcat 的 `webapps/`，可重命名为 `shop.war`：
```
http://localhost:8080/shop
```

## 目录结构

```
db/                         # 数据库脚本（schema/seed）
src/main/java/com/javaweb/shop
  ├── web/                  # Servlet
  ├── service/              # 业务逻辑
  ├── dao/                  # 数据访问
  ├── model/                # 实体模型
  ├── infra/                # 基础设施（连接池等）
  └── util/                 # 工具类
src/main/resources           # db/mail/oss 配置
src/main/webapp              # JSP 页面、静态资源、WEB-INF
docs/                        # 项目文档
assets/                      # README 展示图片
```
