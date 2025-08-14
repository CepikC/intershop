package kz.yandex.intershop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 1000)
    private String description;

    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @Transient
    private int count;

    public Item() {
    }

    public Item(String title, BigDecimal price, String description, String imgPath) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.imgPath = imgPath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImgPath() { return imgPath; }
    public void setImgPath(String imgPath) { this.imgPath = imgPath; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
