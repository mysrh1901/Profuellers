package com.mortgage.controls;

/**
 * ═══════════════════════════════════════════════════════════════════
 * CONTRACTUAL — MSA / SLA CONTROLS DEMO
 * Uncomment each block to trigger specific contractual violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  MSA-7.2  → Critical Vuln SLA ($50K)    (Block A)
 *  MSA-8.3  → Encryption in Transit       (Block B)
 *  MSA-10.2 → PII in Logs                 (Block C)
 *  MSA-12.1 → Data Residency Breach       (Block D)
 */
public class ContractualControls {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public String getLoanId(String reference) {
        return "LOAN-" + reference.hashCode();
    }


    // ─── BLOCK A: MSA-7.2 — Critical Vulnerability (48h SLA) ───────
    // Violates: SQL injection = CRITICAL vulnerability
    // SLA: Must fix within 48 hours of discovery
    // Penalty: $50,000 per incident + engagement termination right
    // Revenue at risk: $4.2M/year engagement

    // public String searchBorrowers(String name, String status) {
    //     // CRITICAL SQL INJECTION — 48 HOUR CLOCK STARTS NOW
    //     String query = "SELECT * FROM BORROWERS WHERE name = '" + name
    //                  + "' AND status = '" + status + "'";
    //     System.out.println("Search: " + query);
    //     return query;
    // }


    // ─── BLOCK B: MSA-8.3 — Encryption in Transit ──────────────────
    // Violates: All data must be encrypted with TLS 1.2+
    // Impact: Loan data transmitted over unencrypted HTTP
    // Penalty: Contract breach + GLBA Safeguards Rule violation

    // public void sendToAnalytics(String loanId, double amount) {
    //     // VIOLATION: HTTP (not HTTPS) — data in transit unencrypted
    //     String url = "http://analytics.internal.com/api/v2/ingest";
    //     System.out.println("Sending to: " + url + " loan=" + loanId + " amt=" + amount);
    // }


    // ─── BLOCK C: MSA-10.2 — No PII in Application Logs ────────────
    // Violates: "PII must not appear in logs, debug output, or error messages"
    // Impact: SSN, income, and address logged to Splunk/ELK
    // Penalty: GLBA + CCPA + contractual breach

    // public void processApplication(String ssn, double income, String employer) {
    //     // VIOLATION: Logging SSN, income, employer = PII in logs
    //     System.out.println("Application: SSN=" + ssn + " income=$" + income
    //                      + " employer=" + employer);
    // }


    // ─── BLOCK D: MSA-12.1 — Data Residency (US-Only) ──────────────
    // Violates: "All data must remain within US boundaries (us-east-1, us-west-2)"
    // Impact: Sending loan data to EU region
    // Penalty: IMMEDIATE TERMINATION right per MSA §12.1

    // private static final String EU_ENDPOINT = "http://backup.eu-west-1.internal.com/replicate";
    //
    // public void replicateData(String loanData) {
    //     // VIOLATION: Sending US client data to EU region
    //     // MSA §12.1: Immediate termination right
    //     System.out.println("Replicating to EU: " + EU_ENDPOINT);
    // }
}
