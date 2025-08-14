package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Item> findAll(String search, int pageNumber, int pageSize, String sort) {
        Pageable pageable;
        switch (sort) {
            case "ALPHA" -> pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("title").ascending());
            case "PRICE" -> pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("price").ascending());
            default -> pageable = PageRequest.of(pageNumber - 1, pageSize);
        }

        if (search != null && !search.isBlank()) {
            return itemRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            return itemRepository.findAll(pageable);
        }
    }


    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow();
    }
}
