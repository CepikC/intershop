package kz.yandex.intershop.controller;

import kz.yandex.intershop.service.CachedItemService;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

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
    public Mono<String> viewItem(@PathVariable Long id, Model model, WebSession session) {
        return cachedItemService.getItemById(id)
                .flatMap(item -> cartService.getItemCount(session, id)
                        .map(count -> {
                            item.setCount(count);
                            model.addAttribute("item", item);
                            return "item";
                        }))
                .switchIfEmpty(Mono.just("error/404"));
    }

    @PostMapping("/plus/{id}")
    public Mono<String> changeItemCountPlus(@PathVariable Long id,
                                        WebSession session) {
        return cartService.changeItemCount(session, id, "plus")
                .thenReturn("redirect:/items/" + id);
    }

    @PostMapping("/minus/{id}")
    public Mono<String> changeItemCountMinus(@PathVariable Long id,
                                        WebSession session) {
        return cartService.changeItemCount(session, id, "minus")
                .thenReturn("redirect:/items/" + id);
    }
}
