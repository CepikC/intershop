package kz.yandex.clientshop.service;

import kz.yandex.clientshop.client.api.PaymentsApi;
import kz.yandex.clientshop.client.domain.PaymentRequest;
import kz.yandex.clientshop.client.domain.PaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final PaymentsApi paymentsApi;

    public PaymentService(PaymentsApi paymentsApi) {
        this.paymentsApi = paymentsApi;
    }

    public Mono<Boolean> processOrderPayment(BigDecimal totalPrice) {
        String userId = String.valueOf(ThreadLocalRandom.current().nextInt(1, 4));
        PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(totalPrice);
        return paymentsApi.processPayment(paymentRequest)
                .map(this::isSuccessfulPayment)
                .onErrorReturn(false);
    }

    private Boolean isSuccessfulPayment(PaymentResponse payment) {
        return payment != null && PaymentResponse.StatusEnum.SUCCESS.equals(payment.getStatus());
    }
}
