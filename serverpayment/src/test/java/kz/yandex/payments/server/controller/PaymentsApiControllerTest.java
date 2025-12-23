package kz.yandex.payments.server.controller;

import kz.yandex.payments.server.api.PaymentService;
import kz.yandex.payments.server.domain.BalanceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentsApiController.class)
class PaymentsApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldReturnBalance() {
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(BigDecimal.valueOf(1000));

        when(paymentService.getBalance("user-1"))
                .thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/payments/balance/{userId}", "user-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(resp ->
                        assertThat(resp.getBalance())
                                .isEqualByComparingTo("1000")
                );
    }

}

