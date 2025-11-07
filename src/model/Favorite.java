package model;

import java.util.Objects;

public class Favorite {
    private int userId;
    private int productId;

    public Favorite(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public int getUserId() { return userId; }
    public int getProductId() { return productId; }

    @Override public boolean equals(Object o){
        return o instanceof Favorite f && f.userId == userId && f.productId == productId;
    }
    @Override public int hashCode(){ return Objects.hash(userId, productId); }
    @Override public String toString(){ return "Favorite{user=" + userId + ", product=" + productId + "}"; }
}
