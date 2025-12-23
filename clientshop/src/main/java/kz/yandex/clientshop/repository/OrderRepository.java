package kz.yandex.clientshop.repository;

import kz.yandex.clientshop.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}