package model;

import java.util.Objects;

public class Product {
    private int id;
    private String name;
    private int userId;      // FK: USERS(User_id)
    private Category category; // FK: CATEGORY(Category_id)
    private String imageUrl;
    private int subcatId;

    public Product(int id, String name, int userId, Category category, String imageUrl, int subcatId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.category = category;
        this.imageUrl = imageUrl;
        this.subcatId = subcatId;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getUserId() { return userId; }
    public Category getCategory() { return category; }
    public Subcategory getSubcategory() { return category == null ? null : category.getSubcategory(); }
    public String getImageUrl() { return imageUrl; }
    public int getSubcatId() { return subcatId; }

    @Override public boolean equals(Object o){ return o instanceof Product p && p.id == id; }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return name + " [#" + id + "]"; }
}
