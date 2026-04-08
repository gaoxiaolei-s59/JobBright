package org.puregxl.site.clawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhaopin")
public class ZhaopinProperties {

    private boolean enabled = true;
    private String searchPageUrl;
    private String searchBaseUrl;
    private String detailBaseUrl;
    private int defaultPageSize = 20;
    private long minIntervalMillis = 1500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSearchBaseUrl() {
        return searchBaseUrl;
    }

    public String getSearchPageUrl() {
        return searchPageUrl;
    }

    public void setSearchPageUrl(String searchPageUrl) {
        this.searchPageUrl = searchPageUrl;
    }

    public void setSearchBaseUrl(String searchBaseUrl) {
        this.searchBaseUrl = searchBaseUrl;
    }

    public String getDetailBaseUrl() {
        return detailBaseUrl;
    }

    public void setDetailBaseUrl(String detailBaseUrl) {
        this.detailBaseUrl = detailBaseUrl;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public long getMinIntervalMillis() {
        return minIntervalMillis;
    }

    public void setMinIntervalMillis(long minIntervalMillis) {
        this.minIntervalMillis = minIntervalMillis;
    }
}
