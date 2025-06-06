package org.tsicoop.framework;

import java.util.Arrays;

public class StringUtil {

    public static String arrayToString(String[] arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        return Arrays.stream(arr).reduce((a, b) -> a + " " + b).orElse("");
    }

    public static String formatSearchQuery(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String withoutAmpersands = input.replace("&", ""); // Remove all ampersands
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < withoutAmpersands.length(); i++) {
            char currentChar = withoutAmpersands.charAt(i);
            if (currentChar == ' ') {
                result.append('&');
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }
}
