package model;

import java.util.Objects;

public class Subcategory {
    private int id;
    private String name;
    private Manufacturer manufacturer; // FK: manufacturer_id

    public Subcategory(int id, String name, Manufacturer manufacturer) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Manufacturer getManufacturer() { return manufacturer; }

    @Override public boolean equals(Object o){ return o instanceof Subcategory s && s.id == id; }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return name + " (" + manufacturer.getName() + ")"; }
}
