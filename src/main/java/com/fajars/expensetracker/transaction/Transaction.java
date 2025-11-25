package com.fajars.expensetracker.transaction;

import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.category.Category;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String type;
    private Double amount;
    private String note;
    private Date date;
    private Date createdAt;
    private Date updatedAt;
}
