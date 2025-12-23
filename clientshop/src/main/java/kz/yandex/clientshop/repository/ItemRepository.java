package kz.yandex.clientshop.repository;

import kz.yandex.clientshop.model.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
}

