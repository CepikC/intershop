package kz.yandex.clientshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.math.BigDecimal;

@Table(name = "order_items")
public class OrderItem {

    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column("count")
    private int count;

    @Column("price")
    private BigDecimal price;

    @Column("order_id")
    private Long orderId;

    @Transient // чтобы Spring Data R2DBC не пытался сохранить это поле в БД
    private Item item;

    public OrderItem() {
    }

    public OrderItem(Long itemId, int count, BigDecimal price) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
}
