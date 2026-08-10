package com.mortgage.controls;

/**
 * SOX ITGC Controls — Clean baseline (all violations commented out)
 * Uncomment blocks during demo to trigger SOX findings.
 */
public class SOXControls {

    public double getInterestRate(String loanId) {
        return 0.0625;
    }

    // ─── UNCOMMENT TO TRIGGER: ITGC-CM-01 Dual Approval ────────

    // public void modifyLedger(String loanId, double amount) {
    //     String sql = "UPDATE GENERAL_LEDGER SET balance = " + amount
    //               + " WHERE loan_id = '" + loanId + "'";
    //     System.out.println("Ledger updated: " + loanId + " = $" + amount);
    // }
}
