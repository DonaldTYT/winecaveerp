package com.kikyosoft.utils;

public class TextUtil {

    /**
     * Converts an integer to an uppercase hexadecimal string.
     * Equivalent to Integer.toHexString(p_i).toUpperCase().
     */
    public static String intToHEX(int p_i) {
        return Integer.toHexString(p_i).toUpperCase();
    }

    /**
     * Converts an integer to a lowercase hexadecimal string.
     */
    public static String intToHex(int p_i) {
        return Integer.toHexString(p_i);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java TextUtil <integer>");
            return;
        }

        try {
            int value = Integer.parseInt(args[0]);
            System.out.println("Hex: " + intToHex(value));
            System.out.println("HEX: " + intToHEX(value));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + args[0]);
        }
    }
}
