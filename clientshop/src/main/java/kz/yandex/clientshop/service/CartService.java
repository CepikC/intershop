package kz.yandex.clientshop.service;

import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_KEY = "cartItems";

    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(WebSession session) {
        return (Map<Long, Integer>) session.getAttributes()
                .computeIfAbsent(CART_KEY, k -> new HashMap<Long, Integer>());
    }

    public Flux<Item> getItems(WebSession session) {
        return Flux.fromIterable(getCart(session).entrySet())
                .flatMap(entry ->
                        itemRepository.findById(entry.getKey())
                                .map(item -> {
                                    item.setCount(entry.getValue());
                                    return item;
                                })
                );
    }

    public Mono<Integer> getItemCount(WebSession session, Long itemId) {
        return Mono.just(getCart(session).getOrDefault(itemId, 0));
    }

    public Mono<BigDecimal> getTotal(WebSession session) {
        return getItems(session)
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Mono<Void> changeItemCount(WebSession session, Long itemId, String action) {
        Map<Long, Integer> cartItems = getCart(session);
        int count = cartItems.getOrDefault(itemId, 0);

        switch (action) {
            case "plus" -> cartItems.put(itemId, count + 1);
            case "minus" -> {
                if (count > 1) cartItems.put(itemId, count - 1);
                else cartItems.remove(itemId);
            }
            case "delete" -> cartItems.remove(itemId);
        }
        return Mono.empty();
    }

    public Mono<Void> clear(WebSession session) {
        getCart(session).clear();
        return Mono.empty();
    }
}

