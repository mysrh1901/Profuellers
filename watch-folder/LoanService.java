package com.mortgage.service;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  KAVACH AI — Compliance Demo File                               ║
 * ║  Uncomment ONE block at a time, save, watch dashboard change    ║
 * ║                                                                  ║
 * ║  These are REAL compliance issues — not basic security bugs      ║
 * ║  SonarQube CANNOT detect these. Only KAVACH can.                ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class LoanService {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public double getFixedRate(int creditScore) {
        if (creditScore >= 750) return 0.0575;
        if (creditScore >= 700) return 0.0625;
        return 0.0675;
    }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 1: SOX — Financial Record Override Without Approval
    // Real issue: Developer can modify loan balance directly
    // No change ticket, no dual approval, no audit trail
    // Triggers: ITGC-CM-01, ITGC-CM-02, SOX Section 404
    // ═══════════════════════════════════════════════════════════════════

    // public void adjustLoanBalance(String loanId, double newBalance) {
    //     // Production loan balance changed with no approval workflow
    //     // SOX requires: change ticket + dual approval + test evidence
    //     System.out.println("Balance override: loan=" + loanId + " new=$" + newBalance);
    //     // No audit log, no segregation of duties, no rollback
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 2: TILA/Reg Z — APR Calculation Using Wrong Day Count
    // Real issue: Using 360-day year instead of 365 changes APR
    // Exceeds TILA tolerance of 1/8 of 1% on 30-year loans
    // Triggers: TILA 12 CFR 1026.22, CFPB enforcement risk
    // ═══════════════════════════════════════════════════════════════════

    // public double calculateAPR(double principal, double rate, int years) {
    //     // BUG: 360-day year understates APR by 0.19% on 30yr loans
    //     // TILA tolerance is 0.125% — this EXCEEDS it
    //     double dailyRate = rate / 360; // Should be /365 for TILA compliance
    //     double apr = dailyRate * 365 * principal / (principal * years);
    //     System.out.println("APR calculated: " + apr + " (using 360-day year)");
    //     return apr;
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 3: Fair Lending (ECOA) — Pricing Based on Neighborhood
    // Real issue: Rate adjustment using property location as factor
    // ZIP-based pricing correlates with race = disparate impact
    // Triggers: ECOA Reg B, Fair Housing Act, DOJ civil rights
    // ═══════════════════════════════════════════════════════════════════

    // public double adjustRateByLocation(double baseRate, String propertyZip) {
    //     // ECOA VIOLATION: property location correlates with race
    //     // This creates disparate impact on minority borrowers
    //     if (propertyZip.startsWith("481") || propertyZip.startsWith("606")) {
    //         baseRate += 0.0125; // 1.25% higher in specific neighborhoods
    //     }
    //     System.out.println("Rate adjusted for location: " + propertyZip + " rate=" + baseRate);
    //     return baseRate;
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 4: PII Exposure — Borrower SSN Logged to Console
    // Real issue: Sensitive data written to application logs
    // Violates GLBA Safeguards Rule + MSA Section 10.2
    // Triggers: GLBA, CCPA, MSA No-PII-in-Logs clause
    // ═══════════════════════════════════════════════════════════════════

    // public void processBorrowerApplication(String ssn, double income, String employer) {
    //     // VIOLATION: PII in logs — SSN, income visible in Splunk/ELK
    //     // MSA 10.2: "PII must not appear in application logs"
    //     // GLBA Safeguards Rule: must protect customer financial data
    //     System.out.println("Processing: SSN=" + ssn + " income=$" + income + " employer=" + employer);
    //     // If logs are breached: SSN exposure → identity theft → class action
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 5: Contractual — Production Credential Hardcoded
    // Real issue: Database password in source code (extractable from JAR)
    // MSA Section 9.1 requires all secrets in approved vault
    // Triggers: MSA 9.1 + MSA 7.2 (48h SLA, $50K penalty)
    // ═══════════════════════════════════════════════════════════════════

    // private static final String PROD_DB_PASS = "Mortg@ge#Pr0d!2026$Live";
    //
    // public void connectToLoanDatabase() {
    //     // MSA VIOLATION: Credential in source — extractable from JAR/git history
    //     // MSA 9.1: "All credentials in approved vault (Secrets Manager)"
    //     // If discovered: 48h SLA starts, $50K penalty per MSA 7.2
    //     System.out.println("Connecting with embedded credential...");
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 6: Data Residency — Sending Loan Data to EU Region
    // Real issue: US client data replicated to EU endpoint
    // MSA Section 12.1: "Data must remain within US boundaries"
    // Triggers: MSA 12.1 (immediate termination right)
    // ═══════════════════════════════════════════════════════════════════

    // public void replicateToBackup(String loanData) {
    //     // MSA VIOLATION: US client data sent to EU region
    //     // MSA 12.1: "All data must remain in us-east-1 or us-west-2"
    //     // If discovered: client can terminate engagement IMMEDIATELY
    //     String euEndpoint = "http://backup.eu-west-1.internal.com/replicate";
    //     System.out.println("Replicating to: " + euEndpoint + " data=" + loanData);
    // }

}
