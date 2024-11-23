package org.example.comparator;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        int num1 = extractNumber(s1);
        int num2 = extractNumber(s2);
        return Integer.compare(num1, num2);
    }

    private int extractNumber(String str) {
        String numStr = str.replaceAll("\\D+", "");
        return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
    }
}

