package com.fajars.expensetracker.debt;

import com.fajars.expensetracker.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "debts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Debt {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String counterpartyName;
    private Double totalAmount;
    private Double remainingAmount;
    private Date dueDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    @OneToMany(mappedBy = "debt")
    private List<DebtPayment> payments;
}
