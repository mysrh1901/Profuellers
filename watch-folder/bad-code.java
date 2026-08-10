package com.mortgage.service;

/**
 * Loan Processing Service — Clean baseline
 * This file has no violations. Uncomment blocks during demo to trigger findings.
 */
public class LoanProcessor {

    public double getFixedRate(int creditScore) {
        if (creditScore >= 750) return 0.0575;
        if (creditScore >= 700) return 0.0625;
        return 0.0675;
    }

    public boolean isEligible(int creditScore, double dti) {
        return creditScore >= 620 && dti <= 0.43;
    }

    // ─── UNCOMMENT DURING DEMO TO TRIGGER VIOLATIONS ────────────

    // public void searchLoans(String name) {
    //     String sql = "SELECT * FROM LOANS WHERE name = '" + name + "'";
    //     System.out.println("Executing: " + sql);
    // }
}
