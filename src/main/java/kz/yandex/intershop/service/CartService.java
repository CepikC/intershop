package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SessionScope
public class CartService {

    private final ItemRepository itemRepository;
    private final Map<Long, Integer> cartItems = new HashMap<>();

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getItems() {
        return cartItems.keySet().stream()
                .map(itemRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(item -> item.setCount(cartItems.get(item.getId())))
                .collect(Collectors.toList());
    }

    public int getItemCount(Long itemId) {
        return cartItems.getOrDefault(itemId, 0);
    }


    public BigDecimal getTotal() {
        return getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void changeItemCount(Long itemId, String action) {
        int count = cartItems.getOrDefault(itemId, 0);

        switch (action) {
            case "plus" -> cartItems.put(itemId, count + 1);
            case "minus" -> {
                if (count > 1) cartItems.put(itemId, count - 1);
                else cartItems.remove(itemId);
            }
            case "delete" -> cartItems.remove(itemId);
        }
    }

    public void clear() {
        cartItems.clear();
    }
}

