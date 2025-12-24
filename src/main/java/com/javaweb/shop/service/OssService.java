package com.javaweb.shop.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import javax.servlet.http.Part;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

// OSS 图片上传与删除封装
public class OssService {
    private final String endpoint;
    private final String bucket;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String baseUrl;
    private final String prefix;
    private final int signExpireSeconds;

    public OssService() throws ValidationException {
        Properties props = loadProperties();
        this.endpoint = require(props, "oss.endpoint");
        this.bucket = require(props, "oss.bucket");
        this.accessKeyId = require(props, "oss.accessKeyId");
        this.accessKeySecret = require(props, "oss.accessKeySecret");
        this.baseUrl = buildBaseUrl(props.getProperty("oss.baseUrl"));
        this.prefix = normalizePrefix(props.getProperty("oss.prefix"));
        this.signExpireSeconds = parseIntWithDefault(props.getProperty("oss.signExpireSeconds"), 3600);
    }

    public String uploadImage(Part part) throws ValidationException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String contentType = part.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("请上传图片文件。");
        }
        String extension = extractExtension(part.getSubmittedFileName());
        String objectKey = prefix + UUID.randomUUID().toString().replace("-", "") + extension;
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream in = part.getInputStream()) {
            client.putObject(bucket, objectKey, in);
        } catch (Exception ex) {
            throw new ValidationException("图片上传失败。");
        } finally {
            client.shutdown();
        }
        return baseUrl + objectKey;
    }

    public void deleteByUrl(String url) throws ValidationException {
        String objectKey = extractObjectKey(url);
        if (isBlank(objectKey)) {
            return;
        }
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            client.deleteObject(bucket, objectKey);
        } catch (Exception ex) {
            throw new ValidationException("图片删除失败。");
        } finally {
            client.shutdown();
        }
    }

    public String signUrl(String urlOrKey) throws ValidationException {
        if (isBlank(urlOrKey)) {
            return null;
        }
        String trimmed = urlOrKey.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            if (trimmed.contains("?")) {
                return trimmed;
            }
            String objectKey = extractObjectKeyForSign(trimmed);
            if (isBlank(objectKey)) {
                return trimmed;
            }
            return generateSignedUrl(objectKey);
        }
        return generateSignedUrl(trimmed);
    }

    private Properties loadProperties() throws ValidationException {
        Properties props = new Properties();
        try (InputStream in = OssService.class.getClassLoader().getResourceAsStream("oss.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ex) {
            throw new ValidationException("OSS 配置加载失败。");
        }

        overrideFromEnvFile(props);
        overrideFromEnv(props, "OSS_ENDPOINT", "oss.endpoint");
        overrideFromEnv(props, "OSS_BUCKET", "oss.bucket");
        overrideFromEnv(props, "OSS_ACCESS_KEY_ID", "oss.accessKeyId");
        overrideFromEnv(props, "OSS_ACCESS_KEY_SECRET", "oss.accessKeySecret");
        overrideFromEnv(props, "OSS_BASE_URL", "oss.baseUrl");
        overrideFromEnv(props, "OSS_PREFIX", "oss.prefix");
        overrideFromEnv(props, "OSS_SIGN_EXPIRE_SECONDS", "oss.signExpireSeconds");

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
        if ("OSS_ENDPOINT".equals(upper)) {
            props.setProperty("oss.endpoint", value);
        } else if ("OSS_BUCKET".equals(upper)) {
            props.setProperty("oss.bucket", value);
        } else if ("OSS_ACCESS_KEY_ID".equals(upper)) {
            props.setProperty("oss.accessKeyId", value);
        } else if ("OSS_ACCESS_KEY_SECRET".equals(upper)) {
            props.setProperty("oss.accessKeySecret", value);
        } else if ("OSS_BASE_URL".equals(upper)) {
            props.setProperty("oss.baseUrl", value);
        } else if ("OSS_PREFIX".equals(upper)) {
            props.setProperty("oss.prefix", value);
        } else if ("OSS_SIGN_EXPIRE_SECONDS".equals(upper)) {
            props.setProperty("oss.signExpireSeconds", value);
        } else if (key.startsWith("oss.")) {
            props.setProperty(key, value);
        }
    }

    private String require(Properties props, String key) throws ValidationException {
        String value = props.getProperty(key);
        if (isBlank(value)) {
            throw new ValidationException("缺少 OSS 配置：" + key);
        }
        return value.trim();
    }

    private String buildBaseUrl(String configured) {
        if (!isBlank(configured)) {
            String normalized = configured.trim();
            return normalized.endsWith("/") ? normalized : normalized + "/";
        }
        String normalizedEndpoint = endpoint.trim();
        String scheme = "https://";
        if (normalizedEndpoint.startsWith("http://")) {
            scheme = "http://";
            normalizedEndpoint = normalizedEndpoint.substring(7);
        } else if (normalizedEndpoint.startsWith("https://")) {
            normalizedEndpoint = normalizedEndpoint.substring(8);
        }
        String host = normalizedEndpoint.endsWith("/") ?
                normalizedEndpoint.substring(0, normalizedEndpoint.length() - 1) :
                normalizedEndpoint;
        return scheme + bucket + "." + host + "/";
    }

    private String normalizePrefix(String value) {
        if (isBlank(value)) {
            return "products/";
        }
        String trimmed = value.trim();
        if (!trimmed.endsWith("/")) {
            trimmed = trimmed + "/";
        }
        return trimmed;
    }

    private String extractObjectKey(String url) {
        if (isBlank(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return null;
        }
        if (trimmed.startsWith(baseUrl)) {
            return trimmed.substring(baseUrl.length());
        }
        return null;
    }

    private String extractObjectKeyForSign(String url) {
        if (isBlank(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith(baseUrl)) {
            return trimmed.substring(baseUrl.length());
        }
        try {
            URL parsed = new URL(trimmed);
            String path = parsed.getPath();
            if (isBlank(path)) {
                return null;
            }
            String objectKey = path.startsWith("/") ? path.substring(1) : path;
            return objectKey.isEmpty() ? null : objectKey;
        } catch (Exception ex) {
            return null;
        }
    }

    private String generateSignedUrl(String objectKey) throws ValidationException {
        if (isBlank(objectKey)) {
            return null;
        }
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            long expiryMillis = signExpireSeconds <= 0 ? 3600_000L : signExpireSeconds * 1000L;
            Date expiration = new Date(System.currentTimeMillis() + expiryMillis);
            URL signed = client.generatePresignedUrl(bucket, objectKey, expiration);
            return signed == null ? null : signed.toString();
        } catch (Exception ex) {
            throw new ValidationException("图片签名失败。");
        } finally {
            client.shutdown();
        }
    }

    private String extractExtension(String filename) {
        if (isBlank(filename)) {
            return ".jpg";
        }
        String clean = Paths.get(filename).getFileName().toString();
        int dot = clean.lastIndexOf('.');
        if (dot < 0 || dot == clean.length() - 1) {
            return ".jpg";
        }
        return clean.substring(dot);
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

    private int parseIntWithDefault(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
