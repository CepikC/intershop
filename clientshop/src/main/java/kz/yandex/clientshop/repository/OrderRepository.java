package kz.yandex.clientshop.repository;

import kz.yandex.clientshop.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByUserId(Long userId);
}