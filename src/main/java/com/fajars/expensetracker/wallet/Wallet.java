package com.fajars.expensetracker.wallet;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.transaction.Transaction;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private Double initialBalance;
    private Date createdAt;
    private Date updatedAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
