package kz.yandex.clientshop.controller;

import kz.yandex.clientshop.service.CachedItemService;
import kz.yandex.clientshop.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final CachedItemService cachedItemService;
    private final CartService cartService;

    public ItemController(CachedItemService cachedItemService, CartService cartService) {
        this.cachedItemService = cachedItemService;
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public Mono<String> viewItem(@PathVariable Long id, Model model, Principal principal) {
        boolean isAuthenticated = principal != null;
        return cachedItemService.getItemById(id)
                .flatMap(item -> cartService.getItemCount(id)
                        .map(count -> {
                            item.setCount(count);
                            model.addAttribute("isAuthenticated", isAuthenticated);
                            model.addAttribute("item", item);
                            return "item";
                        }))
                .switchIfEmpty(Mono.just("error/404"));
    }

    @PostMapping("/plus/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changeItemCountPlus(@PathVariable Long id) {
        return cartService.changeItemCount(id, "plus")
                .thenReturn("redirect:/items/" + id);
    }

    @PostMapping("/minus/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> changeItemCountMinus(@PathVariable Long id) {
        return cartService.changeItemCount(id, "minus")
                .thenReturn("redirect:/items/" + id);
    }
}
