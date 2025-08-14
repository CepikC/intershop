package kz.yandex.intershop.model;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_sum", nullable = false)
    private BigDecimal totalSum = BigDecimal.ZERO;

    public Order() {
    }

    public Order(List<OrderItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalSum = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(Item item, int count, BigDecimal price) {
        OrderItem orderItem = new OrderItem(item, count, price);
        orderItem.setOrder(this);
        this.items.add(orderItem);
        recalculateTotal();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotalSum() { return totalSum; }
    public void setTotalSum(BigDecimal totalSum) { this.totalSum = totalSum; }
}

