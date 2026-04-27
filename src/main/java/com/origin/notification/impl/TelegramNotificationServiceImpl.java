package com.origin.notification.impl;

import com.origin.model.Car;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.notification.NotificationService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationServiceImpl implements NotificationService {
    private static final String TELEGRAM_API = "https://api.telegram.org/bot%s/sendMessage";

    @Value("${telegram.bot.api}")
    private String token;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendRentalMessage(Rental rental) {
        Car car = rental.getCar();
        User user = rental.getUser();

        String msg = String.format("""
                        🚗 New Rental
                        
                        Car: %s %s
                        User: %s
                        Period:
                        %s → %s""",
                car.getBrand(),
                car.getModel(),
                user.getEmail(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );

        sendMessage(msg);
    }

    public void sendCreatePaymentMessage(Payment payment) {
        Rental rental = payment.getRental();
        Car car = rental.getCar();
        User user = rental.getUser();

        String msg = String.format("""
                        💵 New Payment
                        
                        Amount: %s USD
                        User: %s
                        Car: %s %s
                        
                        Period:
                        %s → %s
                        
                        Pay here:
                        %s""",
                payment.getAmountToPay(),
                user.getEmail(),
                car.getBrand(),
                car.getModel(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                payment.getSessionUrl()
        );

        sendMessage(msg);
    }

    public void sendOverdueMessage(Rental rental) {
        Car car = rental.getCar();
        User user = rental.getUser();

        long overdueDays = ChronoUnit.DAYS.between(
                rental.getReturnDate(),
                LocalDate.now()
        );

        String msg = String.format("""
                        ⚠️ Overdue Rental!
                        
                        User: %s
                        Car: %s %s
                        
                        Return date: %s
                        Overdue: %d days
                        
                        Please return the car ASAP 🚗""",
                user.getEmail(),
                car.getBrand(),
                car.getModel(),
                rental.getReturnDate(),
                overdueDays
        );

        sendMessage(msg);
    }

    public void sendMessage(String message) {
        String url = TELEGRAM_API.formatted(token);

        Map<String, String> body = Map.of(
                "chat_id", chatId,
                "text", message
        );

        restTemplate.postForObject(url, body, String.class);
    }
}
