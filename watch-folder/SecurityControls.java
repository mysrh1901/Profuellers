package com.mortgage.controls;

/**
 * Security Controls — Clean baseline (all violations commented out)
 * Uncomment blocks during demo to trigger security findings.
 */
public class SecurityControls {

    public String getLoanStatus(String loanId) {
        return "ACTIVE";
    }

    // ─── UNCOMMENT TO TRIGGER: Hardcoded Secret ─────────────────

    // private static final String DB_PASS = "M0rtg@ge#Pr0d!2026$Critical";
    //
    // public void connectToDatabase() {
    //     System.out.println("Connecting with: " + DB_PASS.substring(0, 5) + "...");
    // }
}
