package com.fajars.expensetracker.dashboard;

import java.time.LocalDate;

public record WeeklyTrendResponse(
    LocalDate date,
    Double income,
    Double expense
) {

}
