package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(Model model) {
        List<Item> items = cartService.getItems();
        BigDecimal total = cartService.getTotal();
        boolean empty = items.isEmpty();

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("empty", empty);

        return "cart";
    }

    @PostMapping("/{id}")
    public String changeCartItemCount(
            @PathVariable Long id,
            @RequestParam String action
    ) {
        cartService.changeItemCount(id, action);
        return "redirect:/cart/items";
    }
}
