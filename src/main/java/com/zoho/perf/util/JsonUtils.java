package com.zoho.perf.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

/**
 * Utility class for JSON operations including validation and escaping.
 */
public class JsonUtils {

    /**
     * Validates if a string is valid JSON.
     *
     * @param json the JSON string to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            new JSONTokener(json).nextValue();
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Escapes special characters in a string for JSON encoding.
     * Handles: quotes, backslashes, newlines, carriage returns, tabs, and control
     * characters.
     *
     * @param value the string to escape
     * @return escaped string safe for JSON
     */
    public static String escapeJson(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder(value.length() + 20);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    // Handle control characters
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            sb.append('0');
                        }
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }

        return sb.toString();
    }

    /**
     * Parses a JSON array string and returns the number of elements.
     *
     * @param json the JSON array string
     * @return number of elements in the array
     * @throws JSONException if the string is not a valid JSON array
     */
    public static int getArrayLength(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        return jsonArray.length();
    }
}
