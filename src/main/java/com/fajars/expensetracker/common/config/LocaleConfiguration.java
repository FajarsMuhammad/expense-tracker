package com.fajars.expensetracker.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Configuration for internationalization (i18n) support.
 *
 * <p>Features:
 * <ul>
 *   <li>Accept-Language header parsing</li>
 *   <li>Default locale: Indonesian (id)</li>
 *   <li>Supported locales: Indonesian (id), English (en)</li>
 *   <li>Automatic fallback to Indonesian for unsupported locales</li>
 *   <li>UTF-8 encoding for message properties</li>
 * </ul>
 *
 * <p>Message files location: src/main/resources/i18n/messages_{locale}.properties
 *
 * @since Milestone 7
 */
@Configuration
public class LocaleConfiguration {

    /**
     * Configure locale resolver to read from Accept-Language header.
     *
     * <p>This resolver:
     * <ul>
     *   <li>Reads Accept-Language header from HTTP requests</li>
     *   <li>Falls back to Indonesian (id) if header missing or unsupported</li>
     *   <li>Supports only id and en locales</li>
 * </ul>
     *
     * @return configured locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("id"));
        resolver.setSupportedLocales(Arrays.asList(
            Locale.forLanguageTag("id"),  // Indonesian
            Locale.forLanguageTag("en")   // English
        ));
        return resolver;
    }

    /**
     * Configure message source for loading localized messages.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Basename: i18n/messages (loads messages_id.properties, messages_en.properties)</li>
     *   <li>Encoding: UTF-8 (supports Indonesian characters)</li>
     *   <li>Fallback: Disabled - always use Indonesian if key not found</li>
     *   <li>Cache: Enabled (production), Disabled (development via -1)</li>
     * </ul>
     *
     * @return configured message source
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.forLanguageTag("id"));

        // Cache messages (set to -1 for no cache in development)
        // messageSource.setCacheSeconds(-1);  // Uncomment for development

        return messageSource;
    }
}
