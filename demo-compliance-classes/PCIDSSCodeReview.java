package com.mortgage.compliance.pcidss;

import java.util.Random;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: PCI-DSS — Code Review Requirement Before Release
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: PCI-DSS (Payment Card Industry Data Security Standard)
 * REQUIREMENT: PCI-DSS 6.3.2 — Code review before release to production
 * REQUIREMENT: PCI-DSS 6.5 — Secure coding (OWASP Top 10)
 * REQUIREMENT: PCI-DSS 3.4 — Render PAN unreadable anywhere it is stored
 * 
 * VIOLATION SCENARIO:
 *   A payment processing module handles cardholder data (PAN, CVV, expiry)
 *   but has NOT undergone the required code review before release.
 *   Additionally, the code contains multiple PCI-DSS violations:
 *   storing CVV, logging card numbers, weak encryption, no masking.
 * 
 * WHAT REGULITH AI DETECTS:
 *   → No code review record (PCI-DSS 6.3.2 violation)
 *   → CVV stored after authorization (PCI-DSS 3.2 violation)
 *   → PAN logged without masking (PCI-DSS 3.4 violation)
 *   → Weak cryptography for card data (PCI-DSS 3.5 violation)
 *   → Missing input validation (PCI-DSS 6.5.1 violation)
 *   → Deployment BLOCKED until code review + fixes complete
 * 
 * CHAIN REACTION:
 *   Code Submitted → No Review Record → PCI-DSS 6.3.2 Breach →
 *   Deployment Gate BLOCKED → Additional violations found →
 *   PCI QSA Audit Finding → Potential Card Brand Fines ($5K-$100K/month)
 * ══════════════════════════════════════════════════════════════════════
 */
public class PCIDSSCodeReview {

    // VIOLATION PCI-DSS 3.5: Encryption key hardcoded in source
    // Must be stored in HSM or approved key management system
    private static final String ENCRYPTION_KEY = "AES256-prod-k3y-m0rtg@ge!2026#Cr1t";
    private static final String PAYMENT_GATEWAY_TOKEN = "pgw-live-51MkF8gH2c9L4pQ7rT0xW3yZ";

    // VIOLATION PCI-DSS 3.2: CVV must NEVER be stored after authorization
    private String storedCVV;
    private String storedPAN;

    /**
     * VIOLATION PCI-DSS 3.2: Stores CVV (Card Verification Value).
     * 
     * PCI-DSS 3.2 STATES:
     *   "Do not store sensitive authentication data after authorization
     *    (even if encrypted). This includes CVV2/CVC2/CID values."
     * 
     * Storing CVV is an AUTOMATIC PCI-DSS FAILURE.
     * Results in: Loss of ability to process card payments.
     */
    public void processPayment(String cardNumber, String cvv, String expiry,
                               double amount, String borrowerName) {

        // VIOLATION: Storing CVV — automatic PCI failure
        this.storedCVV = cvv;
        this.storedPAN = cardNumber;

        // VIOLATION PCI-DSS 3.4: PAN logged in cleartext (not masked)
        // Must mask: show only first 6 and last 4 digits
        System.out.println("Processing payment: card=" + cardNumber +
                          " cvv=" + cvv + " exp=" + expiry +
                          " amount=$" + amount + " borrower=" + borrowerName);

        // VIOLATION: Using deprecated MD5 for "hashing" card data
        String hashedCard = "MD5:" + cardNumber.hashCode();
        System.out.println("Card hash (MD5): " + hashedCard);

        // Should be: AES-256 encryption with proper key management
        // CVV should NEVER be stored, period.
    }

    /**
     * VIOLATION PCI-DSS 6.5.1: SQL injection in payment lookup.
     * 
     * PCI-DSS 6.5 requires addressing common coding vulnerabilities
     * per OWASP Top 10. This method has SQL injection that could
     * expose ALL stored card data.
     */
    public String lookupPaymentHistory(String borrowerId, String dateRange) {
        // CRITICAL: SQL injection — could dump entire payment table
        String query = "SELECT card_number, amount, transaction_date FROM PAYMENTS " +
                      "WHERE borrower_id = '" + borrowerId + "'" +
                      " AND transaction_date > '" + dateRange + "'";

        System.out.println("Payment query: " + query);

        // Attacker input: borrowerId = "' UNION SELECT card_number, cvv, expiry FROM CARDS --"
        // Exposes ALL cardholder data in the system
        return query;
    }

    /**
     * VIOLATION PCI-DSS 3.5: Weak cryptography for card storage.
     * 
     * PCI-DSS requires strong cryptography (AES-256) with proper
     * key management. This uses DES (broken) with hardcoded key.
     */
    public String encryptCardData(String pan) {
        // VIOLATION: DES is broken — crackable in hours
        // PCI-DSS 3.5 requires AES-256 minimum
        String algorithm = "DES";  // Deprecated, broken cipher

        // VIOLATION: Key hardcoded (should be in HSM)
        String key = ENCRYPTION_KEY;

        // VIOLATION: No key rotation implemented
        // PCI-DSS requires annual key rotation at minimum

        System.out.println("Encrypting PAN with " + algorithm + " key: " + key.substring(0, 8) + "...");

        // Weak "encryption" — trivially reversible
        return "ENC:" + pan.hashCode();
    }

    /**
     * VIOLATION PCI-DSS 8.2.1: Weak authentication for payment system.
     * 
     * PCI-DSS requires multi-factor authentication for all access
     * to cardholder data environment. This uses a single weak password.
     */
    public boolean authenticatePaymentAccess(String username, String password) {
        // VIOLATION: Weak auth with no MFA
        // PCI-DSS 8.2.1: Requires strong authentication
        // PCI-DSS 8.3: Requires MFA for CDE access

        // Weak random for session token
        Random rng = new Random();
        String sessionId = String.valueOf(rng.nextInt(999999));

        System.out.println("Auth: user=" + username + " session=" + sessionId);

        // No account lockout (PCI-DSS 8.1.6: lock after 6 attempts)
        // No password complexity (PCI-DSS 8.2.3: minimum 7 chars, numeric + alpha)
        return "admin".equals(username) && "payment123".equals(password);
    }

    /**
     * VIOLATION PCI-DSS 10.2: Insufficient audit trail.
     * 
     * PCI-DSS 10.2 requires logging of ALL access to cardholder data.
     * This method accesses card data but generates no audit record.
     */
    public String retrieveCardForRefund(String transactionId) {
        // VIOLATION: No audit log generated for card data access
        // PCI-DSS 10.2.1: Log all individual access to cardholder data
        // PCI-DSS 10.2.2: Log all actions by administrators

        // Just returns stored PAN with no trail
        return this.storedPAN; // Also: shouldn't be stored in memory
    }
}
