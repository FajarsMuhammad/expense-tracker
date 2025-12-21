package com.fajars.expensetracker.payment.usecase.createpayment;


/**
 * @author : fajars
 * @since : 21/12/25
 */

public record CreatePaymentCmd(String idempotencyKey) {

}
