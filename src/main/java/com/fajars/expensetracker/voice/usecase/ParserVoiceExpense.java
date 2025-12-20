
package com.fajars.expensetracker.voice.usecase;

import com.fajars.expensetracker.voice.parse.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParserVoiceExpense {

    private final AmountParser amountParser = new AmountParser();

    public ParseVoiceExpenseResult execute(UUID userId, String text) {
        Integer amount = amountParser.parse(text);

        if (amount == null) {
            return new ParseVoiceExpenseResult(
                null, null, null, ParsingConfidence.FAILED, LocalDateTime.now()
            );
        }

        return new ParseVoiceExpenseResult(
            amount.doubleValue(), null, null, ParsingConfidence.MEDIUM, LocalDateTime.now()
        );
    }
}
