package com.mortgage.compliance.security;

import java.util.Random;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: Security — SAST Race Condition (HIGH Severity)
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: Application Security (OWASP, PCI-DSS 6.5)
 * FINDING: Race Condition in Rate Calculation (HIGH)
 * SOURCE TOOL: Checkmarx SAST Scan
 * 
 * VIOLATION SCENARIO:
 *   A non-atomic read-modify-write on shared rate configuration leads
 *   to incorrect rate calculations under concurrent access. In a
 *   mortgage system, this means borrowers could receive wrong rates —
 *   a TILA violation AND a security vulnerability.
 * 
 * WHAT KAVACH AI DETECTS:
 *   → SAST finding: Race condition (HIGH severity)
 *   → Non-thread-safe shared mutable state
 *   → Financial calculation affected by concurrency bug
 *   → Weak random number generator (java.util.Random)
 *   → Hardcoded secrets in configuration
 * 
 * CHAIN REACTION:
 *   SAST Finding → Security Control Breach → PCI-DSS 6.5 Violation
 *   → Deployment Gate BLOCKED → SLA timer starts (48h remediation)
 * ══════════════════════════════════════════════════════════════════════
 */
public class SecurityRaceCondition {

    // VULNERABILITY: Hardcoded API credentials for rate feed service
    private static final String RATE_FEED_API_KEY = "ratefeed-prod-key-4eC39HqLyjWD7dc";
    private static final String RATE_FEED_SECRET = "ratefeed-secret-MfKQ946VSugr@Pro";

    // VULNERABILITY: Shared mutable state (race condition)
    // Multiple threads can read/modify this simultaneously
    private static double currentBaseRate = 0.0625;
    private static double currentSpread = 0.0150;
    private static long lastUpdateTimestamp = 0;

    /**
     * VULNERABILITY: Non-atomic read-modify-write.
     * Thread A reads currentBaseRate → Thread B updates it → Thread A
     * uses stale value → Borrower gets WRONG rate.
     * 
     * Checkmarx flags this as HIGH severity race condition.
     */
    public double calculateLiveRate(String borrowerId, double creditScore) {
        // READ: Non-synchronized read of shared state
        double rate = currentBaseRate;
        double spread = currentSpread;

        // MODIFY: Calculation based on potentially stale data
        if (creditScore >= 750) {
            rate = rate - 0.0025;
        } else if (creditScore < 620) {
            rate = rate + 0.0150;
        }

        double finalRate = rate + spread;

        // WRITE: Update shared state without synchronization
        lastUpdateTimestamp = System.currentTimeMillis();

        // VULNERABILITY: System.out with sensitive borrower data
        System.out.println("Rate for borrower " + borrowerId + ": " + finalRate +
                          " (credit: " + creditScore + ")");

        return finalRate;
    }

    /**
     * VULNERABILITY: Updates shared state from external feed.
     * If called concurrently with calculateLiveRate(), produces wrong results.
     */
    public void updateRateFromFeed(double newBaseRate, double newSpread) {
        // Non-atomic update — another thread may read between these two writes
        currentBaseRate = newBaseRate;   // Thread could read HERE (inconsistent state)
        currentSpread = newSpread;
        lastUpdateTimestamp = System.currentTimeMillis();

        System.out.println("Rate feed updated: base=" + newBaseRate + " spread=" + newSpread);
    }

    /**
     * VULNERABILITY: Weak random number generator.
     * java.util.Random is predictable — if used for session tokens or
     * rate-lock confirmation IDs, they can be guessed by attackers.
     */
    public String generateRateLockConfirmation(String loanId) {
        // SAST Finding: java.util.Random is not cryptographically secure
        Random rng = new Random();
        long confirmationId = rng.nextLong();

        // Should use: java.security.SecureRandom
        return "LOCK-" + loanId + "-" + Math.abs(confirmationId);
    }

    /**
     * VULNERABILITY: SSL/TLS verification disabled.
     * Connects to rate feed without verifying server certificate.
     * Enables man-in-the-middle attacks on rate data.
     */
    public void connectToRateFeed() {
        // CRITICAL: SSL bypass allows MITM attacks on rate data
        // TrustAll certificates — attacker could inject fake rates
        System.out.println("Connecting with TrustAll SSL manager...");
        // In real code: setHostnameVerifier(ALLOW_ALL) would be here
    }
}
