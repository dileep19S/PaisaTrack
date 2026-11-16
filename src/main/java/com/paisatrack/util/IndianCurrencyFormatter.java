package com.paisatrack.util;

import com.paisatrack.entity.CategoryType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * Formats monetary BigDecimal values using Indian Rupee conventions (₹1,25,000.00)
 * and dates using DD-MM-YYYY format.
 * Exposed as Spring bean "inr" for direct use in Thymeleaf templates.
 */
@Component("inr")
public class IndianCurrencyFormatter {

    private static final DateTimeFormatter INDIAN_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public String format(BigDecimal amount) {
        BigDecimal safe = (amount == null) ? BigDecimal.ZERO : amount;
        boolean negative = safe.signum() < 0;
        BigDecimal abs = safe.abs().setScale(2, RoundingMode.HALF_UP);

        String plain = abs.toPlainString();
        int dotIndex = plain.indexOf('.');
        String intPart = plain.substring(0, dotIndex);
        String decPart = plain.substring(dotIndex + 1);

        String groupedInt = formatIndianInteger(intPart);
        return (negative ? "-₹" : "₹") + groupedInt + "." + decPart;
    }

    public String formatSigned(BigDecimal amount, CategoryType type) {
        String prefix = (type == CategoryType.INCOME) ? "+" : "-";
        BigDecimal safe = (amount == null) ? BigDecimal.ZERO : amount.abs();
        return prefix + format(safe);
    }

    public String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(INDIAN_DATE_FORMAT);
    }

    private String formatIndianInteger(String digits) {
        int len = digits.length();
        if (len <= 3) {
            return digits;
        }
        String lastThree = digits.substring(len - 3);
        String remaining = digits.substring(0, len - 3);

        StringBuilder sb = new StringBuilder();
        int remLen = remaining.length();
        for (int i = 0; i < remLen; i++) {
            sb.append(remaining.charAt(i));
            int posFromRight = remLen - 1 - i;
            if (posFromRight > 0 && posFromRight % 2 == 0) {
                sb.append(',');
            }
        }
        sb.append(',').append(lastThree);
        return sb.toString();
    }
}
