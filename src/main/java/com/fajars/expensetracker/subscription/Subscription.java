package com.fajars.expensetracker.subscription;

import com.fajars.expensetracker.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String provider;
    private String providerSubscriptionId;
    private String plan;
    private String status;
    private Date startedAt;
    private Date endedAt;
}
