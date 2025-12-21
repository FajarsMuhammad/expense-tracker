package com.fajars.expensetracker.transaction.domain;

import com.fajars.expensetracker.transaction.api.TransactionType;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.category.domain.Category;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user_date", columnList = "user_id, date"),
    @Index(name = "idx_transaction_wallet", columnList = "wallet_id"),
    @Index(name = "idx_transaction_category", columnList = "category_id"),
    @Index(name = "idx_transaction_type", columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Double amount;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
