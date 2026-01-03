package kz.yandex.clientshop.service;

import kz.yandex.clientshop.config.security.SecurityUtils;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.model.Order;
import kz.yandex.clientshop.model.OrderItem;
import kz.yandex.clientshop.repository.ItemRepository;
import kz.yandex.clientshop.repository.OrderItemRepository;
import kz.yandex.clientshop.repository.OrderRepository;
import kz.yandex.clientshop.repository.UserRepository;
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
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ItemRepository itemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public Flux<Order> getAllOrders() {
        return SecurityUtils.currentUsername()
                .flatMap(userRepository::findByUsername)
                .flatMapMany(user -> orderRepository.findAllByUserId(user.getId()))
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
                                .map(items -> {
                                    order.setItems(items);
                                    return order;
                                })
                );
    }


    public Mono<Order> getOrderById(Long id) {
        return SecurityUtils.currentUsername()
                .switchIfEmpty(Mono.error(new RuntimeException("User not authenticated")))
                .flatMap(username ->
                        orderRepository.findById(id)
                                .filter(order ->
                                        order.getUserId().equals(Long.valueOf(username))
                                )
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
                                )
                );
    }

    public Mono<Order> createOrderFromCart(List<Item> cartItems) {
        return SecurityUtils.currentUsername()
                .flatMap(username ->
                        userRepository.findByUsername(username)
                )
                .switchIfEmpty(Mono.error(new RuntimeException("User not authenticated")))
                .flatMap(user -> {
                    Order order = new Order();
                    order.setUserId(user.getId()); // ⭐ ВАЖНО
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
                                                        .map(oi -> oi.getPrice()
                                                                .multiply(BigDecimal.valueOf(oi.getCount())))
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                                savedOrder.setTotalSum(total);

                                                return orderItemRepository.saveAll(orderItems)
                                                        .collectList()
                                                        .doOnNext(savedOrder::setItems)
                                                        .flatMap(items -> orderRepository.save(savedOrder));
                                            })
                            );
                });
    }

    public BigDecimal calculateTotalPrice(List<Item> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

