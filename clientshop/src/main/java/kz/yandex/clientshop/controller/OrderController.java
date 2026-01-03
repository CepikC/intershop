package kz.yandex.clientshop.controller;

import kz.yandex.clientshop.service.CartService;
import kz.yandex.clientshop.service.OrderService;
import kz.yandex.clientshop.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, CartService cartService, PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> listOrders(Model model) {
        return orderService.getAllOrders()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
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
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> buy(Model model) {
        return cartService.getItems().collectList()
                .flatMap(cartItems -> {
                            BigDecimal totalPrice = orderService.calculateTotalPrice(cartItems);
                            return paymentService.processOrderPayment(totalPrice)
                                    .flatMap(paymentSuccess -> {
                                        if (!paymentSuccess) {
                                            model.addAttribute("error", "Недостаточно средств");
                                            return Mono.just("error");
                                        }
                                        return orderService.createOrderFromCart(cartItems)
                                                .flatMap(newOrder ->
                                                        cartService.clear()
                                                                .thenReturn("redirect:/orders/" + newOrder.getId() + "?newOrder=true")
                                                );
                                    });
                        });
    }
}

