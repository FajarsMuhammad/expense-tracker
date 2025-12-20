
package com.fajars.expensetracker.voice.parse;

import java.util.Map;

public class AmountParser {

    private static final Map<String, Integer> ONES = Map.of(
        "satu", 1,
        "dua", 2,
        "tiga", 3,
        "empat", 4,
        "lima", 5,
        "enam", 6,
        "tujuh", 7,
        "delapan", 8,
        "sembilan", 9
    );

    public Integer parse(String text) {
        text = text.toLowerCase();

        if (text.matches(".*\\d{3,}.*")) {
            return Integer.parseInt(text.replaceAll("\\D", ""));
        }

        if (!text.contains("ribu")) {
            return null;
        }

        int base = 0;
        for (var e : ONES.entrySet()) {
            if (text.contains(e.getKey())) {
                base = e.getValue();
            }
        }
        if (text.contains("puluh")) {
            base *= 10;
        }

        return base > 0 ? base * 1000 : null;
    }
}
