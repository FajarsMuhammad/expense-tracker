package com.fajars.expensetracker.wallet;

public enum Currency {
    IDR("Indonesian Rupiah", "Rp"),
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    SGD("Singapore Dollar", "S$"),
    MYR("Malaysian Ringgit", "RM");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
