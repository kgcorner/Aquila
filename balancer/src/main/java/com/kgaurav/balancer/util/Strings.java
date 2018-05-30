package com.kgaurav.balancer.util;

/**
 * Contains Utility operation which can be performed on {@link String}
 * @author kgaurav
 */
public class Strings {
    /**
     * Check if the provided string is null or empty
     * @param s String to check
     * @return true if the string is empty or null, otherwise false
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() <1;
    }
}
