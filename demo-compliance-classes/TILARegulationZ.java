package com.mortgage.compliance.regulatory;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: TILA / Regulation Z — APR Calculation Accuracy
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: Regulatory — Truth in Lending Act (TILA)
 * REGULATION: Regulation Z (12 CFR Part 1026)
 * REQUIREMENT: APR must be accurate to within 1/8 of 1 percent (0.125%)
 * 
 * VIOLATION SCENARIO:
 *   A developer modifies the APR calculation logic for ARM loans to
 *   implement new CFPB guidance, but introduces a precision error.
 *   The calculated APR is off by 0.23% — exceeding the TILA tolerance.
 *   This affects every Loan Estimate and Closing Disclosure generated.
 * 
 * WHAT REGULITH AI DETECTS:
 *   → Change to APR calculation method (financial logic modified)
 *   → TILA Reg Z tolerance threshold at risk
 *   → Missing validation of calculation precision
 *   → CFPB enforcement action exposure
 *   → Borrower restitution liability ($millions)
 * 
 * CHAIN REACTION:
 *   Code Change → APR Logic Modified → TILA Tolerance Breach →
 *   CFPB Enforcement Risk → Borrower Restitution → SOX Disclosure
 * ══════════════════════════════════════════════════════════════════════
 */
public class TILARegulationZ {

    // TILA Regulation Z: APR tolerance is 1/8 of 1 percent
    private static final double TILA_APR_TOLERANCE = 0.00125; // 0.125%

    // CFPB Bulletin 2026-03: New ARM rate cap methodology
    private static final double ARM_PERIODIC_CAP = 0.02;    // 2% per adjustment
    private static final double ARM_LIFETIME_CAP = 0.06;    // 6% lifetime

    // VIOLATION: Hardcoded fee schedule that should come from regulated config
    private static final String FEE_CONFIG_KEY = "AKIAIOSFODNN7EXAMPLE";

    /**
     * VIOLATION: APR calculation has precision error.
     * 
     * TILA requires APR disclosure accurate to 1/8 of 1%.
     * This implementation uses integer division that truncates precision,
     * causing APR to be understated by up to 0.23% on large loans.
     * 
     * Impact: Every Loan Estimate (LE) and Closing Disclosure (CD)
     * generated with this code contains an inaccurate APR.
     */
    public double calculateAPR(double principal, double nominalRate, int termYears,
                               double originationFee, double closingCosts) {

        double totalFinanceCharge = originationFee + closingCosts;
        double adjustedPrincipal = principal + totalFinanceCharge;

        double monthlyRate = nominalRate / 12;
        int numPayments = termYears * 12;

        // PRECISION ERROR: Integer division before multiplication
        // Should be: (double)(numPayments) in calculation
        double monthlyPayment = adjustedPrincipal *
            (monthlyRate * Math.pow(1 + monthlyRate, numPayments)) /
            (Math.pow(1 + monthlyRate, numPayments) - 1);

        double totalPaid = monthlyPayment * numPayments;
        double totalInterest = totalPaid - principal;

        // BUG: Using integer division (termYears is int) — truncates result
        // This causes APR to be UNDERSTATED by 0.15-0.23% on 30-year loans
        double apr = (totalInterest / principal) / termYears;

        // VIOLATION: No tolerance check before returning
        // Should validate: |calculated_apr - disclosed_apr| < 0.00125
        System.out.println("APR calculated: " + apr + " for loan " + principal);

        return apr;
    }

    /**
     * VIOLATION: ARM rate adjustment without TILA-required disclosures.
     * 
     * When an ARM rate adjusts, TILA requires:
     *   - 210-day advance notice to borrower
     *   - New payment amount disclosure
     *   - Maximum rate disclosure
     *   - Payment shock disclosure
     * 
     * This method adjusts the rate but generates NO disclosures.
     */
    public double adjustARMRate(double currentRate, double indexRate, double margin,
                                int adjustmentPeriod) {

        double newRate = indexRate + margin;

        // Apply periodic cap per CFPB 2026-03
        if (newRate > currentRate + ARM_PERIODIC_CAP) {
            newRate = currentRate + ARM_PERIODIC_CAP;
        }

        // VIOLATION: No disclosure generation
        // TILA 12 CFR 1026.20(c) requires advance notice
        // No borrower notification triggered
        // No payment change letter generated

        System.out.println("ARM adjusted: " + currentRate + " -> " + newRate +
                          " (period " + adjustmentPeriod + ")");

        return newRate;
    }

    /**
     * VIOLATION: Fee calculation that could trigger TILA tolerance breach.
     * 
     * If fees are calculated incorrectly, the APR disclosed on the
     * Loan Estimate will differ from the Closing Disclosure APR,
     * triggering a TILA "changed circumstance" — or worse, a violation.
     */
    public double calculateTotalFinanceCharge(double principal, double rate,
                                              int termYears, boolean isVA) {

        double origination = principal * 0.01;    // 1% origination
        double discount = principal * 0.005;      // 0.5% discount points
        double processing = 850.00;
        double underwriting = 1200.00;
        double appraisal = 550.00;

        // VIOLATION: VA loans cannot charge origination > 1% (38 CFR 36.4313)
        // This code charges origination + discount regardless of loan type
        // VA borrowers would be overcharged — TILA + VA violation
        double totalCharges = origination + discount + processing + underwriting + appraisal;

        // Not validating against TILA tolerance before disclosure
        System.out.println("Finance charges: $" + totalCharges);

        return totalCharges;
    }
}
