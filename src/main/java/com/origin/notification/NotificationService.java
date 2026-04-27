package com.origin.notification;

import com.origin.model.Payment;
import com.origin.model.Rental;

public interface NotificationService {
    void sendRentalMessage(Rental rental);

    void sendCreatePaymentMessage(Payment payment);

    void sendOverdueMessage(Rental rental);
}
