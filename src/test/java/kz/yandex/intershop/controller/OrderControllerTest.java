package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.model.Order;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private OrderController controller;

    @Test
    void shouldReturnOrdersView() {
        when(orderService.getAllOrders()).thenReturn(List.of(new Order()));

        String view = controller.listOrders(model);

        assertThat(view).isEqualTo("orders");
        verify(model).addAttribute(eq("orders"), any());
    }

    @Test
    void shouldReturnOrderView() {
        Order order = new Order();
        when(orderService.getOrderById(5L)).thenReturn(order);

        String view = controller.viewOrder(5L, true, model);

        assertThat(view).isEqualTo("order");
        verify(model).addAttribute("order", order);
        verify(model).addAttribute("newOrder", true);
    }

    @Test
    void shouldBuyAndRedirectToNewOrder() {
        Order order = new Order();
        order.setId(7L);
        when(cartService.getItems()).thenReturn(List.of(new Item()));
        when(orderService.createOrderFromCart(any())).thenReturn(order);

        String view = controller.buy();

        assertThat(view).isEqualTo("redirect:/orders/7?newOrder=true");
        verify(cartService).clear();
    }
}
