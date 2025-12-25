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
  · <a href="#上线部署指南ecs--nginx-反向代理">上线部署指南</a>
  · <a href="#代码结构">目录结构</a>
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
<Context docBase="YOUR_PROJECT_PATH/src/main/webapp" reloadable="true">
  <Resources className="org.apache.catalina.webresources.StandardRoot">
    <PreResources className="org.apache.catalina.webresources.DirResourceSet"
                  base="YOUR_PROJECT_PATH/target/classes"
                  webAppMount="/WEB-INF/classes" />
  </Resources>
</Context>
```
说明：
- `docBase` 是你的本地 Web 根目录（JSP/静态资源），这里用的是**绝对路径**，需要改成你电脑上的实际路径。
- `PreResources` 把 `target/classes` 映射到 `/WEB-INF/classes`，Tomcat 会优先加载这里的最新 class。
- 修改 Java 后重新执行 `mvn -DskipTests compile`，Tomcat 会自动加载新 class。
- 依赖库（`WEB-INF/lib`）不会从 `target/classes` 自动更新；**首次配置或依赖变更时**需确保 `src/main/webapp/WEB-INF/lib` 有最新依赖。
  - 简单做法：执行一次 `mvn -DskipTests package`（会把依赖打入 WAR）
  - 或手动同步依赖：`mvn -DskipTests dependency:copy-dependencies -DoutputDirectory=src/main/webapp/WEB-INF/lib`

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

将 `target/howill-shop-1.0.0.war` 放入 Tomcat 的 `webapps/`，可重命名为 `shop.war`：
```
http://localhost:8080/shop
```

## 上线部署指南（ECS + Nginx + systemd）

> 以阿里云 ECS 为例，使用 HTTP 反向代理（不启用 HTTPS）。

### 0) 购买与安全组

- 2 核 4G 起步（小流量），系统盘 40GB+
- 系统：Alibaba Cloud Linux 3 / Ubuntu 22.04
- 安全组放行：22、80（如需直连 Tomcat 再放行 8080）

### 1) 安装 JDK 11、Tomcat 9

```bash
# Alibaba Cloud Linux / CentOS
sudo yum -y install java-11-openjdk wget
cd /opt
wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.113/bin/apache-tomcat-9.0.113.tar.gz
tar -xzf apache-tomcat-9.0.113.tar.gz
ln -s apache-tomcat-9.0.113 tomcat
```

### 2) 创建 tomcat 用户并授权（推荐）

```bash
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat
```

### 3) MySQL 初始化

```bash
mysql -u root -p < /path/db/schema.sql
mysql -u root -p javaweb_shop < /path/db/seed.sql
```

### 4) 上传 WAR

```bash
scp target/howill-shop-1.0.0.war root@服务器IP:/opt/tomcat/webapps/shop.war
```

### 5) 配置 .env

将 `.env` 上传到服务器，比如 `/opt/shop/.env`。

### 6) 使用 systemd 管理 Tomcat

创建 `/etc/systemd/system/tomcat.service`：

```ini
[Unit]
Description=Apache Tomcat 9
After=network.target

[Service]
Type=forking
User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="JAVA_OPTS=-Denv.file=/opt/shop/.env"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启用并启动：
```bash
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat
sudo systemctl status tomcat
```

此时 Tomcat 地址为：
```
http://服务器IP:8080/shop
```

### 7) 安装并配置 Nginx（HTTP 反向代理）

```bash
sudo yum -y install nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

在 `/etc/nginx/conf.d/shop.conf` 写入：

```nginx
server {
    listen 80;
    server_name 你的域名或公网IP;

    # 访问根路径时跳转到应用上下文
    location = / {
        return 302 /shop/;
    }

    # 注意：proxy_pass 不带路径，避免资源路径重复
    location /shop/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo nginx -t
sudo systemctl reload nginx
```

访问：
```
http://你的域名/
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
