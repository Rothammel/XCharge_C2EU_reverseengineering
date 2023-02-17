package net.xcharge.sdk.server.coder.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListUtil {
    public static <T> List<T> copyIterator(Iterator<T> iter) {
        List<T> copy = new ArrayList<>();
        while (iter.hasNext()) {
            copy.add(iter.next());
        }
        return copy;
    }
}
