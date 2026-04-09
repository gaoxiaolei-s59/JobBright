package org.puregxl.site.clawler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.puregxl.site.clawler.config.LiepinProperties;
import org.puregxl.site.clawler.dto.LiepinFetchedJob;
import org.puregxl.site.clawler.dto.LiepinSearchRequest;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.util.SourceKeyGenerator;
import org.puregxl.site.clawler.util.TextCleaner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LiepinClient {

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LiepinProperties properties;
    private final LiepinRequestThrottle throttle;

    public LiepinClient(
            @Qualifier("liepinHttpClient") HttpClient httpClient,
            ObjectMapper objectMapper,
            LiepinProperties properties,
            LiepinRequestThrottle throttle
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.throttle = throttle;
    }

    public List<LiepinFetchedJob> searchJobs(LiepinSearchRequest request) {
        ensureEnabled();
        throttle.acquire(properties.getMinIntervalMillis());
        SessionContext sessionContext = openSession(request);
        throttle.acquire(properties.getMinIntervalMillis());
        String responseBody = fetchJobList(request, sessionContext);
        return parseResponse(responseBody);
    }

    private SessionContext openSession(LiepinSearchRequest request) {
        String userAgent = randomUserAgent();
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(buildSearchPageUrl(request)))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("User-Agent", userAgent)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("猎聘搜索页返回异常状态码: " + response.statusCode());
            }
            String cookieHeader = buildCookieHeader(response.headers().allValues("Set-Cookie"));
            String xsrfToken = extractCookieValue(response.headers().allValues("Set-Cookie"), "XSRF-TOKEN");
            return new SessionContext(userAgent, cookieHeader, xsrfToken);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("打开猎聘搜索页失败", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("打开猎聘搜索页失败", exception);
        }
    }

    private String fetchJobList(LiepinSearchRequest request, SessionContext sessionContext) {
        String ckId = randomToken(32);
        String traceId = UUID.randomUUID().toString();
        int currentPage = request.page() == null ? 0 : request.page() - 1;
        int pageSize = request.pageSize() == null ? properties.getDefaultPageSize() : request.pageSize();
        String payload = buildRequestBody(request, ckId, currentPage, pageSize);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(properties.getSearchApiUrl()))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Content-Type", "application/json")
                .header("Origin", "https://www.liepin.com")
                .header("Referer", buildSearchPageUrl(request))
                .header("User-Agent", sessionContext.userAgent())
                .header("X-Client-Type", "web")
                .header("X-Fscp-Bi-Stat", buildBiStat(request, ckId, currentPage, pageSize))
                .header("X-Fscp-Std-Info", "{\"client_id\":\"40108\"}")
                .header("X-Fscp-Trace-Id", traceId)
                .header("X-Fscp-Version", "1.1")
                .header("X-Requested-With", "XMLHttpRequest")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        if (!TextCleaner.isBlank(sessionContext.cookieHeader())) {
            builder.header("Cookie", sessionContext.cookieHeader());
        }
        if (!TextCleaner.isBlank(sessionContext.xsrfToken())) {
            builder.header("X-XSRF-TOKEN", sessionContext.xsrfToken());
        }
        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("猎聘搜索接口返回异常状态码: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("调用猎聘搜索接口失败", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("调用猎聘搜索接口失败", exception);
        }
    }

    private List<LiepinFetchedJob> parseResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.path("flag").asInt() != 1) {
                throw new IllegalStateException("猎聘搜索接口返回失败: " + root.path("msg").asText("未知错误"));
            }
            JsonNode jobCardList = root.path("data").path("data").path("jobCardList");
            List<LiepinFetchedJob> jobs = new ArrayList<>();
            if (!jobCardList.isArray()) {
                return jobs;
            }
            for (JsonNode item : jobCardList) {
                LiepinFetchedJob fetchedJob = mapJobCard(item);
                if (fetchedJob != null) {
                    jobs.add(fetchedJob);
                }
            }
            return jobs;
        } catch (IOException exception) {
            throw new IllegalStateException("解析猎聘搜索结果失败", exception);
        }
    }

    private LiepinFetchedJob mapJobCard(JsonNode item) {
        JsonNode jobNode = item.path("job");
        JsonNode compNode = item.path("comp");
        JobPosting posting = new JobPosting();
        posting.setSourceSite("liepin");
        posting.setTitle(cleanText(jobNode.path("title").asText(null)));
        posting.setCompany(cleanText(compNode.path("compName").asText(null)));
        posting.setLocation(cleanText(jobNode.path("dq").asText(null)));
        posting.setSalary(cleanText(jobNode.path("salary").asText(null)));
        posting.setSourceUrl(cleanText(jobNode.path("link").asText(null)));
        posting.setCrawledAt(LocalDateTime.now());
        posting.setSummary(buildSummary(jobNode));
        if (TextCleaner.isBlank(posting.getTitle())) {
            return null;
        }
        posting.setSourceKey(SourceKeyGenerator.generate(posting));
        return new LiepinFetchedJob(
                posting,
                cleanText(jobNode.path("jobId").asText(null)),
                compNode.hasNonNull("compId") ? compNode.path("compId").asText() : null,
                cleanText(compNode.path("compScale").asText(null)),
                cleanText(compNode.path("compIndustry").asText(null)),
                resolveCompanyLogo(compNode.path("compLogo").asText(null)),
                cleanText(jobNode.path("refreshTime").asText(null)),
                cleanText(compNode.path("compStage").asText(null)),
                item.toString()
        );
    }

    private String buildSummary(JsonNode jobNode) {
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, cleanText(jobNode.path("requireWorkYears").asText(null)));
        addIfPresent(parts, cleanText(jobNode.path("requireEduLevel").asText(null)));
        JsonNode labels = jobNode.path("labels");
        if (labels.isArray()) {
            for (JsonNode label : labels) {
                addIfPresent(parts, cleanText(label.asText(null)));
            }
        }
        String summary = String.join(" | ", parts);
        return TextCleaner.isBlank(summary) ? null : TextCleaner.limit(summary, 1800);
    }

    private void addIfPresent(List<String> parts, String value) {
        if (!TextCleaner.isBlank(value)) {
            parts.add(value);
        }
    }

    private String buildSearchPageUrl(LiepinSearchRequest request) {
        StringBuilder builder = new StringBuilder(properties.getSearchPageUrl())
                .append("?key=").append(encode(request.keyword()));
        if (!TextCleaner.isBlank(request.cityCode())) {
            builder.append("&dq=").append(encode(request.cityCode()));
        }
        return builder.toString();
    }

    private String buildBiStat(LiepinSearchRequest request, String ckId, int currentPage, int pageSize) {
        StringBuilder location = new StringBuilder("https://www.liepin.com/zhaopin/?");
        if (!TextCleaner.isBlank(request.cityCode())) {
            location.append("dq=").append(encode(request.cityCode())).append("&");
        }
        location.append("currentPage=").append(currentPage)
                .append("&pageSize=").append(pageSize)
                .append("&key=").append(encode(request.keyword()))
                .append("&sfrom=search_job_pc")
                .append("&ckId=").append(ckId)
                .append("&scene=init")
                .append("&suggestId=");
        return "{\"location\":\"" + location + "\"}";
    }

    private String buildRequestBody(LiepinSearchRequest request, String ckId, int currentPage, int pageSize) {
        String cityCode = TextCleaner.defaultString(request.cityCode());
        String keyword = TextCleaner.defaultString(request.keyword());
        return """
                {"data":{"mainSearchPcConditionForm":{"city":"","dq":"%s","pubTime":"","currentPage":%d,"pageSize":%d,"key":"%s","suggestTag":"","workYearCode":"","compId":"","compName":"","compTag":"","industry":"","salary":"","jobKind":"","compScale":"","compKind":"","compStage":"","eduLevel":"","otherCity":""},"passThroughForm":{"scene":"init","ckId":"%s","suggest":null}}}
                """.formatted(
                escapeJson(cityCode),
                currentPage,
                pageSize,
                escapeJson(keyword),
                escapeJson(ckId)
        );
    }

    private String buildCookieHeader(List<String> setCookieHeaders) {
        List<String> cookies = new ArrayList<>();
        for (String setCookie : setCookieHeaders) {
            if (TextCleaner.isBlank(setCookie)) {
                continue;
            }
            int separator = setCookie.indexOf(';');
            cookies.add(separator >= 0 ? setCookie.substring(0, separator) : setCookie);
        }
        return String.join("; ", cookies);
    }

    private String extractCookieValue(List<String> setCookieHeaders, String cookieName) {
        for (String setCookie : setCookieHeaders) {
            if (TextCleaner.isBlank(setCookie)) {
                continue;
            }
            String[] cookieParts = setCookie.split(";", 2);
            String[] keyValue = cookieParts[0].split("=", 2);
            if (keyValue.length == 2 && cookieName.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private String resolveCompanyLogo(String logo) {
        String cleanedLogo = cleanText(logo);
        if (TextCleaner.isBlank(cleanedLogo)) {
            return null;
        }
        if (cleanedLogo.startsWith("http://") || cleanedLogo.startsWith("https://")) {
            return cleanedLogo;
        }
        return properties.getLogoBaseUrl() + cleanedLogo;
    }

    private String cleanText(String value) {
        return TextCleaner.clean(value);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String randomUserAgent() {
        return USER_AGENTS.get(ThreadLocalRandom.current().nextInt(USER_AGENTS.size()));
    }

    private String randomToken(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(TextCleaner.defaultString(value), StandardCharsets.UTF_8);
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("猎聘抓取功能未启用");
        }
    }

    private record SessionContext(
            String userAgent,
            String cookieHeader,
            String xsrfToken
    ) {
    }
}
