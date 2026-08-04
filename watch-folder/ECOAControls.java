package com.mortgage.controls;

/**
 * ═══════════════════════════════════════════════════════════════════
 * FAIR LENDING — ECOA / REGULATION B CONTROLS DEMO
 * Uncomment each block to trigger specific ECOA control violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  ECOA-PROXY-01  → ZIP Code Proxy    (Block A)
 *  ECOA-AGE-01    → Age Discrimination (Block B)
 *  ECOA-INCOME-01 → Income Source Bias (Block C)
 */
public class ECOAControls {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public boolean isEligible(int creditScore, double dti) {
        return creditScore >= 620 && dti <= 0.43;
    }


    // ─── BLOCK A: ECOA-PROXY-01 — ZIP Code as Race Proxy ───────────
    // Violates: ECOA Reg B 12 CFR 1002.6 — no race/national origin in decisions
    // ZIP code correlates with race = digital redlining
    // Impact: 78% of denials in "high-risk ZIPs" are minority borrowers
    // Penalty: DOJ civil rights action + CFPB enforcement + restitution

    // public boolean checkEligibilityWithZip(double income, int score, String zipCode) {
    //     // VIOLATION: ZIP code is a proxy for race/ethnicity
    //     // These ZIPs are predominantly minority neighborhoods
    //     if (zipCode.startsWith("100") || zipCode.startsWith("606")
    //         || zipCode.startsWith("303") || zipCode.startsWith("481")) {
    //         return false; // DIGITAL REDLINING
    //     }
    //     System.out.println("Eligibility check: zip=" + zipCode + " income=" + income);
    //     return score >= 620;
    // }


    // ─── BLOCK B: ECOA-AGE-01 — Age-Based Discrimination ───────────
    // Violates: ECOA prohibits age as negative factor in credit decisions
    // Impact: Older borrowers charged higher rates for same credit profile
    // Penalty: Class action + CFPB consent order + restitution

    // public double calculateRateWithAge(double baseRate, int creditScore, int age) {
    //     double rate = baseRate;
    //     if (creditScore >= 750) rate -= 0.005;
    //     // VIOLATION: Age used as pricing factor — ECOA prohibits this
    //     if (age > 62) {
    //         rate += 0.0075; // Charging elderly more = discrimination
    //     }
    //     if (age < 25) {
    //         rate += 0.005; // Charging young more = discrimination
    //     }
    //     System.out.println("Rate: " + rate + " age=" + age + " credit=" + creditScore);
    //     return rate;
    // }


    // ─── BLOCK C: ECOA-INCOME-01 — Income Source Discrimination ────
    // Violates: ECOA 12 CFR 1002.6(b)(5) — cannot discount income from
    //           alimony, child support, or public assistance
    // Impact: Women and single parents disproportionately denied
    // Penalty: Disparate impact finding + DOJ action

    // public boolean checkIncomeEligibility(double income, String incomeSource, double debt) {
    //     double dti = debt / (income / 12.0);
    //     // VIOLATION: Stricter DTI for alimony/support income
    //     // ECOA says this income MUST be counted equally
    //     if ("ALIMONY".equals(incomeSource) || "CHILD_SUPPORT".equals(incomeSource)) {
    //         if (dti > 0.35) return false; // Stricter threshold = discrimination
    //     }
    //     System.out.println("Income check: source=" + incomeSource + " dti=" + dti);
    //     return dti <= 0.43;
    // }
}
