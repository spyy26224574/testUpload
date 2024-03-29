package com.filepicker.utils;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by droidNinja on 29/07/16.
 */
public class Utils {

    public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element: target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }
}
