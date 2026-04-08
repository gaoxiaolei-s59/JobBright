package org.puregxl.site.clawler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.puregxl.site.clawler.config.ZhaopinProperties;
import org.puregxl.site.clawler.dto.ZhaopinFetchedJob;
import org.puregxl.site.clawler.dto.ZhaopinJobRawTagItem;
import org.puregxl.site.clawler.dto.ZhaopinSearchRequest;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ZhaopinClient {

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ZhaopinProperties properties;
    private final ZhaopinRequestThrottle throttle;

    public ZhaopinClient(
            @Qualifier("zhaopinHttpClient") HttpClient httpClient,
            ObjectMapper objectMapper,
            ZhaopinProperties properties,
            ZhaopinRequestThrottle throttle
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.throttle = throttle;
    }

    public List<ZhaopinFetchedJob> searchJobs(ZhaopinSearchRequest request) {
        ensureEnabled();
        throttle.acquire(properties.getMinIntervalMillis());

        String requestId = buildRequestId();
        String userAgent = randomUserAgent();
        List<ZhaopinFetchedJob> htmlResults = fetchFromSearchPage(request, userAgent);
        if (!htmlResults.isEmpty()) {
            return htmlResults;
        }

        String requestUrl = buildSearchApiUrl(request, requestId);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", "https://sou.zhaopin.com/")
                .header("User-Agent", userAgent)
                .header("x-zp-page-request-id", requestId)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("智联搜索接口返回异常状态码: " + response.statusCode());
            }
            return parseListResponse(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("调用智联搜索接口失败", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("调用智联搜索接口失败", exception);
        }
    }

    private List<ZhaopinFetchedJob> fetchFromSearchPage(ZhaopinSearchRequest request, String userAgent) {
        String pageUrl = buildSearchPageUrl(request);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(pageUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", "https://www.zhaopin.com/")
                .header("User-Agent", userAgent)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                return List.of();
            }
            return parseSearchPage(response.body(), pageUrl);
        } catch (IOException exception) {
            return List.of();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    public String fetchJobDetailSummary(String sourceUrl) {
        if (TextCleaner.isBlank(sourceUrl)) {
            return null;
        }
        throttle.acquire(properties.getMinIntervalMillis());
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(sourceUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", "https://www.zhaopin.com/")
                .header("User-Agent", randomUserAgent())
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                return null;
            }
            Document document = Jsoup.parse(response.body(), sourceUrl);
            String text = TextCleaner.clean(document.select(".describtion__detail-content, .jobdetail-box, .jobdetail").text());
            if (TextCleaner.isBlank(text)) {
                text = TextCleaner.clean(document.body().text());
            }
            return TextCleaner.limit(text, 1800);
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private List<ZhaopinFetchedJob> parseListResponse(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode dataNode = firstNonNull(root.get("data"), root.get("result"));
        JsonNode listNode = firstNonNull(
                dataNode == null ? null : dataNode.get("results"),
                dataNode == null ? null : dataNode.get("list"),
                dataNode == null ? null : dataNode.get("positionResult")
        );
        return parsePositionArray(listNode);
    }

    private List<ZhaopinFetchedJob> parseSearchPage(String html, String pageUrl) throws IOException {
        Document document = Jsoup.parse(html, pageUrl);
        String stateJson = extractInitialStateJson(document);
        if (!TextCleaner.isBlank(stateJson)) {
            JsonNode root = objectMapper.readTree(stateJson);
            JsonNode listNode = firstNonNull(root.get("positionList"), root.get("positions"));
            List<ZhaopinFetchedJob> jobs = parsePositionArray(listNode);
            if (!jobs.isEmpty()) {
                return jobs;
            }
        }
        return parseFromDom(document);
    }

    private String extractInitialStateJson(Document document) {
        for (Element script : document.select("script")) {
            String scriptText = script.data();
            String marker = "__INITIAL_STATE__=";
            int markerIndex = scriptText.indexOf(marker);
            if (markerIndex < 0) {
                continue;
            }
            int start = markerIndex + marker.length();
            int end = scriptText.lastIndexOf("</script>");
            String json = end > start ? scriptText.substring(start, end) : scriptText.substring(start);
            if (json.endsWith(";")) {
                json = json.substring(0, json.length() - 1);
            }
            return json.trim();
        }
        return null;
    }

    private List<ZhaopinFetchedJob> parsePositionArray(JsonNode listNode) {
        List<ZhaopinFetchedJob> jobs = new ArrayList<>();
        if (listNode == null || !listNode.isArray()) {
            return jobs;
        }

        Iterator<JsonNode> iterator = listNode.elements();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            ZhaopinFetchedJob fetchedJob = mapPositionNode(node);
            if (fetchedJob != null) {
                jobs.add(fetchedJob);
            }
        }
        return jobs;
    }

    private ZhaopinFetchedJob mapPositionNode(JsonNode node) {
        JobPosting posting = new JobPosting();
        posting.setSourceSite("zhaopin");
        posting.setTitle(cleanValue(node, "name", "jobName", "positionName"));
        posting.setCompany(cleanValue(node, "companyName", "company", "companyTitle"));
        posting.setLocation(cleanValue(node, "workCity", "city", "cityName", "cityDistrict"));
        posting.setSalary(cleanValue(node, "salary60", "salary", "providesalary_text"));
        posting.setSummary(cleanValue(node, "jobSummary"));

        JsonNode jobDetailData = node.get("jobDetailData");
        if (jobDetailData != null && !jobDetailData.isNull()) {
            JsonNode positionNode = jobDetailData.path("position").path("desc");
            if (TextCleaner.isBlank(posting.getSummary())) {
                posting.setSummary(cleanValue(positionNode, "description"));
            }
        }

        posting.setSourceUrl(resolveJobUrl(cleanValue(node, "positionURL", "positionUrl", "jobUrl", "url", "positionDetailUrl")));
        posting.setCrawledAt(LocalDateTime.now());
        if (TextCleaner.isBlank(posting.getTitle())) {
            return null;
        }
        posting.setSummary(TextCleaner.limit(posting.getSummary(), 1800));
        posting.setSourceKey(SourceKeyGenerator.generate(posting));
        return new ZhaopinFetchedJob(
                posting,
                longValue(node, "jobId", "positionId"),
                cleanValue(node, "number", "positionNumber"),
                longValue(node, "companyId"),
                cleanValue(node, "workCity", "city", "cityName"),
                cleanValue(node, "cityDistrict"),
                cleanValue(node, "salary60", "salary", "providesalary_text"),
                cleanValue(node, "workingExp", "workExperience"),
                cleanValue(node, "education"),
                cleanValue(node, "companySize"),
                cleanValue(node, "propertyName", "property"),
                cleanValue(node, "industryName"),
                resolveCompanyLogoUrl(node),
                cleanValue(node, "publishTime"),
                safeRawJson(node),
                extractTags(node)
        );
    }

    private List<ZhaopinFetchedJob> parseFromDom(Document document) {
        List<ZhaopinFetchedJob> jobs = new ArrayList<>();
        for (Element item : document.select(".joblist-box__item")) {
            Element titleElement = item.selectFirst(".jobinfo__name");
            if (titleElement == null) {
                continue;
            }
            JobPosting posting = new JobPosting();
            posting.setSourceSite("zhaopin");
            posting.setTitle(TextCleaner.clean(titleElement.text()));
            posting.setSourceUrl(resolveJobUrl(TextCleaner.clean(titleElement.attr("href"))));
            Element companyElement = item.selectFirst(".companyinfo__name");
            posting.setCompany(companyElement == null ? null : TextCleaner.clean(companyElement.text()));
            Element salaryElement = item.selectFirst(".jobinfo__salary");
            posting.setSalary(salaryElement == null ? null : TextCleaner.clean(salaryElement.text()));
            Element locationElement = item.selectFirst(".jobinfo__other-info-item span");
            posting.setLocation(locationElement == null ? null : TextCleaner.clean(locationElement.text()));
            List<String> tags = item.select(".jobinfo__tag .joblist-box__item-tag").eachText();
            posting.setSummary(TextCleaner.limit(TextCleaner.clean(String.join(" ", tags)), 1800));
            posting.setCrawledAt(LocalDateTime.now());
            posting.setSourceKey(SourceKeyGenerator.generate(posting));
            jobs.add(new ZhaopinFetchedJob(
                    posting,
                    null,
                    null,
                    null,
                    posting.getLocation(),
                    null,
                    posting.getSalary(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "{}",
                    tags.stream().map(tag -> new ZhaopinJobRawTagItem("skill", tag)).toList()
            ));
        }
        return jobs;
    }

    private List<ZhaopinJobRawTagItem> extractTags(JsonNode node) {
        List<ZhaopinJobRawTagItem> tags = new ArrayList<>();
        collectTagValues(node.get("skillLabel"), "skill", tags);
        collectTagValues(node.get("jobSkillTags"), "skill", tags);
        collectTagValues(node.get("positionCommercialLabel"), "commercial", tags);
        collectTagValues(node.get("welfareTagList"), "welfare", tags);
        collectTagValues(node.get("showSkillTags"), "display", tags);
        return deduplicateTags(tags);
    }

    private List<ZhaopinJobRawTagItem> deduplicateTags(List<ZhaopinJobRawTagItem> tags) {
        List<ZhaopinJobRawTagItem> result = new ArrayList<>();
        for (ZhaopinJobRawTagItem tag : tags) {
            boolean exists = result.stream().anyMatch(item ->
                    item.tagType().equals(tag.tagType()) && item.tagName().equals(tag.tagName()));
            if (!exists) {
                result.add(tag);
            }
        }
        return result;
    }

    private void collectTagValues(JsonNode node, String tagType, List<ZhaopinJobRawTagItem> tags) {
        if (node == null || node.isNull() || !node.isArray()) {
            return;
        }
        for (JsonNode item : node) {
            String tagName = cleanValue(item, "value", "name", "tag", "typeName");
            if (!TextCleaner.isBlank(tagName)) {
                tags.add(new ZhaopinJobRawTagItem(tagType, tagName));
            }
        }
    }

    private String safeRawJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException exception) {
            return "{}";
        }
    }

    private Long longValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isNumber()) {
                return field.longValue();
            }
            String text = TextCleaner.clean(field.asText());
            if (TextCleaner.isBlank(text)) {
                continue;
            }
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String resolveJobUrl(String url) {
        if (TextCleaner.isBlank(url)) {
            return null;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        return properties.getDetailBaseUrl() + (url.startsWith("/") ? url : "/" + url);
    }

    private String resolveCompanyLogoUrl(JsonNode node) {
        return resolveJobUrl(cleanValue(node, "companyLogo", "logoUrl", "logo", "companyLogoUrl"));
    }

    private String buildSearchApiUrl(ZhaopinSearchRequest request, String requestId) {
        int page = request.page() == null ? 1 : request.page();
        int pageSize = request.pageSize() == null ? properties.getDefaultPageSize() : request.pageSize();
        int start = Math.max(page - 1, 0) * pageSize;
        return properties.getSearchBaseUrl()
                + "?start=" + start
                + "&pageSize=" + pageSize
                + "&cityId=" + request.cityId()
                + "&workExperience=" + defaultFilter(request.workExperience())
                + "&education=" + defaultFilter(request.education())
                + "&companyType=" + defaultFilter(request.companyType())
                + "&employmentType=" + defaultFilter(request.employmentType())
                + "&jobWelfareTag=-1"
                + "&kt=3"
                + "&_v=" + buildVersionNonce()
                + "&x-zp-page-request-id=" + encode(requestId)
                + "&kw=" + encode(request.keyword());
    }

    private String buildSearchPageUrl(ZhaopinSearchRequest request) {
        int page = request.page() == null ? 1 : request.page();
        return properties.getSearchPageUrl()
                + "?jl=" + request.cityId()
                + "&kw=" + encode(request.keyword())
                + "&p=" + page;
    }

    private int defaultFilter(Integer value) {
        return value == null ? -1 : value;
    }

    private String buildVersionNonce() {
        double value = ThreadLocalRandom.current().nextDouble(0.1, 0.99999999);
        return String.format("%.8f", value);
    }

    private String buildRequestId() {
        String left = java.util.UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();
        int right = ThreadLocalRandom.current().nextInt(100000, 999999);
        return left + "-" + now + "-" + right;
    }

    private String randomUserAgent() {
        return USER_AGENTS.get(ThreadLocalRandom.current().nextInt(USER_AGENTS.size()));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("当前环境未启用智联客户端");
        }
    }

    private JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private String cleanValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isArray()) {
                List<String> values = new ArrayList<>();
                field.forEach(item -> values.add(item.asText()));
                String joined = String.join(" ", values);
                if (!TextCleaner.isBlank(joined)) {
                    return TextCleaner.clean(joined);
                }
                continue;
            }
            String cleaned = TextCleaner.clean(field.asText());
            if (!TextCleaner.isBlank(cleaned)) {
                return cleaned;
            }
        }
        return null;
    }
}
