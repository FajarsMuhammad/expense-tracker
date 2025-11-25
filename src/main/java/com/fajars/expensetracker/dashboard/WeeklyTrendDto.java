package com.fajars.expensetracker.dashboard;

import java.util.Date;

public record WeeklyTrendDto(
        Date date,
        Double income,
        Double expense
) {}
