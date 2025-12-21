package com.fajars.expensetracker.dashboard.api;

import java.time.LocalDate;

public record WeeklyTrendResponse(
    LocalDate date,
    Double income,
    Double expense
) {

}
