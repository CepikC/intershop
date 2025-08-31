package kz.yandex.intershop.controller;

import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/orders")
    public Mono<String> listOrders(Model model) {
        return orderService.getAllOrders()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> viewOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    ) {
        return orderService.getOrderById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                });
    }

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        return cartService.getItems(session).collectList()
                .flatMap(cartItems -> orderService.createOrderFromCart(cartItems))
                .flatMap(newOrder -> cartService.clear(session).thenReturn(newOrder))
                .map(newOrder -> "redirect:/orders/" + newOrder.getId() + "?newOrder=true");
    }
}

