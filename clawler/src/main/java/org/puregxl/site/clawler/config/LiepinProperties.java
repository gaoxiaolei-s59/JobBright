package org.puregxl.site.clawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "liepin")
public class LiepinProperties {

    private boolean enabled = true;
    private String searchPageUrl;
    private String searchApiUrl;
    private String logoBaseUrl;
    private int defaultPageSize = 20;
    private long minIntervalMillis = 1500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSearchPageUrl() {
        return searchPageUrl;
    }

    public void setSearchPageUrl(String searchPageUrl) {
        this.searchPageUrl = searchPageUrl;
    }

    public String getSearchApiUrl() {
        return searchApiUrl;
    }

    public void setSearchApiUrl(String searchApiUrl) {
        this.searchApiUrl = searchApiUrl;
    }

    public String getLogoBaseUrl() {
        return logoBaseUrl;
    }

    public void setLogoBaseUrl(String logoBaseUrl) {
        this.logoBaseUrl = logoBaseUrl;
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
