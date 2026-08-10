package com.mortgage.controls;

/**
 * Contractual / MSA Controls — Clean baseline
 * Uncomment blocks during demo to trigger contractual findings.
 */
public class ContractualControls {

    public String getLoanId(String reference) {
        return "LOAN-" + reference.hashCode();
    }

    // ─── UNCOMMENT TO TRIGGER: SQL Injection + MSA SLA ──────────

    // public String searchBorrowers(String name, String status) {
    //     String query = "SELECT * FROM BORROWERS WHERE name = '" + name
    //                  + "' AND status = '" + status + "'";
    //     System.out.println("Search: " + query);
    //     return query;
    // }
}
