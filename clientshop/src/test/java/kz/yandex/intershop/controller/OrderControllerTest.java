package kz.yandex.intershop.controller;

import kz.yandex.clientshop.controller.OrderController;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.model.Order;
import kz.yandex.clientshop.service.CartService;
import kz.yandex.clientshop.service.OrderService;
import kz.yandex.clientshop.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderController controller;

    @Test
    void shouldReturnOrdersView() {
        Order order = new Order();

        when(orderService.getAllOrders()).thenReturn(Flux.just(order));

        StepVerifier.create(controller.listOrders(model))
                .expectNext("orders")
                .verifyComplete();

        verify(model).addAttribute("orders", List.of(order));
    }

    @Test
    void shouldReturnOrderView() {
        Order order = new Order();
        order.setId(5L);

        when(orderService.getOrderById(5L)).thenReturn(Mono.just(order));

        StepVerifier.create(controller.viewOrder(5L, true, model))
                .expectNext("order")
                .verifyComplete();

        verify(model).addAttribute("order", order);
        verify(model).addAttribute("newOrder", true);
    }

    @Test
    void shouldBuyAndRedirectToNewOrder() {
        Item item = new Item();
        item.setPrice(BigDecimal.TEN);
        item.setCount(1);

        Order newOrder = new Order();
        newOrder.setId(7L);

        when(cartService.getItems())
                .thenReturn(Flux.just(item));

        when(orderService.calculateTotalPrice(anyList()))
                .thenReturn(BigDecimal.TEN);

        when(paymentService.processOrderPayment(BigDecimal.TEN))
                .thenReturn(Mono.just(true));

        when(orderService.createOrderFromCart(anyList()))
                .thenReturn(Mono.just(newOrder));

        when(cartService.clear())
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.buy(model))
                .expectNext("redirect:/orders/7?newOrder=true")
                .verifyComplete();

        verify(paymentService).processOrderPayment(BigDecimal.TEN);
        verify(cartService).clear();
    }
}

