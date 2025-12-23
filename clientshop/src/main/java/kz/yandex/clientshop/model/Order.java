package kz.yandex.clientshop.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table("orders")
public class Order {

    @Id
    private Long id;

    @Column("total_sum")
    private BigDecimal totalSum = BigDecimal.ZERO;

    @Transient // <- чтобы БД не пыталась сохранять как колонку
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public BigDecimal getTotalSum() { return totalSum; }
    public void setTotalSum(BigDecimal totalSum) { this.totalSum = totalSum; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

