package com.fajars.expensetracker.user.domain;

import com.fajars.expensetracker.debt.domain.Debt;
import com.fajars.expensetracker.subscription.domain.Subscription;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.category.domain.Category;
import com.fajars.expensetracker.transaction.domain.Transaction;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String passwordHash;

    @NotBlank
    private String name;

    private String locale;
    private Date createdAt;
    private Date updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Wallet> wallets;

    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    private List<Debt> debts;

    @OneToMany(mappedBy = "user")
    private List<Subscription> subscriptions;
}
