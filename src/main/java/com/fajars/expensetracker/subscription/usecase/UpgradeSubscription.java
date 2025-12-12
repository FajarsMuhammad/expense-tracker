package com.fajars.expensetracker.subscription.usecase;


import com.fajars.expensetracker.subscription.UpgradeInfoResponse;
import java.util.UUID;

/**
 * @author : fajars
 * @since : 06/12/25
 */

public interface UpgradeSubscription {

    UpgradeInfoResponse upgrade(UUID userId);

}
