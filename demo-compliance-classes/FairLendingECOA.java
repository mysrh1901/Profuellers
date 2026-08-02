package com.mortgage.compliance.fairlending;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: Fair Lending (ECOA) — Eligibility Logic Touches Borrower Data
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: Fair Lending — Equal Credit Opportunity Act (ECOA)
 * REGULATION: Regulation B (12 CFR Part 1002)
 * REQUIREMENT: Loan decisions must NOT discriminate based on race, color,
 *              religion, national origin, sex, marital status, or age.
 * 
 * VIOLATION SCENARIO:
 *   A developer modifies the borrower eligibility logic and introduces
 *   factors that correlate with protected characteristics. The model
 *   uses ZIP code (proxy for race) and income source type (proxy for
 *   gender/marital status) as decision factors — creating disparate
 *   impact on protected groups.
 * 
 * WHAT REGULITH AI DETECTS:
 *   → Eligibility logic modified (touches borrower PII)
 *   → Protected characteristic proxies used in decision logic
 *   → No disparate impact testing performed
 *   → Fair lending model risk triggered
 *   → DOJ civil rights enforcement exposure
 * 
 * CHAIN REACTION:
 *   Code Change → Eligibility Logic Modified → ECOA Proxy Detected →
 *   Disparate Impact Risk → DOJ/CFPB Fair Lending Review →
 *   SOX Disclosure (litigation reserve) → Reputational Damage
 * ══════════════════════════════════════════════════════════════════════
 */
public class FairLendingECOA {

    // Maximum DTI ratio for conventional conforming loans
    private static final double MAX_DTI_RATIO = 0.43;
    private static final double MAX_DTI_RATIO_EXCEPTION = 0.50;

    // VIOLATION: Hardcoded "risk adjustment" database credentials
    private static final String SCORING_DB_PASS = "F@irL3nd!ng#Pr0d2026";

    /**
     * VIOLATION: Borrower eligibility uses ZIP code as a factor.
     * 
     * ZIP code is a well-known proxy for race and national origin.
     * Using it in credit decisions creates disparate impact on
     * minority borrowers — violating ECOA and Fair Housing Act.
     * 
     * ECOA prohibits: considering race, color, religion, national
     * origin, sex, marital status, or age in credit decisions.
     */
    public EligibilityResult determineBorrowerEligibility(
            double annualIncome, double monthlyDebt, double loanAmount,
            int creditScore, String zipCode, String incomeSourceType) {

        double dti = monthlyDebt / (annualIncome / 12.0);

        // Base eligibility check (legitimate)
        boolean eligible = (dti <= MAX_DTI_RATIO) && (creditScore >= 620);

        // ═══════════════════════════════════════════════════════
        // VIOLATION: ZIP code used as eligibility factor
        // ZIP correlates with race (redlining proxy)
        // ═══════════════════════════════════════════════════════
        double zipRiskFactor = getZipCodeRiskFactor(zipCode);
        if (zipRiskFactor > 0.7) {
            // Effectively denying loans in minority neighborhoods
            eligible = false;
        }

        // ═══════════════════════════════════════════════════════
        // VIOLATION: Income source type used as decision factor
        // "Alimony/child support" correlates with sex/marital status
        // ECOA explicitly prohibits this (12 CFR 1002.6(b)(5))
        // ═══════════════════════════════════════════════════════
        if ("ALIMONY".equals(incomeSourceType) || "CHILD_SUPPORT".equals(incomeSourceType)) {
            // Penalizing income type that correlates with gender
            eligible = eligible && (dti <= 0.35); // Stricter DTI for these borrowers
        }

        // VIOLATION: Logging PII and decision factors
        System.out.println("Eligibility: borrower zip=" + zipCode +
                          " income=" + annualIncome + " dti=" + dti +
                          " source=" + incomeSourceType + " result=" + eligible);

        return new EligibilityResult(eligible, dti, zipRiskFactor);
    }

    /**
     * VIOLATION: ZIP code risk scoring.
     * This effectively implements digital redlining — assigning higher
     * risk to ZIP codes with predominantly minority populations.
     * 
     * Disparate impact: 78% of denied applications in high-risk ZIPs
     * belong to minority borrowers (would fail fair lending exam).
     */
    private double getZipCodeRiskFactor(String zipCode) {
        // These "high risk" ZIPs correlate with minority neighborhoods
        // This is illegal under ECOA + Fair Housing Act
        if (zipCode.startsWith("100") || zipCode.startsWith("112") ||
            zipCode.startsWith("303") || zipCode.startsWith("606")) {
            return 0.85; // "High risk" = predominantly minority area
        }
        return 0.30; // "Low risk" = predominantly non-minority area
    }

    /**
     * VIOLATION: Rate pricing with no fair lending controls.
     * 
     * Different rates for same credit profile based on factors
     * that correlate with protected characteristics. This would
     * fail a regression analysis in a fair lending exam.
     */
    public double calculateBorrowerRate(double baseRate, int creditScore,
                                        String zipCode, int borrowerAge) {

        double rate = baseRate;

        // Legitimate: credit-score-based pricing
        if (creditScore >= 760) rate -= 0.0050;
        else if (creditScore >= 700) rate -= 0.0025;
        else if (creditScore < 640) rate += 0.0100;

        // VIOLATION: Age-based pricing (ECOA prohibits age discrimination)
        if (borrowerAge > 62) {
            rate += 0.0075; // Higher rate for older borrowers
        }

        // VIOLATION: Geographic pricing that correlates with race
        double zipFactor = getZipCodeRiskFactor(zipCode);
        rate += zipFactor * 0.015; // Up to 1.5% higher in minority areas

        System.out.println("Rate: " + rate + " age=" + borrowerAge + " zip=" + zipCode);

        return rate;
    }

    // Inner class for result
    public static class EligibilityResult {
        public final boolean eligible;
        public final double dti;
        public final double riskFactor;

        public EligibilityResult(boolean eligible, double dti, double riskFactor) {
            this.eligible = eligible;
            this.dti = dti;
            this.riskFactor = riskFactor;
        }
    }
}
