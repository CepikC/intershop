package kz.yandex.clientshop.controller;

import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.service.CachedItemService;
import kz.yandex.clientshop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/main/items")
public class MainController {

    private final CachedItemService itemService;
    private final CartService cartService;

    public MainController(CachedItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> main(@RequestParam(defaultValue = "") String search,
                             @RequestParam(defaultValue = "NO") String sort,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam(defaultValue = "1") int pageNumber,
                             Model model,
                             WebSession session) {
        return itemService.findAll(search, pageNumber, pageSize, sort)
                .flatMap(page ->
                        cartService.getItems(session).collectList()
                                .map(cartItems -> {
                                    List<List<Item>> rows = new ArrayList<>();

                                    for (Item it : page.getContent()) {
                                        cartItems.stream()
                                                .filter(ci -> ci.getId().equals(it.getId()))
                                                .findFirst()
                                                .ifPresent(ci -> it.setCount(ci.getCount()));

                                        rows.add(List.of(it));
                                    }

                                    model.addAttribute("items", rows);
                                    model.addAttribute("search", search);
                                    model.addAttribute("sort", sort);
                                    model.addAttribute("paging", new Object() {
                                        public int pageNumber() { return pageNumber; }
                                        public int pageSize() { return pageSize; }
                                        public boolean hasNext() { return page.hasNext(); }
                                        public boolean hasPrevious() { return page.hasPrevious(); }
                                    });

                                    return "main";
                                })
                );
    }

    @PostMapping("/plus/{id}")
    public Mono<String> changeItemCountPlus(@PathVariable Long id,
                                        WebSession session) {
        return cartService.changeItemCount(session, id, "plus")
                .thenReturn("redirect:/main/items");
    }

    @PostMapping("/minus/{id}")
    public Mono<String> changeItemCountMinus(@PathVariable Long id,
                                        WebSession session) {
        return cartService.changeItemCount(session, id, "minus")
                .thenReturn("redirect:/main/items");
    }
}


