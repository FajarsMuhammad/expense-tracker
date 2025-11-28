package com.fajars.expensetracker.category;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.transaction.Transaction;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // null for default categories

    private String name;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    private Date createdAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    // Helper method to check if this is a default category
    public boolean isDefault() {
        return user == null;
    }
}
