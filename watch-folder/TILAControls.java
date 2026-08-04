package com.mortgage.controls;

/**
 * ═══════════════════════════════════════════════════════════════════
 * TILA / REGULATION Z CONTROLS DEMO
 * Uncomment each block to trigger specific TILA control violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  TILA-APR-01  → APR Accuracy Error    (Block A)
 *  TILA-ARM-01  → ARM Notice Missing    (Block B)
 *  TILA-RESPA-01→ Fee Tolerance Breach  (Block C)
 */
public class TILAControls {

    private static final double TILA_TOLERANCE = 0.00125; // 1/8 of 1%

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public double getBaseRate() {
        return 0.0675;
    }


    // ─── BLOCK A: TILA-APR-01 — APR Calculation Inaccuracy ─────────
    // Violates: APR off by more than 1/8 of 1% (TILA Reg Z 12 CFR 1026.22)
    // Impact: Every Loan Estimate and Closing Disclosure is wrong
    // Penalty: CFPB enforcement + borrower restitution ($millions)

    // public double calculateAPR(double principal, double rate, int years, double fees) {
    //     // BUG: Integer division truncates result on 30-year loans
    //     // APR understated by ~0.23% — EXCEEDS TILA tolerance of 0.125%
    //     double totalCost = principal + fees;
    //     double interest = totalCost * rate * years;
    //     double apr = interest / principal / years; // PRECISION ERROR
    //     System.out.println("APR = " + apr + " (tolerance check: SKIPPED)");
    //     return apr;
    // }


    // ─── BLOCK B: TILA-ARM-01 — ARM Rate Adjustment Without Notice ─
    // Violates: 12 CFR 1026.20(c) — 210-day advance notice required
    // Impact: Borrower unaware of payment increase
    // Penalty: TILA violation + borrower damages

    // public double adjustARMRate(double currentRate, double indexRate, double margin) {
    //     double newRate = indexRate + margin;
    //     if (newRate > currentRate + 0.02) {
    //         newRate = currentRate + 0.02; // periodic cap
    //     }
    //     // VIOLATION: No disclosure generated, no borrower notification
    //     // TILA requires 210-day advance notice + payment change letter
    //     System.out.println("ARM adjusted: " + currentRate + " -> " + newRate);
    //     return newRate;
    // }


    // ─── BLOCK C: TILA-RESPA-01 — Fee Tolerance Breach ─────────────
    // Violates: 12 CFR 1026.19(e) — fees cannot exceed LE by > 10%
    // Impact: Closing Disclosure fees higher than Loan Estimate
    // Penalty: Lender must absorb excess fees + CFPB action

    // public double calculateClosingFees(double principal, boolean isVA) {
    //     double origination = principal * 0.015; // 1.5% — VA max is 1%!
    //     double processing = 1200.00;
    //     double underwriting = 1500.00;
    //     // VIOLATION: VA loans cannot exceed 1% origination (38 CFR 36.4313)
    //     // Also: total may exceed Loan Estimate by > 10% = tolerance breach
    //     double total = origination + processing + underwriting;
    //     System.out.println("Closing fees: $" + total + " (VA: " + isVA + ")");
    //     return total;
    // }
}
