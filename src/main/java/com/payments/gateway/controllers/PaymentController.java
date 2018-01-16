package com.payments.gateway.controllers;

import com.payments.gateway.entities.Payment;
import com.payments.gateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(value = "/payments")
public class PaymentController {
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping()
    public Flux<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @GetMapping(value = "{id}")
    public Mono<ResponseEntity<Payment>> getPaymentById(@PathVariable(value = "id") String id) {
        return paymentRepository.findById(id)
                .flatMap(
                        savedPayment -> Mono.just(new ResponseEntity<>(savedPayment, OK))
                ).defaultIfEmpty(
                        new ResponseEntity<>(NOT_FOUND)
                );
    }

    @DeleteMapping(value = "{id}")
    public Mono<ResponseEntity<Void>> deletePaymentById(@PathVariable(value = "id") String id) {
        return paymentRepository.findById(id)
                .flatMap(
                        savedPayment -> paymentRepository.deleteById(id).then(Mono.just(new ResponseEntity<Void>(OK)))
                ).defaultIfEmpty(
                        new ResponseEntity<>(NOT_FOUND)
                );
    }

    @PostMapping()
    @ResponseStatus(CREATED)
    public Mono<Payment> createPayment(@Valid @RequestBody Payment payment) {
        return paymentRepository.save(
                Payment.of(
                        null,
                        payment.getFrom(),
                        payment.getTo(),
                        payment.getAmount()
                )
        );
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Payment>> updatePaymentById(@PathVariable(value = "id") String id,
                                                           @Valid @RequestBody Payment payment) {
        return paymentRepository.findById(id)
                .flatMap(
                        savedPayment ->
                                paymentRepository.save(
                                        Payment.of(
                                                savedPayment.getId(),
                                                payment.getFrom(),
                                                payment.getTo(),
                                                payment.getAmount())
                                )
                ).map(
                        updatedPayment -> new ResponseEntity<>(updatedPayment, OK)
                ).defaultIfEmpty(
                        new ResponseEntity<>(NOT_FOUND)
                );
    }
}
