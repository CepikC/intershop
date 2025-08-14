package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.model.Order;
import kz.yandex.intershop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    public Order createOrderFromCart(List<Item> cartItems) {
        Order order = new Order();
        for (Item cartItem : cartItems) {
            order.addItem(cartItem, cartItem.getCount(), cartItem.getPrice());
        }
        return orderRepository.save(order);
    }

}
