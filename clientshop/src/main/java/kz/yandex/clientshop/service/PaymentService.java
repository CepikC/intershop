package kz.yandex.clientshop.service;

import kz.yandex.clientshop.client.api.PaymentsApi;
import kz.yandex.clientshop.client.domain.BalanceResponse;
import kz.yandex.clientshop.client.domain.PaymentRequest;
import kz.yandex.clientshop.client.domain.PaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentsApi paymentsApi;

    public PaymentService(PaymentsApi paymentsApi) {
        this.paymentsApi = paymentsApi;
    }

    public Mono<Boolean> processOrderPayment(BigDecimal totalPrice) {
        final String userId = UUID.randomUUID().toString();
        return checkBalance(totalPrice, userId)
                .flatMap(balanceStatus -> processPayment(balanceStatus, totalPrice, userId));
    }

    private Mono<Boolean> checkBalance(BigDecimal totalPrice, String userId) {
        return paymentsApi.getBalance(userId)
                .map(balance -> isSufficientBalance(balance, totalPrice));
    }

    private Mono<Boolean> processPayment(Boolean balanceStatus, BigDecimal totalPrice, String userId) {
        if (!balanceStatus) {
            return Mono.just(false);
        }
        final PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(totalPrice);

        return paymentsApi.processPayment(paymentRequest)
                .map(this::isSuccessfulPayment);
    }

    private Boolean isSufficientBalance(BalanceResponse balance, BigDecimal totalPrice) {
        return balance != null && balance.getBalance().compareTo(totalPrice) >= 0;
    }

    private Boolean isSuccessfulPayment(PaymentResponse payment) {
        return payment != null && PaymentResponse.StatusEnum.SUCCESS.equals(payment.getStatus());
    }
}
