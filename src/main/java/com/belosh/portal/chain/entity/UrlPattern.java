package com.belosh.portal.chain.entity;

import java.util.regex.Pattern;

public class UrlPattern {
    private static final Pattern PATTERN = Pattern.compile("/?\\*");
    private final int position;
    private final String processedUrl;

    public UrlPattern(String processedUrl) {
        this.position = processedUrl.startsWith("*") ? 1
                : processedUrl.endsWith("*") ? -1
                : 0;
        this.processedUrl = PATTERN.matcher(processedUrl).replaceAll("");
    }

    public boolean matches(String path) {
        return (position == -1) ? path.startsWith(processedUrl)
                : (position == 1) ? path.endsWith(processedUrl)
                : path.equals(processedUrl);
    }
}
