package org.puregxl.site.clawler.crawler;

public record FieldSelector(
        String selector,
        String attribute,
        boolean required
) {
}
