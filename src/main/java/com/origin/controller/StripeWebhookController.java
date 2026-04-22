package com.origin.controller;

import com.origin.service.payment.StripeWebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Webhooks used by Stripe to manipulate payments")
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class StripeWebhookController {
    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        stripeWebhookService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
