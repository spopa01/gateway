package com.payments.gateway.entities;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data(staticConstructor = "of")
public class Payment {
    @Id private final String id;
    private final String from;
    private final String to;
    private final Integer amount;
}
