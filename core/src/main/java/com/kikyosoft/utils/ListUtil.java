package com.kikyosoft.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {

    /**
     * Returns a mutable list from the given varargs.
     * Similar to List.of() but returns a modifiable list.
     */
    @SafeVarargs
    public static <V> List<V> of(V... values) {
        List<V> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    /**
     * Chop a list into multiple sublists based on a fixed sublist size.
     */
    public static <T> List<List<T>> chopByListSize(List<T> list, final int subListSize) {
        if (list == null || subListSize <= 0) return Collections.emptyList();

        List<List<T>> parts = new ArrayList<>();
        final int size = list.size();
        for (int i = 0; i < size; i += subListSize) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(size, i + subListSize))));
        }
        return parts;
    }

    /**
     * Chop a list into a fixed number of sublists.
     */
    public static <T> List<List<T>> chopByListCount(final List<T> list, final int subListCount) {
        if (list == null || subListCount <= 0) return Collections.emptyList();

        List<List<T>> parts = new ArrayList<>();
        final int totalSize = list.size();
        final int chunkSize = totalSize / subListCount;
        int remainder = totalSize % subListCount;
        int take;

        for (int i = 0; i < totalSize; i += take) {
            take = (remainder-- > 0) ? chunkSize + 1 : chunkSize;
            parts.add(new ArrayList<>(list.subList(i, Math.min(totalSize, i + take))));
        }

        return parts;
    }

    /**
     * Returns an empty list if the input iterable is null.
     */
    public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    /**
     * Alias for emptyIfNull.
     */
    public static <T> Iterable<T> safe(Iterable<T> iterable) {
        return emptyIfNull(iterable);
    }

    /**
     * Remove prefix from each string in the list if present.
     */
    public static List<String> stripPrefix(List<String> inputList, String prefix) {
        List<String> result = new ArrayList<>();
        if (inputList == null || prefix == null) return result;

        for (String str : inputList) {
            if (str != null && str.startsWith(prefix)) {
                result.add(str.substring(prefix.length()));
            } else {
                result.add(str);
            }
        }

        return result;
    }
}
