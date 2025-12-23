package kz.yandex.clientshop.controller;

import kz.yandex.clientshop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> viewCart(Model model, WebSession session) {
        return cartService.getItems(session).collectList()
                .flatMap(items ->
                        cartService.getTotal(session)
                                .map(total -> {
                                    model.addAttribute("items", items);
                                    model.addAttribute("total", total);
                                    model.addAttribute("empty", items.isEmpty());
                                    return "cart";
                                })
                );
    }

    @PostMapping("/plus/{id}")
    public Mono<String> changeCartItemCountPlus(
            @PathVariable Long id,
            WebSession session
    ) {
        return cartService.changeItemCount(session, id, "plus")
                .thenReturn("redirect:/cart/items");
    }
    @PostMapping("/minus/{id}")
    public Mono<String> changeCartItemCountMinus(
            @PathVariable Long id,
            WebSession session
    ) {
        return cartService.changeItemCount(session, id, "minus")
                .thenReturn("redirect:/cart/items");
    }
    @PostMapping("/delete/{id}")
    public Mono<String> changeCartItemDelete(
            @PathVariable Long id,
            WebSession session
    ) {
        return cartService.changeItemCount(session, id, "delete")
                .thenReturn("redirect:/cart/items");
    }
}
