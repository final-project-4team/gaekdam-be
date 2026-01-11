package com.gaekdam.gaekdambe.global.crypto;


public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String maskName(String name) {
        if (name == null || name.isBlank())
            return "***";
        int length = name.length();
        if (length == 1)
            return name;
        if (length == 2)
            return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(length - 2) + name.charAt(length - 1);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank())
            return "***-****-****";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11)
            return digits.substring(0, 3) + "-****-" + digits.substring(7);
        if (digits.length() == 10)
            return digits.substring(0, 2) + "-****-" + digits.substring(6);
        return "***-****-****";
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank())
            return "***@***.***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 0)
            return "***@***.***";
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() <= 3)
            return localPart.charAt(0) + "***" + domain;
        return localPart.substring(0, 3) + "***" + domain;
    }
}
