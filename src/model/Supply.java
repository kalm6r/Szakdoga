package model;

import java.time.LocalDate;
import java.util.Objects;

public class Supply {
    private int productId;       // PK == FK: PRODUCT(Product_id)
    private LocalDate bought;
    private int pieces;
    private int purchasePrice;
    private int sellPrice;

    public Supply(int productId, LocalDate bought, int pieces, int purchasePrice, int sellPrice) {
        this.productId = productId;
        this.bought = bought;
        this.pieces = pieces;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
    }

    public int getProductId() { return productId; }
    public LocalDate getBought() { return bought; }
    public int getPieces() { return pieces; }
    public int getPurchasePrice() { return purchasePrice; }
    public int getSellPrice() { return sellPrice; }

    @Override public boolean equals(Object o){ return o instanceof Supply s && s.productId == productId; }
    @Override public int hashCode(){ return Objects.hash(productId); }
    @Override public String toString(){ return "Supply{productId=" + productId + ", sell=" + sellPrice + " Ft}"; }
}
