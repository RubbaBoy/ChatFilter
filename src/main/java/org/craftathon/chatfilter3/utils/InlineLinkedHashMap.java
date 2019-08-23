package org.craftathon.chatfilter3.utils;

import java.util.LinkedHashMap;

public class InlineLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private boolean runWithChecks = false;

    private Class key = null;
    private Class value = null;

    public InlineLinkedHashMap(Object... assignments) {
        initialize(assignments);
    }

    private int i2 = 0;
    public LinkedHashMap<K, V> initialize(Object[] assignments) {
        if (assignments.length % 2 != 0) {
            System.err.println("Error: assignments.length % 2 = " + (assignments.length % 2));
            return this;
        }

        Object k = null;
        Object v;
        for (Object obj : assignments) {
            if (i2 == 0) {
                i2++;
                if (runWithChecks) {
                    if (!obj.getClass().getCanonicalName().equals(key.getCanonicalName())) {
                        System.err.println("An error occurred! " + obj.getClass() + " is not equal to: " + key);
                        return this;
                    }
                }
                k = obj;
            } else if (i2 == 1) {
                i2 = 0;
                if (runWithChecks) {
                    if (!obj.getClass().getCanonicalName().equals(value.getCanonicalName())) {
                        System.err.println("An error occurred! " + obj.getClass() + " is not equal to: " + value);
                        return this;
                    }
                }
                v = obj;

                put((K) k, (V) v);
            }
        }

        return this;
    }

    public InlineLinkedHashMap(Class key, Class value) {
        this.key = key;
        this.value = value;

        this.runWithChecks = true;
    }
}