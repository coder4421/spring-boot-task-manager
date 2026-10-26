package com.vrund.tasks;
import jakarta.persistence.*;
@Entity
@Table(name = "app_accounts")
class Account {
    @Id @Column(length = 64) String username;
    @Column(nullable = false, length = 100) String passwordHash;
    protected Account() {}
    Account(String username, String passwordHash) { this.username = username; this.passwordHash = passwordHash; }
}
