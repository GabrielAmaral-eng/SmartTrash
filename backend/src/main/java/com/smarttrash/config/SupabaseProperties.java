package com.smarttrash.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smarttrash.supabase")
public record SupabaseProperties(
        String url,
        String publishableKey
) {
    public String restUrl() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("smarttrash.supabase.url must be configured when smarttrash.data-source=supabase");
        }
        return url.replaceAll("/+$", "") + "/rest/v1";
    }

    public String requiredPublishableKey() {
        if (publishableKey == null || publishableKey.isBlank()) {
            throw new IllegalStateException("smarttrash.supabase.publishable-key must be configured when smarttrash.data-source=supabase");
        }
        return publishableKey;
    }
}
