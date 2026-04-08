package org.puregxl.site.clawler.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.puregxl.site.clawler.entity.JobPosting;
import org.puregxl.site.clawler.util.SourceKeyGenerator;
import org.puregxl.site.clawler.util.TextCleaner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlJobCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LineJobCrawler/1.0; +https://localhost)";
    private static final int TIMEOUT_MILLIS = 15_000;

    public List<JobPosting> crawl(SiteCrawlConfig config) {
        try {
            Document document = Jsoup.connect(config.pageUrl())
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .get();

            Elements items = document.select(config.itemSelector());
            int maxItems = config.maxItems() == null || config.maxItems() < 1
                    ? items.size()
                    : Math.min(config.maxItems(), items.size());

            List<JobPosting> postings = new ArrayList<>();
            for (int i = 0; i < maxItems; i++) {
                Element item = items.get(i);
                JobPosting posting = mapToJobPosting(config, item);
                if (posting != null) {
                    postings.add(posting);
                }
            }
            return postings;
        } catch (IOException exception) {
            throw new IllegalStateException("抓取页面失败: " + config.pageUrl(), exception);
        }
    }

    private JobPosting mapToJobPosting(SiteCrawlConfig config, Element item) {
        String title = extractValue(item, config.title());
        if (TextCleaner.isBlank(title)) {
            return null;
        }

        String sourceUrl = extractValue(item, config.jobLink());
        sourceUrl = buildAbsoluteUrl(config.pageUrl(), sourceUrl);

        JobPosting posting = new JobPosting();
        posting.setSourceSite(config.siteName());
        posting.setTitle(title);
        posting.setCompany(extractValue(item, config.company()));
        posting.setLocation(extractValue(item, config.location()));
        posting.setSalary(extractValue(item, config.salary()));
        posting.setSummary(extractValue(item, config.summary()));
        posting.setSourceUrl(sourceUrl);
        posting.setCrawledAt(LocalDateTime.now());
        posting.setSourceKey(SourceKeyGenerator.generate(posting));
        return posting;
    }

    private String extractValue(Element root, FieldSelector selector) {
        if (selector == null || TextCleaner.isBlank(selector.selector())) {
            return null;
        }

        Element element = root.selectFirst(selector.selector());
        if (element == null) {
            return null;
        }

        String rawValue = TextCleaner.isBlank(selector.attribute())
                ? element.text()
                : element.attr(selector.attribute());
        return TextCleaner.clean(rawValue);
    }

    private String buildAbsoluteUrl(String pageUrl, String maybeRelativeUrl) {
        if (TextCleaner.isBlank(maybeRelativeUrl)) {
            return null;
        }
        try {
            return URI.create(pageUrl).resolve(maybeRelativeUrl).toString();
        } catch (IllegalArgumentException exception) {
            return maybeRelativeUrl;
        }
    }
}
