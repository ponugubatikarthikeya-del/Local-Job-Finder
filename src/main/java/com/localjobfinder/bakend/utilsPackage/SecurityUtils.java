package com.localjobfinder.bakend.utilsPackage;

import org.apache.commons.text.StringEscapeUtils;

public class SecurityUtils {

    // Sanitize user input to prevent XSS warnings
    public static String sanitize(String input) {
        if (input == null) return null;
        return StringEscapeUtils.escapeHtml4(input);
    }

}
