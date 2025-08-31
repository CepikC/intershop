package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.model.Order;
import kz.yandex.intershop.model.OrderItem;
import kz.yandex.intershop.repository.ItemRepository;
import kz.yandex.intershop.repository.OrderItemRepository;
import kz.yandex.intershop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository; // добавляем

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
    }

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .flatMap(orderItem ->
                                        itemRepository.findById(orderItem.getItemId())
                                                .map(item -> {
                                                    orderItem.setItem(item); // <-- добавили товар
                                                    return orderItem;
                                                })
                                )
                                .collectList()
                                .map(items -> {
                                    order.setItems(items);
                                    return order;
                                })
                );
    }


    public Mono<Order> getOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .flatMap(orderItem ->
                                        itemRepository.findById(orderItem.getItemId())
                                                .map(item -> {
                                                    orderItem.setItem(item);
                                                    return orderItem;
                                                })
                                )
                                .collectList()
                                .map(orderItems -> {
                                    order.setItems(orderItems);
                                    return order;
                                })
                );
    }

    public Mono<Order> createOrderFromCart(List<Item> cartItems) {
        Order order = new Order();
        order.setTotalSum(BigDecimal.ZERO);

        return orderRepository.save(order)
                .flatMap(savedOrder ->
                        Flux.fromIterable(cartItems)
                                .map(cartItem -> {
                                    OrderItem oi = new OrderItem();
                                    oi.setItemId(cartItem.getId());
                                    oi.setCount(cartItem.getCount());
                                    oi.setPrice(cartItem.getPrice());
                                    oi.setOrderId(savedOrder.getId());
                                    return oi;
                                })
                                .collectList()
                                .flatMap(orderItems -> {
                                    BigDecimal total = orderItems.stream()
                                            .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getCount())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    savedOrder.setTotalSum(total);

                                    return orderItemRepository.saveAll(orderItems)
                                            .collectList()
                                            .doOnNext(savedOrder::setItems)
                                            .flatMap(savedItems -> orderRepository.save(savedOrder));
                                })
                );
    }
}

