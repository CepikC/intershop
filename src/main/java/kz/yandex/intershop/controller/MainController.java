package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/main/items")
public class MainController {

    private final ItemService itemService;
    private final CartService cartService;

    public MainController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping
    public String main(@RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "NO") String sort,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(defaultValue = "1") int pageNumber,
                       Model model) {
        var page = itemService.findAll(search, pageNumber, pageSize, sort);

        var cartItems = cartService.getItems();

        java.util.List<java.util.List<Item>> rows = new java.util.ArrayList<>();

        for (Item it : page.getContent()) {
            cartItems.stream()
                    .filter(ci -> ci.getId().equals(it.getId()))
                    .findFirst()
                    .ifPresent(ci -> it.setCount(ci.getCount()));

            rows.add(java.util.List.of(it));
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
    }

    @PostMapping("/{id}")
    public String changeItemCount(
            @PathVariable Long id,
            @RequestParam String action
    ) {
        cartService.changeItemCount(id, action);
        return "redirect:/main/items";
    }
}

