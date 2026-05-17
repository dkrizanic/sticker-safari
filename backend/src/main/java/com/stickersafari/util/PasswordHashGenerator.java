package com.stickersafari.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for migration files.
 * Run this class to generate hashed passwords for seed data.
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Generating BCrypt hashes for seed data passwords:");
        System.out.println("================================================");
        
        // Generate hash for "collector" password
        String collectorPassword = "collector";
        String collectorHash = encoder.encode(collectorPassword);
        System.out.println("\nPassword: " + collectorPassword);
        System.out.println("Hash: " + collectorHash);
        
        // Generate hash for "admin" password
        String adminPassword = "admin";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("\nPassword: " + adminPassword);
        System.out.println("Hash: " + adminHash);
        
        System.out.println("\n================================================");
        System.out.println("Copy these hashes to your migration file V2__seed_initial_data.sql");
    }
}
