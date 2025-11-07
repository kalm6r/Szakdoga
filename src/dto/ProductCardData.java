package dto;

public class ProductCardData {
    private final int id;
    private final String name;
    private final String categoryName;
    private final Integer sellPrice;

    public ProductCardData(int id, String name, String categoryName, Integer sellPrice) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
        this.sellPrice = sellPrice;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategoryName() { return categoryName; }
    public Integer getSellPrice() { return sellPrice; }
}
