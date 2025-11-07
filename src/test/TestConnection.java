package test;

import util.DBUtil;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("✅ Kapcsolat sikeresen létrejött az Oracle DB-vel!");
        } catch (Exception e) {
            System.out.println("❌ Sikertelen kapcsolódás!");
            e.printStackTrace();
        }
    }
}
