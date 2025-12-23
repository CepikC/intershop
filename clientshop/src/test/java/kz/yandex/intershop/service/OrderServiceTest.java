package kz.yandex.intershop.service;

import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.model.Order;
import kz.yandex.clientshop.model.OrderItem;
import kz.yandex.clientshop.repository.ItemRepository;
import kz.yandex.clientshop.repository.OrderItemRepository;
import kz.yandex.clientshop.repository.OrderRepository;
import kz.yandex.clientshop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    private Item testItem;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Test Item");
        testItem.setPrice(BigDecimal.valueOf(100));
        testItem.setCount(2);

        testOrder = new Order();
        testOrder.setId(10L);

        testOrderItem = new OrderItem();
        testOrderItem.setId(100L);
        testOrderItem.setItemId(1L);
        testOrderItem.setOrderId(10L);
        testOrderItem.setCount(2);
        testOrderItem.setPrice(BigDecimal.valueOf(100));
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWithItems() {
        when(orderRepository.findAll()).thenReturn(Flux.just(testOrder));
        when(orderItemRepository.findByOrderId(10L)).thenReturn(Flux.just(testOrderItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(orderService.getAllOrders())
                .expectNextMatches(order ->
                        order.getId().equals(10L) &&
                                order.getItems().size() == 1 &&
                                order.getItems().get(0).getItem().getTitle().equals("Test Item"))
                .verifyComplete();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() {
        when(orderRepository.findById(10L)).thenReturn(Mono.just(testOrder));
        when(orderItemRepository.findByOrderId(10L)).thenReturn(Flux.just(testOrderItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(orderService.getOrderById(10L))
                .expectNextMatches(order ->
                        order.getId().equals(10L) &&
                                order.getItems().get(0).getItem().getPrice().equals(BigDecimal.valueOf(100)))
                .verifyComplete();
    }

    @Test
    void getOrderById_ShouldReturnError_WhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderById(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Order not found"))
                .verify();
    }
    @Test
    void createOrderFromCart_ShouldSaveOrderAndItems() {
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));
        when(orderItemRepository.saveAll((Iterable<OrderItem>) any()))
                .thenReturn(Flux.just(testOrderItem));

        StepVerifier.create(orderService.createOrderFromCart(List.of(testItem)))
                .expectNextMatches(order ->
                        order.getId().equals(10L) &&
                                order.getItems().size() == 1 &&
                                order.getTotalSum().equals(BigDecimal.valueOf(200)))
                .verifyComplete();
    }
}

