package com.fajars.expensetracker.dashboard;

import java.time.LocalDate;

public record WeeklyTrendDto(
    LocalDate date,
    Double income,
    Double expense
) {

}
