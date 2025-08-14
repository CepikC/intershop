package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public String viewItem(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        int count = cartService.getItemCount(id);
        item.setCount(count);
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/{id}")
    public String changeItemCount(
            @PathVariable Long id,
            @RequestParam String action
    ) {
        cartService.changeItemCount(id, action);
        return "redirect:/items/" + id;
    }
}
