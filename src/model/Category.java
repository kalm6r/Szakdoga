package model;

import java.util.Objects;

public class Category {
    private int id;
    private String name;
    private Subcategory subcategory; // FK: subcat_id

    public Category(int id, String name, Subcategory subcategory) {
        this.id = id;
        this.name = name;
        this.subcategory = subcategory;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Subcategory getSubcategory() { return subcategory; }

    @Override public boolean equals(Object o){ return o instanceof Category c && c.id == id; }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return name; }
}
