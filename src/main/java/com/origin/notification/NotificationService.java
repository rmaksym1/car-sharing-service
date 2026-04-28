package com.origin.notification;

import com.origin.model.Payment;
import com.origin.model.Rental;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface NotificationService {
    void sendRentalMessage(Rental rental);

    void sendCreatePaymentMessage(Payment payment);

    void sendOverdueMessage(Rental rental);

    void sendPaymentSucceededMessage(Payment payment);

    void sendPaymentCancelledMessage(Payment payment);

    void send(String chatId, String message, InlineKeyboardMarkup markup);
}
