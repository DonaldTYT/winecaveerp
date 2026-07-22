package com.kikyosoft.utils;

import java.text.Normalizer;

/**
 * Converts European accented text to a plain ASCII representation suitable
 * for legacy systems that cannot store Unicode characters.
 */
public final class EuropeanTextNormalizer {

    private EuropeanTextNormalizer() {
    }

    /**
     * Examples: Chateau, Societe, Strasse, AEther and Oresund.
     * Unsupported non-ASCII characters are omitted.
     *
     * @param text Unicode text; may be {@code null}
     * @return ASCII-only text, or {@code null} when the input is {@code null}
     */
    public static String toEnglishAscii(String text) {
        if (text == null) {
            return null;
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        StringBuilder ascii = new StringBuilder(normalized.length());

        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            int type = Character.getType(character);
            if (type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK) {
                continue;
            }

            if (character <= 0x7f) {
                ascii.append(character);
                continue;
            }

            appendSpecialCharacter(ascii, character);
        }

        return ascii.toString();
    }

    private static void appendSpecialCharacter(StringBuilder ascii, char character) {
        switch (character) {
            case '\u00c6': ascii.append("AE"); break; // AE ligature
            case '\u00e6': ascii.append("ae"); break;
            case '\u0152': ascii.append("OE"); break; // OE ligature
            case '\u0153': ascii.append("oe"); break;
            case '\u00d8': ascii.append('O'); break;
            case '\u00f8': ascii.append('o'); break;
            case '\u0141': ascii.append('L'); break;
            case '\u0142': ascii.append('l'); break;
            case '\u00d0': case '\u0110': ascii.append('D'); break;
            case '\u00f0': case '\u0111': ascii.append('d'); break;
            case '\u00de': ascii.append("Th"); break;
            case '\u00fe': ascii.append("th"); break;
            case '\u00df': ascii.append("ss"); break;
            case '\u0126': ascii.append('H'); break;
            case '\u0127': ascii.append('h'); break;
            case '\u0131': ascii.append('i'); break;
            case '\u014a': ascii.append('N'); break;
            case '\u014b': ascii.append('n'); break;
            case '\u0166': ascii.append('T'); break;
            case '\u0167': ascii.append('t'); break;
            case '\u00a0': ascii.append(' '); break; // non-breaking space
            case '\u2018': case '\u2019': case '\u201a': case '\u201b':
                ascii.append('\'');
                break;
            case '\u201c': case '\u201d': case '\u201e': case '\u201f':
                ascii.append('"');
                break;
            case '\u2010': case '\u2011': case '\u2012': case '\u2013': case '\u2014':
                ascii.append('-');
                break;
            case '\u2026': ascii.append("..."); break;
            default:
                // The caller requested ASCII-only output. Unknown Unicode
                // characters are therefore removed rather than changed to '?'.
                break;
        }
    }
}
