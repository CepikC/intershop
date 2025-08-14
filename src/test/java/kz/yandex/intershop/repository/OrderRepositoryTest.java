package kz.yandex.intershop.repository;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.model.Order;
import kz.yandex.intershop.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll(); // убираем данные от Liquibase или других тестов
    }

    @Test
    void testSaveAndFindById() {
        // given
        Item iphone = new Item();
        iphone.setTitle("Apple iPhone 15 Pro");
        iphone.setPrice(BigDecimal.valueOf(699_990));
        iphone.setDescription("Флагманский смартфон");
        itemRepository.save(iphone); // если нет cascade от OrderItem → Item

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(iphone);
        orderItem.setPrice(iphone.getPrice());
        orderItem.setCount(1);

        Order order = new Order();
        order.setItems(List.of(orderItem));
        order.setTotalSum(BigDecimal.valueOf(1_500));

        // when
        Order savedOrder = orderRepository.save(order);
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getItems())
                .hasSize(1)
                .extracting(oi -> oi.getItem().getTitle())
                .containsExactly("Apple iPhone 15 Pro");
        assertThat(foundOrder.get().getTotalSum()).isEqualByComparingTo("1500");
    }


}
