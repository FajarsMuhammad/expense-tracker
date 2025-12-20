
package com.fajars.expensetracker.voice.parse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParseVoiceExpenseResult(
    Double amount,
    UUID categoryId,
    UUID walletId,
    ParsingConfidence confidence,
    LocalDateTime date
) {

}
