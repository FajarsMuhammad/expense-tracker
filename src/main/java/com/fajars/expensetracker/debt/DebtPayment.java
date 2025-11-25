package com.fajars.expensetracker.debt;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "debt_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtPayment {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "debt_id")
    private Debt debt;

    private Double amount;
    private Date paidAt;
    private String note;
}
