package model;

import java.time.LocalDate;
import java.util.Objects;

public class User {
    private int id;
    private String userName;
    private String email;
    private String password;
    private LocalDate createdAt;

    public User(int id, String userName, String email, String password, LocalDate createdAt) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDate getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o){ return o instanceof User u && u.id == id; }
    @Override public int hashCode(){ return Objects.hash(id); }
    @Override public String toString(){ return userName + " <" + email + ">"; }
}
