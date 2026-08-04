package com.mortgage.controls;

import java.util.Random;

/**
 * ═══════════════════════════════════════════════════════════════════
 * PCI-DSS CONTROLS DEMO
 * Uncomment each block to trigger specific PCI-DSS violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  PCI-3.2   → CVV Storage (auto-fail)    (Block A)
 *  PCI-3.4   → PAN Unmasked in Logs       (Block B)
 *  PCI-3.5   → Weak Encryption (DES)      (Block C)
 *  PCI-6.5   → Injection in Card Module   (Block D)
 */
public class PCIDSSControls {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public String getPaymentStatus(String transactionId) {
        return "COMPLETED";
    }


    // ─── BLOCK A: PCI-3.2 — CVV Storage (AUTOMATIC PCI FAILURE) ────
    // Violates: "Do not store CVV after authorization — even if encrypted"
    // Impact: Automatic PCI non-compliance = CANNOT process cards
    // Penalty: Card brand fines ($5K-$100K/month) + loss of processing

    // private String storedCVV; // NEVER store CVV!
    //
    // public void capturePayment(String pan, String cvv, double amount) {
    //     this.storedCVV = cvv; // AUTOMATIC PCI FAILURE
    //     System.out.println("Payment captured: card=" + pan + " cvv=" + cvv);
    // }


    // ─── BLOCK B: PCI-3.4 — PAN Logged Without Masking ─────────────
    // Violates: "Render PAN unreadable anywhere it is stored"
    // Must show: first 6 + last 4 only (e.g., 411111******1234)
    // Impact: Full card numbers in Splunk/ELK = PCI breach

    // public void logTransaction(String cardNumber, double amount, String merchant) {
    //     // VIOLATION: Full PAN in logs — must mask to first6+last4
    //     System.out.println("Transaction: card=" + cardNumber
    //                      + " amount=$" + amount + " merchant=" + merchant);
    // }


    // ─── BLOCK C: PCI-3.5 — Weak Encryption ────────────────────────
    // Violates: Must use strong crypto (AES-256 minimum)
    // DES is broken (crackable in hours), MD5 has known collisions
    // Impact: Stored card data can be decrypted by attacker

    // public String encryptCardData(String pan) {
    //     // VIOLATION: DES is broken — crackable in hours
    //     // VIOLATION: MD5 has known collisions — not for security use
    //     String desEncrypted = "DES:" + pan.hashCode();
    //     String md5Hash = "MD5:" + pan.hashCode();
    //     Random rng = new Random(); // Weak random for IV
    //     System.out.println("Card encrypted with DES, hashed with MD5");
    //     return desEncrypted;
    // }


    // ─── BLOCK D: PCI-6.5 — SQL Injection in Payment Module ────────
    // Violates: "Address common coding vulnerabilities (OWASP Top 10)"
    // Impact: Attacker can dump ALL card data from payment table
    // Penalty: Full PCI breach + forensic investigation + fines

    // public String lookupPayments(String customerId, String dateFrom) {
    //     // CRITICAL: SQL injection can expose all stored card data
    //     String query = "SELECT card_number, amount FROM PAYMENTS"
    //                  + " WHERE customer_id = '" + customerId + "'"
    //                  + " AND date > '" + dateFrom + "'";
    //     System.out.println("Payment query: " + query);
    //     return query;
    // }
}
