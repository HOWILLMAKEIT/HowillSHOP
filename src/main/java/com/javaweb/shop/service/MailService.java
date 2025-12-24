package com.javaweb.shop.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.javaweb.shop.model.OrderItem;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

// 发货邮件发送服务
public class MailService {
    private final Properties mailProps;

    public MailService() throws ValidationException {
        this.mailProps = loadProperties();
    }

    public void sendShipmentEmail(String toEmail, String orderNo, String merchantName,
                                  List<OrderItem> items, LocalDateTime shippedAt)
            throws ValidationException {
        if (toEmail == null || toEmail.isBlank()) {
            throw new ValidationException("用户邮箱为空。");
        }
        try {
            Session session = buildSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailProps.getProperty("mail.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("订单已发货通知");
            message.setText(buildShipmentContent(orderNo, merchantName, items, shippedAt));
            Transport.send(message);
        } catch (Exception ex) {
            throw new ValidationException("邮件发送失败。");
        }
    }

    private String buildShipmentContent(String orderNo, String merchantName,
                                        List<OrderItem> items, LocalDateTime shippedAt) {
        String displayMerchant = isBlank(merchantName) ? "商家" : merchantName;
        String timeText = shippedAt == null ? "已发货"
                : shippedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder content = new StringBuilder();
        content.append("您好，感谢您在 HowillSHOP 小昊商城 购物！").append("\n\n");
        content.append("您的订单已发货，详情如下：").append("\n");
        content.append("订单号：").append(orderNo).append("\n");
        content.append("发货时间：").append(timeText).append("\n");
        content.append("发货店铺：").append(displayMerchant).append("\n\n");

        content.append("发货商品：").append("\n");
        if (items == null || items.isEmpty()) {
            content.append("- 请在订单详情中查看商品明细").append("\n");
        } else {
            for (OrderItem item : items) {
                content.append("- ")
                        .append(item.getProductName())
                        .append(" × ")
                        .append(item.getQuantity())
                        .append("\n");
            }
        }
        content.append("\n如有问题，请联系发货店铺或 HowillSHOP 小昊商城 客服。");
        return content.toString();
    }

    private Session buildSession() {
        String username = mailProps.getProperty("mail.username");
        String password = mailProps.getProperty("mail.password");
        boolean auth = "true".equalsIgnoreCase(mailProps.getProperty("mail.smtp.auth"));
        if (auth) {
            // SMTP 需要认证时才走 Authenticator
            return Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        return Session.getInstance(mailProps);
    }

    private Properties loadProperties() throws ValidationException {
        Properties props = new Properties();
        try (InputStream in = MailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ex) {
            throw new ValidationException("邮件配置加载失败。");
        }

        // .env 优先级最高，方便本地覆盖
        overrideFromEnvFile(props);
        overrideFromEnv(props, "MAIL_SMTP_HOST", "mail.smtp.host");
        overrideFromEnv(props, "MAIL_SMTP_PORT", "mail.smtp.port");
        overrideFromEnv(props, "MAIL_SMTP_AUTH", "mail.smtp.auth");
        overrideFromEnv(props, "MAIL_SMTP_STARTTLS", "mail.smtp.starttls.enable");
        overrideFromEnv(props, "MAIL_USERNAME", "mail.username");
        overrideFromEnv(props, "MAIL_PASSWORD", "mail.password");
        overrideFromEnv(props, "MAIL_FROM", "mail.from");

        if (isBlank(props.getProperty("mail.smtp.host"))) {
            throw new ValidationException("缺少 mail.smtp.host 配置。");
        }
        if (isBlank(props.getProperty("mail.from"))) {
            throw new ValidationException("缺少 mail.from 配置。");
        }

        if (isBlank(props.getProperty("mail.smtp.port"))) {
            props.setProperty("mail.smtp.port", "587");
        }
        if (isBlank(props.getProperty("mail.smtp.auth"))) {
            props.setProperty("mail.smtp.auth", "true");
        }
        if (isBlank(props.getProperty("mail.smtp.starttls.enable"))) {
            props.setProperty("mail.smtp.starttls.enable", "true");
        }

        if ("true".equalsIgnoreCase(props.getProperty("mail.smtp.auth"))) {
            if (isBlank(props.getProperty("mail.username")) || isBlank(props.getProperty("mail.password"))) {
                throw new ValidationException("缺少 mail.username 或 mail.password 配置。");
            }
        }

        return props;
    }

    private void overrideFromEnv(Properties props, String envKey, String propKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            props.setProperty(propKey, value);
        }
    }

    private void overrideFromEnvFile(Properties props) throws ValidationException {
        String envPath = System.getenv("ENV_FILE");
        if (isBlank(envPath)) {
            envPath = System.getProperty("env.file");
        }
        if (isBlank(envPath)) {
            envPath = ".env";
        }

        Path path = Paths.get(envPath);
        if (!Files.exists(path)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                value = stripQuotes(value);
                applyEnvValue(props, key, value);
            }
        } catch (Exception ex) {
            throw new ValidationException(".env 文件读取失败。");
        }
    }

    private void applyEnvValue(Properties props, String key, String value) {
        if (isBlank(value)) {
            return;
        }
        String upper = key.trim().toUpperCase();
        if ("MAIL_SMTP_HOST".equals(upper)) {
            props.setProperty("mail.smtp.host", value);
        } else if ("MAIL_SMTP_PORT".equals(upper)) {
            props.setProperty("mail.smtp.port", value);
        } else if ("MAIL_SMTP_AUTH".equals(upper)) {
            props.setProperty("mail.smtp.auth", value);
        } else if ("MAIL_SMTP_STARTTLS".equals(upper)) {
            props.setProperty("mail.smtp.starttls.enable", value);
        } else if ("MAIL_USERNAME".equals(upper)) {
            props.setProperty("mail.username", value);
        } else if ("MAIL_PASSWORD".equals(upper)) {
            props.setProperty("mail.password", value);
        } else if ("MAIL_FROM".equals(upper)) {
            props.setProperty("mail.from", value);
        } else if (key.startsWith("mail.")) {
            props.setProperty(key, value);
        }
    }

    private String stripQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
