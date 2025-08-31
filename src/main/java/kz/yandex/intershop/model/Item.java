package kz.yandex.intershop.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "items")
public class Item {

    @Id
    private Long id;

    @Column("title")
    private String title;

    @Column("price")
    private BigDecimal price;

    @Column("description")
    private String description;

    @Column("img_path")
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
