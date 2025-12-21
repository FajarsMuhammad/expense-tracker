package com.fajars.expensetracker.transaction.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated transaction list with income and expense totals")
public record TransactionPageResponse(
    @Schema(description = "Total income amount for all filtered transactions (not just current page)", example = "76000.0")
    BigDecimal incomeTotal,

    @Schema(description = "Total expense amount for all filtered transactions (not just current page)",
        example = "76000.0"
    )
    BigDecimal expenseTotal,

    Page<TransactionResponse> transactions
) {

    public static TransactionPageResponse from(
        Page<TransactionResponse> transactions,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal
    ) {
        return new TransactionPageResponse(incomeTotal, expenseTotal, transactions);
    }

}
