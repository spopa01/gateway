package com.payments.gateway;

import com.payments.gateway.entities.Payment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Common {
    protected static Payment payment(Integer amount) {
        return payment(null, amount);
    }

    protected static String FROM = "from";
    protected static String TO = "to";

    protected static Payment payment(String id, Integer amount) {
        return Payment.of(id, FROM+amount, TO+amount, amount);
    }

    protected static List<Payment> payments(Integer... amounts) {
        return Arrays.stream(amounts).map(Common::payment).collect(Collectors.toList());
    }

    protected static int AMT_1 = 1;//amount of value 1
    protected static int AMT_2 = 2;//amount of value 2
    protected static int AMT_3 = 3;//amount of value 3

    protected static int NO_PMTS = 0;   //no payments
    protected static int ONE_PMT = 1;   //one payment
    protected static int TWO_PMTS = 2;  //two payments
    protected static int THREE_PMTS = 3;//two payments

    protected static String ID_FIELD = "id";

    protected static String TEST_MONGO_ID     = "5a40c71feaa1239082b3b4d8";
    protected static String TEST_NOT_MONGO_ID = "xxxxxxxxxxxxxxxxxxxxxxxx";
}
