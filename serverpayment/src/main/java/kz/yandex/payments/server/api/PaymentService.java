package kz.yandex.payments.server.api;

import kz.yandex.payments.server.domain.BalanceResponse;
import kz.yandex.payments.server.domain.PaymentRequest;
import kz.yandex.payments.server.domain.PaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentService {

    private final ConcurrentHashMap<String, BigDecimal> userBalances = new ConcurrentHashMap<>();

    public PaymentService() {
        userBalances.put("1", new BigDecimal("1500000.00"));
        userBalances.put("2", new BigDecimal("500000.00"));
        userBalances.put("3", new BigDecimal("2500000.00"));
    }

    /**
     * Получение баланса пользователя
     * Логика: если пользователь существует - возвращаем его баланс,
     * иначе генерируем случайный баланс от 0 до 3000
     */
    public Mono<BalanceResponse> getBalance(String userId) {
        return Mono.fromCallable(() -> {
            BigDecimal balance = userBalances.computeIfAbsent(userId,
                    k -> BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 3000000))
                            .setScale(2, BigDecimal.ROUND_HALF_UP));

            return new BalanceResponse(userId, balance);
        });
    }

    /**
     * Осуществление платежа
     * Проверяет достаточность средств и списывает сумму
     */
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return Mono.fromCallable(() -> {
            AtomicReference<PaymentResponse> responseRef = new AtomicReference<>();
            userBalances.compute(request.getUserId(), (userId, currentBalance) -> {
                if (currentBalance == null) {
                    responseRef.set(new PaymentResponse(
                            userId,
                            request.getAmount(),
                            BigDecimal.ZERO,
                            PaymentResponse.StatusEnum.FAILED,
                            "Не найден пользователь по id " + userId
                    ));
                    return null;
                }
                if (currentBalance.compareTo(request.getAmount()) < 0) {
                    responseRef.set(new PaymentResponse(
                            userId,
                            request.getAmount(),
                            currentBalance,
                            PaymentResponse.StatusEnum.FAILED,
                            "Недостаточно средств для платежа"
                    ));
                    return currentBalance;
                }
                BigDecimal newBalance = currentBalance.subtract(request.getAmount());
                responseRef.set(new PaymentResponse(
                        userId,
                        request.getAmount(),
                        newBalance,
                        PaymentResponse.StatusEnum.SUCCESS,
                        "Платеж успешно обработан"
                ));
                return newBalance;
            });
            return responseRef.get();
        });
    }
}
