package kz.yandex.intershop.service;

import kz.yandex.clientshop.config.security.SecurityUtils;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.model.Order;
import kz.yandex.clientshop.model.OrderItem;
import kz.yandex.clientshop.model.User;
import kz.yandex.clientshop.repository.ItemRepository;
import kz.yandex.clientshop.repository.OrderItemRepository;
import kz.yandex.clientshop.repository.OrderRepository;
import kz.yandex.clientshop.repository.UserRepository;
import kz.yandex.clientshop.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private Item testItem;
    private Order testOrder;
    private OrderItem testOrderItem;
    private User testUser;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::currentUsername)
                .thenReturn(Mono.just("user1"));

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("user1");

        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Test Item");
        testItem.setPrice(BigDecimal.valueOf(100));
        testItem.setCount(2);

        testOrder = new Order();
        testOrder.setId(10L);
        testOrder.setUserId(1L);

        testOrderItem = new OrderItem();
        testOrderItem.setId(100L);
        testOrderItem.setItemId(1L);
        testOrderItem.setOrderId(10L);
        testOrderItem.setCount(2);
        testOrderItem.setPrice(BigDecimal.valueOf(100));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void getAllOrders_ShouldReturnOrdersWithItems() {
        when(userRepository.findByUsername("user1"))
                .thenReturn(Mono.just(testUser));
        when(orderRepository.findAllByUserId(1L))
                .thenReturn(Flux.just(testOrder));
        when(orderItemRepository.findByOrderId(10L))
                .thenReturn(Flux.just(testOrderItem));
        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(testItem));

        StepVerifier.create(orderService.getAllOrders())
                .expectNextMatches(order ->
                        order.getItems().size() == 1 &&
                                order.getItems().get(0).getItem().getTitle().equals("Test Item")
                )
                .verifyComplete();
    }

    @Test
    void createOrderFromCart_ShouldSaveOrderAndItems() {
        when(userRepository.findByUsername("user1"))
                .thenReturn(Mono.just(testUser));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));
        when(orderItemRepository.saveAll(any(Iterable.class)))
                .thenReturn(Flux.just(testOrderItem));

        StepVerifier.create(orderService.createOrderFromCart(List.of(testItem)))
                .expectNextMatches(order ->
                        order.getTotalSum().equals(BigDecimal.valueOf(200)) &&
                                order.getItems().size() == 1
                )
                .verifyComplete();
    }
}


