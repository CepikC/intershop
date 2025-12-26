package kz.yandex.payments.server.api;

import kz.yandex.payments.server.domain.PaymentRequest;
import kz.yandex.payments.server.domain.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    private ConcurrentHashMap<String, BigDecimal> userBalances;

    @BeforeEach
    void setUp() throws Exception {
        Field field = PaymentService.class.getDeclaredField("userBalances");
        field.setAccessible(true);
        userBalances = (ConcurrentHashMap<String, BigDecimal>) field.get(paymentService);
    }

    @Test
    void getBalance_ShouldReturnExistingBalance_WhenUserExists() {
        String userId = "1";
        BigDecimal expectedBalance = new BigDecimal("1500000.00");

        StepVerifier.create(paymentService.getBalance(userId))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo(userId);
                    assertThat(result.getBalance()).isEqualTo(expectedBalance);
                })
                .verifyComplete();
    }

    @Test
    void getBalance_ShouldGenerateRandomBalance_WhenUserDoesNotExist() {
        String userId = "newUser";

        StepVerifier.create(paymentService.getBalance(userId))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo(userId);
                    assertThat(result.getBalance()).isNotNull();
                    assertThat(result.getBalance())
                            .isBetween(BigDecimal.ZERO, new BigDecimal("3000000.00"));
                })
                .verifyComplete();

        assertThat(userBalances).containsKey(userId);
    }

    @Test
    void processPayment_ShouldReturnSuccess_WhenSufficientBalance() {
        String userId = "1";
        BigDecimal paymentAmount = new BigDecimal("500000.00");
        BigDecimal expectedNewBalance = new BigDecimal("1000000.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo(userId);
                    assertThat(result.getAmount()).isEqualTo(paymentAmount);
                    assertThat(result.getRemainingBalance()).isEqualTo(expectedNewBalance);
                    assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
                    assertThat(result.getMessage()).isEqualTo("Платеж успешно обработан");
                })
                .verifyComplete();

        assertThat(userBalances.get(userId)).isEqualTo(expectedNewBalance);
    }

    @Test
    void processPayment_ShouldReturnFailed_WhenUserNotFound() {
        String userId = "nonExistentUser";
        BigDecimal paymentAmount = new BigDecimal("100000.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo(userId);
                    assertThat(result.getAmount()).isEqualTo(paymentAmount);
                    assertThat(result.getRemainingBalance()).isEqualTo(BigDecimal.ZERO);
                    assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.FAILED);
                    assertThat(result.getMessage())
                            .isEqualTo("Не найден пользователь по id " + userId);
                })
                .verifyComplete();

        assertThat(userBalances).doesNotContainKey(userId);
    }

    @Test
    void processPayment_ShouldHandleZeroAmount() {
        String userId = "1";
        BigDecimal paymentAmount = BigDecimal.ZERO;
        BigDecimal initialBalance = new BigDecimal("1500000.00");

        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(paymentAmount);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(result -> {
                    assertThat(result.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
                    assertThat(result.getRemainingBalance()).isEqualTo(initialBalance);
                })
                .verifyComplete();

        assertThat(userBalances.get(userId)).isEqualTo(initialBalance);
    }

    @Test
    void getBalance_ShouldGenerateDifferentBalances_ForDifferentNewUsers() {
        String userId1 = "newUser1";
        String userId2 = "newUser2";

        StepVerifier.create(paymentService.getBalance(userId1))
                .assertNext(result ->
                        assertThat(result.getBalance())
                                .isBetween(BigDecimal.ZERO, new BigDecimal("3000000.00"))
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance(userId2))
                .assertNext(result ->
                        assertThat(result.getBalance())
                                .isBetween(BigDecimal.ZERO, new BigDecimal("3000000.00"))
                )
                .verifyComplete();

        assertThat(userBalances).containsKeys(userId1, userId2);
    }
}
