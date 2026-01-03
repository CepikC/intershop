package kz.yandex.clientshop.controller;

import kz.yandex.clientshop.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> viewCart(Model model) {
        return cartService.getItems().collectList()
                .flatMap(items ->
                        cartService.getTotal()
                                .map(total -> {
                                    model.addAttribute("items", items);
                                    model.addAttribute("total", total);
                                    model.addAttribute("empty", items.isEmpty());
                                    return "cart";
                                })
                );
    }

    @PostMapping("/plus/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changeCartItemCountPlus(
            @PathVariable Long id
    ) {
        return cartService.changeItemCount(id, "plus")
                .thenReturn("redirect:/cart/items");
    }
    @PostMapping("/minus/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changeCartItemCountMinus(
            @PathVariable Long id
    ) {
        return cartService.changeItemCount(id, "minus")
                .thenReturn("redirect:/cart/items");
    }
    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changeCartItemDelete(
            @PathVariable Long id
    ) {
        return cartService.changeItemCount(id, "delete")
                .thenReturn("redirect:/cart/items");
    }
}
