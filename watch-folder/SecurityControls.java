package com.mortgage.controls;

import java.util.Random;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SECURITY CONTROLS DEMO (OWASP / SAST)
 * Uncomment each block to trigger specific security control violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  OWASP-A03 → Injection           (Block A)
 *  OWASP-A02 → Cryptographic Fail  (Block B)
 *  OWASP-A07 → Hardcoded Secrets   (Block C)
 *  CWE-362   → Race Condition      (Block D)
 */
public class SecurityControls {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public String getLoanStatus(String loanId) {
        return "ACTIVE";
    }


    // ─── BLOCK A: OWASP-A03 — SQL Injection ────────────────────────
    // Violates: String concatenation in SQL query
    // Impact: Attacker can dump/modify entire database

    // public String findBorrower(String name) {
    //     String query = "SELECT * FROM BORROWERS WHERE name = '" + name + "'";
    //     System.out.println("Executing: " + query);
    //     return query;
    // }


    // ─── BLOCK B: OWASP-A02 — Weak Cryptography ───────────────────
    // Violates: Using DES (broken) and MD5 (broken) for sensitive data

    // public String hashPassword(String password) {
    //     // DES is crackable in hours, MD5 has known collisions
    //     String hash = "MD5:" + password.hashCode();
    //     String encrypted = "DES:" + password;
    //     System.out.println("Stored hash: " + hash);
    //     return hash;
    // }


    // ─── BLOCK C: OWASP-A07 — Hardcoded Credentials ───────────────
    // Violates: Secrets in source code (extractable from JAR/git history)

    // private static final String AWS_KEY = "AKIAIOSFODNN7prod2026";
    // private static final String JWT_SECRET = "eyJhbGciOiJSUzI1N!Pr0d#Key";
    // private static final String DB_PASS = "M0rtg@ge#Pr0d!2026$Critical";
    //
    // public void connectToDatabase() {
    //     System.out.println("Connecting with: " + DB_PASS.substring(0, 5) + "...");
    // }


    // ─── BLOCK D: CWE-362 — Race Condition ─────────────────────────
    // Violates: Non-thread-safe shared mutable state in rate calc
    // Impact: Borrowers could get wrong rate under concurrent access

    // private static double sharedRate = 0.0625;
    //
    // public double getAndUpdateRate(double newRate) {
    //     double current = sharedRate; // READ (stale if another thread writes)
    //     sharedRate = newRate;        // WRITE (non-atomic)
    //     Random rng = new Random();   // Weak random for rate-lock ID
    //     System.out.println("Rate: " + current + " -> " + newRate);
    //     return current;
    // }
}
