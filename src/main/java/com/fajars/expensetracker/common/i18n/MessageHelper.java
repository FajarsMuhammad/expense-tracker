package com.fajars.expensetracker.common.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Helper class for retrieving localized messages.
 *
 * <p>Automatically uses the current locale from LocaleContextHolder,
 * which is set by AcceptHeaderLocaleResolver based on Accept-Language header.
 *
 * <p>Usage:
 * <pre>
 * String message = messageHelper.getMessage("error.wallet.not_found");
 * String messageWithParams = messageHelper.getMessage("error.validation.min_length", "Name", 2);
 * </pre>
 *
 * @since Milestone 7
 */
@Component
@RequiredArgsConstructor
public class MessageHelper {

    private final MessageSource messageSource;

    /**
     * Get localized message for the current locale.
     *
     * @param code message code (e.g., "error.wallet.not_found")
     * @return localized message
     */
    public String getMessage(String code) {
        return getMessage(code, (Object[]) null);
    }

    /**
     * Get localized message with parameters for the current locale.
     *
     * @param code message code
     * @param args parameters to substitute in message (e.g., field names, values)
     * @return localized message with substituted parameters
     */
    public String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }

    /**
     * Get localized message with default fallback.
     *
     * @param code message code
     * @param defaultMessage message to return if code not found
     * @return localized message or default message
     */
    public String getMessageWithDefault(String code, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, defaultMessage, locale);
    }

    /**
     * Get localized message with parameters and default fallback.
     *
     * @param code message code
     * @param defaultMessage message to return if code not found
     * @param args parameters to substitute in message
     * @return localized message with parameters or default message
     */
    public String getMessageWithDefault(String code, String defaultMessage, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    /**
     * Get current locale from context.
     *
     * @return current locale (id or en)
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Get current locale language code (e.g., "id", "en").
     *
     * @return language code
     */
    public String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }
}
