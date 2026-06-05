package org.hongxi.whatsmars.elasticsearch.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品模型，用于 Elasticsearch 示例演示
 */
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = -8203526537425911165L;

    private String id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String description;
    private List<String> tags;

    public Product() {
    }

    public Product(String id, String name, String category, double price, int quantity, String description, List<String> tags) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', category='" + category +
                "', price=" + price + ", quantity=" + quantity +
                ", description='" + description + "', tags=" + tags + "}";
    }
}
