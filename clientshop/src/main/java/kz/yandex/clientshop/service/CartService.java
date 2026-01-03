package kz.yandex.clientshop.service;

import kz.yandex.clientshop.config.security.SecurityUtils;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private final Map<String, Map<Long, Integer>> carts = new ConcurrentHashMap<>();

    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(String username) {
        return carts.computeIfAbsent(username, u -> new ConcurrentHashMap<>());
    }

    public Flux<Item> getItems() {
        return SecurityUtils.currentUsername()
                .flatMapMany(username ->
                        Flux.fromIterable(getCart(username).entrySet())
                                .flatMap(entry ->
                                        itemRepository.findById(entry.getKey())
                                                .map(item -> {
                                                    item.setCount(entry.getValue());
                                                    return item;
                                                })
                                )
                );
    }

    public Mono<Integer> getItemCount(Long itemId) {
        return SecurityUtils.currentUsername()
                .map(username ->
                        getCart(username).getOrDefault(itemId, 0)
                )
                .defaultIfEmpty(0);
    }

    public Mono<BigDecimal> getTotal() {
        return getItems()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Mono<Void> changeItemCount(Long itemId, String action) {
        return SecurityUtils.currentUsername()
                .doOnNext(username -> {
                    Map<Long, Integer> cart = getCart(username);
                    int count = cart.getOrDefault(itemId, 0);

                    switch (action) {
                        case "plus" -> cart.put(itemId, count + 1);
                        case "minus" -> {
                            if (count > 1) cart.put(itemId, count - 1);
                            else cart.remove(itemId);
                        }
                        case "delete" -> cart.remove(itemId);
                    }
                })
                .then();
    }

    public Mono<Void> clear() {
        return SecurityUtils.currentUsername()
                .doOnNext(username -> getCart(username).clear())
                .then();
    }
}

