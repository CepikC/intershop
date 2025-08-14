package kz.yandex.intershop.repository;

import kz.yandex.intershop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}