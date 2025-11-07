package model;

import java.util.Objects;

public class Manufacturer {
    private int id;
    private String name;
    private String modelName;

    public Manufacturer(int id, String name, String modelName) {
        this.id = id;
        this.name = name;
        this.modelName = modelName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getModelName() { return modelName; }

    @Override public boolean equals(Object o){ return o instanceof Manufacturer m && m.id == id; }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return name + (modelName != null ? " " + modelName : ""); }
}
