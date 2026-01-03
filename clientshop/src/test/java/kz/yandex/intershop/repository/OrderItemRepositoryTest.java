package kz.yandex.intershop.repository;

import kz.yandex.clientshop.IntershopApplication;
import kz.yandex.clientshop.model.OrderItem;
import kz.yandex.clientshop.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = IntershopApplication.class)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldSaveAndFindByOrderId() {
        OrderItem orderItem = new OrderItem(100L, 3, new BigDecimal("150.00"));
        orderItem.setOrderId(1L);

        // Сохраняем
        StepVerifier.create(orderItemRepository.save(orderItem))
                .assertNext(saved -> {
                    assert saved.getId() != null;
                    assert saved.getItemId().equals(100L);
                    assert saved.getCount() == 3;
                    assert saved.getPrice().compareTo(new BigDecimal("150.00")) == 0;
                    assert saved.getOrderId().equals(1L);
                    assert saved.getItem() == null; // Transient поле не сохраняется
                })
                .verifyComplete();

        // Проверяем выборку по orderId
        StepVerifier.create(orderItemRepository.findByOrderId(1L))
                .expectNextMatches(oi ->
                        oi.getItemId().equals(100L) &&
                                oi.getCount() == 3 &&
                                oi.getPrice().compareTo(new BigDecimal("150.00")) == 0 &&
                                oi.getOrderId().equals(1L)
                )
                .verifyComplete();
    }
}

