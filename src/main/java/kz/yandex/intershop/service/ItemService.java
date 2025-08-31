package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemCustomRepository;
import kz.yandex.intershop.repository.ItemRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemCustomRepository itemCustomRepository;

    public ItemService(ItemRepository itemRepository, ItemCustomRepository itemCustomRepository) {
        this.itemRepository = itemRepository;
        this.itemCustomRepository = itemCustomRepository;
    }

    public Mono<Page<Item>> findAll(String search, int pageNumber, int pageSize, String sort) {
        Pageable pageable = switch (sort) {
            case "ALPHA" -> PageRequest.of(pageNumber - 1, pageSize, Sort.by("title").ascending());
            case "PRICE" -> PageRequest.of(pageNumber - 1, pageSize, Sort.by("price").ascending());
            default -> PageRequest.of(pageNumber - 1, pageSize);
        };

        Flux<Item> itemsFlux;
        Mono<Long> totalMono;

        if (search != null && !search.isBlank()) {
            itemsFlux = itemCustomRepository.findByTitleContainingIgnoreCase(search, pageable);
            totalMono = itemCustomRepository.countByTitleContainingIgnoreCase(search);
        } else {
            itemsFlux = itemCustomRepository.findAll(pageable);
            totalMono = itemCustomRepository.countAll();
        }

        return itemsFlux.collectList()
                .zipWith(totalMono)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }


    public Mono<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }
}
