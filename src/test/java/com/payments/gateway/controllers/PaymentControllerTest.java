package com.payments.gateway.controllers;

import com.payments.gateway.Common;
import com.payments.gateway.entities.Payment;
import com.payments.gateway.repositories.PaymentRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the controller.
 * Basically an end to end test even using a real (embedded) mongodb.
 * */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest extends Common {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Before
    public void setUp() {
        //create some payments
        paymentRepository.saveAll(Common.payments(Common.AMT_1, Common.AMT_2)).collect(Collectors.toList()).block();
    }

    @After
    public void tearDown() {
        //clear them
        paymentRepository.deleteAll().block();
    }

    private Payment[] expectedPayments() {
        List<Payment> payments = paymentRepository.findAll().collect(Collectors.toList()).block();
        return payments.toArray(new Payment[payments.size()]);
    }

    @Test
    public void fetchAllPayments() {
        Payment[] expectedPayments = expectedPayments();

        webTestClient
                .get()
                .uri("payments")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Payment.class)
                    .hasSize(expectedPayments.length)
                    .contains(expectedPayments);
    }

    @Test
    public void fetchPaymentByIdWithNonAvailableId() {
        webTestClient
                .get()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, Common.TEST_NOT_MONGO_ID))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                    .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void fetchPaymentByIdWithAvailableId() {
        Payment expectedPayment = expectedPayments()[0];

        webTestClient
                .get()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, expectedPayment.getId()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(Payment.class)
                    .isEqualTo(expectedPayment);
    }

    @Test
    public void deletePaymentByIdWithNonAvailableId() {
        webTestClient
                .delete()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, Common.TEST_NOT_MONGO_ID))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                    .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(expectedPayments().length).isEqualTo(Common.TWO_PMTS);
    }

    @Test
    public void deletePaymentByIdWithAvailableId() {
        Payment expectedPayment = expectedPayments()[0];

        webTestClient
                .delete()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, expectedPayment.getId()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                    .isOk();

        assertThat(expectedPayments().length).isEqualTo(Common.ONE_PMT);
    }

    @Test
    public void createPaymentValidInput() {
        webTestClient
                .post()
                .uri("payments")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(payment(Common.AMT_3)), Payment.class)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.from").isEqualTo(FROM+Common.AMT_3)
                    .jsonPath("$.to").isEqualTo(TO+Common.AMT_3);

        assertThat(expectedPayments().length).isEqualTo(Common.THREE_PMTS);
    }

    @Test
    public void createPaymentUserProvidedIdNotUsed() {
        EntityExchangeResult<Payment> result = webTestClient
                .post()
                .uri("payments")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(payment(Common.TEST_MONGO_ID, Common.AMT_3)), Payment.class)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(Payment.class)
                .returnResult();

        assertThat(result.getResponseBody().getId()).isNotEqualTo(Common.TEST_MONGO_ID);

        assertThat(expectedPayments().length).isEqualTo(Common.THREE_PMTS);
    }

    @Test
    public void createPaymentWithInvalidInput() {
        webTestClient
                .post()
                .uri("payments")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody("some invalid json")
                .exchange()
                .expectStatus()
                    .is4xxClientError();

        assertThat(expectedPayments().length).isEqualTo(Common.TWO_PMTS);
    }

    @Test
    public void updatePaymentWithAvailableId() {
        Payment expectedPayment = expectedPayments()[0];

        webTestClient
                .put()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, expectedPayment.getId()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(payment(Common.AMT_3)), Payment.class)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(Payment.class)
                    .isEqualTo(payment(expectedPayment.getId(), Common.AMT_3));

        assertThat(expectedPayments().length).isEqualTo(Common.TWO_PMTS);
    }

    @Test
    public void updatePaymentWithNonAvailableId() {
        webTestClient
                .put()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, Common.TEST_NOT_MONGO_ID))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(payment(Common.AMT_3)), Payment.class)
                .exchange()
                .expectStatus()
                    .isNotFound();

        assertThat(expectedPayments().length).isEqualTo(Common.TWO_PMTS);
    }

    @Test
    public void updatePaymentUserProvidedIdNotUsed() {
        Payment expectedPayment = expectedPayments()[0];

        webTestClient
                .put()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, expectedPayment.getId()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(payment(Common.TEST_MONGO_ID, Common.AMT_3)), Payment.class)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(Payment.class)
                    .isEqualTo(payment(expectedPayment.getId(), Common.AMT_3));
    }

    @Test
    public void updatePaymentWithInvalidInput() {
        Payment expectedPayment = expectedPayments()[0];

        webTestClient
                .put()
                .uri("payments/{id}", Collections.singletonMap(Common.ID_FIELD, expectedPayment.getId()))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody("some invalid json")
                .exchange()
                .expectStatus()
                    .is4xxClientError();
    }
}