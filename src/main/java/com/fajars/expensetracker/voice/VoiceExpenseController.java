
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.parse.ParseVoiceExpenseResult;
import com.fajars.expensetracker.voice.parse.ParsingConfidence;
import com.fajars.expensetracker.voice.usecase.CreateTransactionFromVoiceUseCase;
import com.fajars.expensetracker.voice.usecase.ParserVoiceExpense;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voice-expense")
@RequiredArgsConstructor
public class VoiceExpenseController {

    private final ParserVoiceExpense parseUseCase;

    private final CreateTransactionFromVoiceUseCase saveUseCase;

    @PostMapping
    public ParseVoiceExpenseResult parse(@RequestBody String text) {
        UUID userId = UUID.randomUUID(); // stub

        ParseVoiceExpenseResult result = parseUseCase.execute(userId, text);

        if (result.confidence() == ParsingConfidence.HIGH) {
            saveUseCase.execute(userId, result, text);
        }

        return result;
    }
}
