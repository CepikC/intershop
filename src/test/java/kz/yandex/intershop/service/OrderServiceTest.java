package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.model.Order;
import kz.yandex.intershop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrders_shouldReturnList() {
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).containsExactly(order);
        verify(orderRepository).findAll();
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void getOrderById_whenExists_shouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_whenNotExists_shouldThrowException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> orderService.getOrderById(99L));

        verify(orderRepository).findById(99L);
    }

    @Test
    void createOrderFromCart_shouldSaveOrderWithItems() {
        // given
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Item1");
        item1.setPrice(BigDecimal.valueOf(100));
        item1.setCount(2);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Item2");
        item2.setPrice(BigDecimal.valueOf(50));
        item2.setCount(1);

        Order savedOrder = new Order();
        savedOrder.setId(10L);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // when
        Order result = orderService.createOrderFromCart(List.of(item1, item2));

        // then
        assertThat(result).isEqualTo(savedOrder);
        verify(orderRepository).save(any(Order.class));
    }
}

