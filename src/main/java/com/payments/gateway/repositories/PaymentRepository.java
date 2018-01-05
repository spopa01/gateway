package com.payments.gateway.repositories;

import com.payments.gateway.entities.Payment;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Spring will wire the right implementation for us
 * */

public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> { }