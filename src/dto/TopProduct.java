package dto;

public class TopProduct {
    private final int productId;
    private final String productName;
    private final String manufacturerName;
    private final String categoryName;
    private final int sellPrice;

    public TopProduct(int productId, String productName, String manufacturerName,
                      String categoryName, int sellPrice) {
        this.productId = productId;
        this.productName = productName;
        this.manufacturerName = manufacturerName;
        this.categoryName = categoryName;
        this.sellPrice = sellPrice;
    }
    public int getProductId(){ return productId; }
    public String getProductName(){ return productName; }
    public String getManufacturerName(){ return manufacturerName; }
    public String getCategoryName(){ return categoryName; }
    public int getSellPrice(){ return sellPrice; }
}
