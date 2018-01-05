package com.payments.gateway.entities;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Completely useless as we basically test Lombok.
 * */

public class PaymentTest {
    private static String id = "id";
    private static String from = "from";
    private static String to = "to";
    private static Integer amount = 1;

    @Test
    public void lombokShouldWork() {
        //we can only create a payment through the static factory method and use the getter methods
        Payment payment = Payment.of(id, from, to, amount);
        assertThat(payment.getId()).isEqualTo(id);
        assertThat(payment.getFrom()).isEqualTo(from);
        assertThat(payment.getTo()).isEqualTo(to);
        assertThat(payment.getAmount()).isEqualTo(amount);
    }
}