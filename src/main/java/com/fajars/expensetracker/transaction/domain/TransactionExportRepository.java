package com.fajars.expensetracker.transaction.domain;

import com.fajars.expensetracker.transaction.api.TransactionType;
import com.fajars.expensetracker.transaction.projection.TransactionExportRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TransactionExportRepository extends Repository<Transaction, UUID> {

    @Query("""
        SELECT new com.fajars.expensetracker.transaction.projection.TransactionExportRow(
            t.id,
            t.date,
            t.amount,
            t.type,
            w.name,
            c.name,
            t.note
        )
        FROM Transaction t
        JOIN t.wallet w
        LEFT JOIN t.category c
        WHERE t.user.id = :userId
          AND (t.wallet.id = COALESCE(:walletId, t.wallet.id))
          AND (t.category.id = COALESCE(:categoryId, t.category.id))
          AND (t.type = COALESCE(:type, t.type))
          AND (t.date >= COALESCE(:fromDate, t.date))
          AND (t.date <= COALESCE(:toDate, t.date))
        ORDER BY t.date DESC
        """)
    List<TransactionExportRow> exportTransactions(
        UUID userId,
        UUID walletId,
        UUID categoryId,
        TransactionType type,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Pageable pageable
    );

}
