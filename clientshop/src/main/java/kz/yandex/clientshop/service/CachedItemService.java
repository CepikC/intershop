package kz.yandex.clientshop.service;

import kz.yandex.clientshop.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class CachedItemService {

    private final ItemService itemService;
    private final ReactiveRedisTemplate<String, Item> redisTemplate;

    public CachedItemService(ItemService itemService, ReactiveRedisTemplate<String, Item> redisTemplate) {
        this.itemService = itemService;
        this.redisTemplate = redisTemplate;
    }

    public Mono<Item> getItemById(Long id) {
        String key = "item:" + id;

        return redisTemplate.opsForValue().get(key)
                .doOnNext(item -> System.out.println("✅ [CACHE HIT] Товар " + id + " взят из Redis"))
                .switchIfEmpty(
                        itemService.getItemById(id)
                                .doOnNext(item -> System.out.println("❌ [CACHE MISS] Товар " + id + " не найден в Redis, берём из БД"))
                                .flatMap(item -> redisTemplate.opsForValue()
                                        .set(key, item, Duration.ofMinutes(10)) // TTL = 10 мин
                                        .thenReturn(item)
                                )
                );
    }

    public Mono<Page<Item>> findAll(String search, int pageNumber, int pageSize, String sort) {
        String key = String.format("items:page:%d:size:%d:sort:%s:search:%s",
                pageNumber, pageSize, sort, search == null ? "" : search);

        // Загружаем список товаров из Redis
        return redisTemplate.opsForList().range(key, 0, -1)
                .collectList()
                .flatMap(cachedItems -> {
                    if (!cachedItems.isEmpty()) {
                        System.out.println("✅ [CACHE HIT] Страница товаров найдена в Redis по ключу: " + key);
                        // Отдаем страницу из кэша (без total — упрощённый вариант)
                        return Mono.just(new PageImpl<>(cachedItems));
                    }
                    System.out.println("❌ [CACHE MISS] Нет страницы в Redis, грузим из БД. Ключ: " + key);
                    // Если в кэше нет — тянем из БД
                    return itemService.findAll(search, pageNumber, pageSize, sort)
                            .flatMap(page -> {
                                List<Item> items = page.getContent();
                                System.out.println("📥 Сохраняем в Redis " + items.size() + " товаров по ключу: " + key);
                                return redisTemplate.opsForList()
                                        .rightPushAll(key, items)
                                        .then(redisTemplate.expire(key, Duration.ofMinutes(5))) // TTL = 5 мин
                                        .thenReturn(page);
                            });
                });
    }
}
