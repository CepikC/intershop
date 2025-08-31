package kz.yandex.intershop.repository;

import kz.yandex.intershop.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndFindOrder() {
        Order order = new Order(new BigDecimal("250.50"));

        // Проверка сохранения
        StepVerifier.create(orderRepository.save(order))
                .assertNext(saved -> {
                    assert saved.getId() != null; // id сгенерирован
                    assert saved.getTotalSum().compareTo(new BigDecimal("250.50")) == 0;
                    assert saved.getItems().isEmpty(); // Transient поле не участвует в БД
                })
                .verifyComplete();

        // Проверка поиска
        StepVerifier.create(orderRepository.findAll())
                .expectNextMatches(o -> o.getTotalSum().compareTo(new BigDecimal("250.50")) == 0)
                .verifyComplete();
    }
}

