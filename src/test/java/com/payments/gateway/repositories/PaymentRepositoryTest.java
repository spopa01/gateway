package com.payments.gateway.repositories;

import com.payments.gateway.Common;
import com.payments.gateway.entities.Payment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Completely useless as we basically test Spring Data.
 * */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentRepositoryTest extends Common {
    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Not ideal to test for multiple things in one test, but here we go, we should be able to:
     * - create a payment, when id is null, repository will generate one, use the provided one when available
     * - update a payment by id
     * - delete a payment by id
     * - find payment by id
     * - find all payments
     * */

    @Test
    public void reactiveMongoDBRepositoryShouldWork() {
        //check that repository is empty
        assertThat(paymentRepository.count().block().intValue()).isEqualTo(NO_PMTS);

        //1a. create without a given id, repository will generate an id
        Payment payment1 = paymentRepository.save(payment(Common.AMT_1)).block();
        //1b. create with a given id, repository will not generate an id
        Payment payment2 = paymentRepository.save(payment(Common.TEST_MONGO_ID, Common.AMT_1)).block();

        //check an id is generated when the id is null
        assertThat(payment1.getId()).isNotNull();
        //check an id is not generated when the id is not null
        assertThat(payment2.getId()).isEqualTo(Common.TEST_MONGO_ID);
        //check that there are two payments
        assertThat(paymentRepository.count().block().intValue()).isEqualTo(Common.TWO_PMTS);
        //the rest of the details match
        assertThat(payment1).isEqualTo(payment(payment1.getId(), Common.AMT_1));
        assertThat(payment2).isEqualTo(payment(Common.TEST_MONGO_ID, Common.AMT_1));

        //2. update
        Payment payment3 = paymentRepository.save(payment(payment1.getId(), Common.AMT_2)).block();

        //updated payment has same id as the initially created payment
        assertThat(payment3.getId()).isEqualTo(payment1.getId());
        //check that there are still two payments
        assertThat(paymentRepository.count().block().intValue()).isEqualTo(Common.TWO_PMTS);
        //and the rest of the details match for the updated payment
        assertThat(payment3).isEqualTo(payment(payment3.getId(), Common.AMT_2));

        //3. find by id - and check that the details match (also the previous update did not update the other payments)
        assertThat(paymentRepository.findById(Common.TEST_MONGO_ID).block()).isEqualTo(payment(Common.TEST_MONGO_ID, Common.AMT_1));

        //4. find all - and check that the details match
        List<Payment> payments = paymentRepository.findAll().collect(Collectors.toList()).block();
        assertThat(payments).containsExactlyInAnyOrder(payment2, payment3);

        //5. delete by id
        paymentRepository.deleteById(payment1.getId()).block();
        //make sure the payment can't be found anymore
        assertThat(paymentRepository.findById(payment1.getId()).block()).isNull();
        //check that there is one payment left
        assertThat(paymentRepository.count().block().intValue()).isEqualTo(Common.ONE_PMT);
    }
}