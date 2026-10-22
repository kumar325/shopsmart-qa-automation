package pages;

import java.math.BigDecimal;

/**
 * Parses prices shown in the UI. BigDecimal, not double: 0.1 + 0.2 != 0.3 in floating point,
 * which would make price assertions flaky.
 */
public final class Money {

    private Money() {
    }

    /** "$29.99" or "Item total: $29.99" -> 29.99 */
    public static BigDecimal parse(String text) {
        int dollar = text.indexOf('$');
        if (dollar < 0) {
            throw new IllegalArgumentException("No price found in: " + text);
        }
        return new BigDecimal(text.substring(dollar + 1).trim());
    }
}
