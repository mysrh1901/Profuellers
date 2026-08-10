package com.mortgage.service;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  DEMO FILE — Uncomment blocks one at a time during demo         ║
 * ║                                                                  ║
 * ║  Each block triggers a DIFFERENT compliance domain:              ║
 * ║    1. SOX        → Uncomment Block 1                            ║
 * ║    2. SECURITY   → Uncomment Block 2                            ║
 * ║    3. TILA/Reg Z → Uncomment Block 3                            ║
 * ║    4. ECOA       → Uncomment Block 4                            ║
 * ║    5. CONTRACTUAL→ Uncomment Block 5                            ║
 * ║    6. PCI-DSS    → Uncomment Block 6                            ║
 * ║                                                                  ║
 * ║  INSTRUCTIONS:                                                   ║
 * ║    - Uncomment ONE block at a time                              ║
 * ║    - Save the file (Ctrl+S)                                     ║
 * ║    - Dashboard refreshes in 3 seconds with new findings         ║
 * ║    - Show how the Chain Reactor propagates across domains        ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class LoanService {

    // ═══════════════════════════════════════════════════════════════════
    // CLEAN BASELINE — No violations (all clear on dashboard)
    // ═══════════════════════════════════════════════════════════════════

    public double calculateMonthlyPayment(double principal, double rate, int years) {
        double monthlyRate = rate / 12.0;
        int payments = years * 12;
        return principal * (monthlyRate * Math.pow(1 + monthlyRate, payments))
                / (Math.pow(1 + monthlyRate, payments) - 1);
    }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 1: SOX — Change to financial system without dual approval
    // Uncomment below to trigger:
    //   → SOX ITGC-CM-01 (Dual Approval Required)
    //   → SOX ITGC-SD-01 (Security Testing Before Release)
    //   → Audit Trail Gap
    // ═══════════════════════════════════════════════════════════════════

    // public void adjustInterestRate(String loanId, double newRate) {
    //     // SOX VIOLATION: Direct modification of financial-significant data
    //     // No change ticket, no approval, no segregation of duties
    //     String sql = "UPDATE INTEREST_RATES SET rate_value = " + newRate
    //                + " WHERE loan_id = '" + loanId + "'";
    //     System.out.println("Rate changed to: " + newRate + " for loan: " + loanId);
    //     // This bypasses all SOX controls — Material Weakness finding
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 2: SECURITY — SAST Race Condition + Hardcoded Secret
    // Uncomment below to trigger:
    //   → Hardcoded Secret (CRITICAL)
    //   → System.out bypasses audit (HIGH)
    //   → Weak Random Number Generator (MEDIUM)
    //   → Deployment Gate BLOCKED
    // ═══════════════════════════════════════════════════════════════════

    // private static final String API_SECRET = "sk-prod-m0rtg@ge!2026#Crit1cal";
    //
    // public String generateSessionToken(String borrowerId) {
    //     java.util.Random rng = new java.util.Random();
    //     long token = rng.nextLong();
    //     System.out.println("Token generated for borrower: " + borrowerId + " = " + token);
    //     return "TOK-" + Math.abs(token);
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 3: TILA / Regulation Z — APR Calculation Error
    // Uncomment below to trigger:
    //   → TILA Reg Z tolerance violation (APR off by > 1/8 of 1%)
    //   → Financial logic change without validation
    //   → CFPB enforcement risk
    // ═══════════════════════════════════════════════════════════════════

    // public double calculateAPR(double principal, double rate, int termYears, double fees) {
    //     // BUG: Integer division truncates precision on 30-year loans
    //     // APR understated by 0.23% — exceeds TILA 1/8% tolerance
    //     double totalInterest = (principal * rate * termYears) + fees;
    //     double apr = totalInterest / principal / termYears; // integer division!
    //     System.out.println("APR calculated: " + apr + " for principal: " + principal);
    //     // No tolerance check: |calculated - disclosed| must be < 0.00125
    //     return apr;
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 4: Fair Lending (ECOA) — ZIP Code Proxy Discrimination
    // Uncomment below to trigger:
    //   → ECOA Regulation B violation (protected class proxy)
    //   → Disparate impact risk
    //   → DOJ civil rights enforcement exposure
    // ═══════════════════════════════════════════════════════════════════

    // public boolean checkBorrowerEligibility(double income, int creditScore, String zipCode) {
    //     double dti = income / 5000.0;
    //     // ECOA VIOLATION: ZIP code is a proxy for race/national origin
    //     // This effectively implements digital redlining
    //     if (zipCode.startsWith("100") || zipCode.startsWith("606")) {
    //         return false; // Denying loans in minority neighborhoods
    //     }
    //     System.out.println("Eligibility: income=" + income + " zip=" + zipCode + " score=" + creditScore);
    //     return creditScore >= 620 && dti <= 0.43;
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 5: CONTRACTUAL — MSA §7.2 SQL Injection (48h SLA / $50K)
    // Uncomment below to trigger:
    //   → SQL Injection (CRITICAL) — starts 48-hour SLA clock
    //   → MSA §7.2 penalty: $50,000 per incident
    //   → Client termination right activated
    //   → Insecure HTTP (data in transit)
    // ═══════════════════════════════════════════════════════════════════

    // public String searchLoans(String borrowerName, String status) {
    //     // CRITICAL: SQL injection — attacker can dump all loan data
    //     // MSA §7.2: "Critical vulns must be fixed within 48 hours"
    //     // PENALTY: $50,000 per incident + right to terminate
    //     String query = "SELECT * FROM LOANS WHERE name = '" + borrowerName
    //                  + "' AND status = '" + status + "'";
    //     System.out.println("Query: " + query);
    //     // Also sending data over unencrypted HTTP
    //     String endpoint = "http://analytics.internal.com/api/loans";
    //     return query;
    // }


    // ═══════════════════════════════════════════════════════════════════
    // BLOCK 6: PCI-DSS — Card Data Handling Violations
    // Uncomment below to trigger:
    //   → PCI-DSS 3.2 violation (CVV stored after auth)
    //   → PCI-DSS 3.4 violation (PAN logged unmasked)
    //   → PCI-DSS 3.5 violation (DES encryption — broken cipher)
    //   → PCI-DSS 6.3.2 (no code review before release)
    //   → Automatic PCI failure — cannot process cards
    // ═══════════════════════════════════════════════════════════════════

    // private String storedCVV;
    //
    // public void processPayment(String cardNumber, String cvv, double amount) {
    //     // PCI VIOLATION: Storing CVV — automatic PCI failure
    //     this.storedCVV = cvv;
    //     // PCI VIOLATION: Logging full PAN — must mask (show only first 6 + last 4)
    //     System.out.println("Payment: card=" + cardNumber + " cvv=" + cvv + " amt=$" + amount);
    //     // PCI VIOLATION: DES is broken — must use AES-256
    //     String encrypted = "DES:" + cardNumber.hashCode();
    //     System.out.println("Encrypted with DES: " + encrypted);
    // }

}
