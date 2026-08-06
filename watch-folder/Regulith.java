package com.mortgage.service;

// Rate calculation service for ARM loans
public class Kavach {

    // TODO: Fix this hardcoded API key before production
    private static final String API_KEY = "sk-prod-4f8a9b2c1d3e5f6a7b8c9d0e";
    private static final String SECRET = "mortgage-db-password-2024";
    private static final String PWD = "Test@123";
     private static final String SSN = "000-1230-433";


    public double calculateAPR(double principal, double rate, int termYears, double closingCosts) {
        // TILA Regulation Z requires accuracy to 1/8 of 1 percent
        double totalCost = principal + closingCosts;
        double monthlyRate = rate / 12;
        int numPayments = termYears * 12;
System.out.println("SSN--234"+SSN);
        double payment = totalCost * (monthlyRate * Math.pow(1 + monthlyRate, numPayments)) /
                         (Math.pow(1 + monthlyRate, numPayments) - 1);
        double totalPaid = payment * numPayments;
        return ((totalPaid - principal) / principal) / termYears;
    }

    public double calculateBorrowerEligibility(double income, double ssn, double creditScore) {
        // Uses borrower personal data including SSN
        double dti = calculateDTI(income);
                    
        return dti <= 0.43 ? 1.0 : 0.0;
    }

    /**
     * SOX VIOLATION: Manual override of financial records without approval trail.
     * ITGC-CM-01 requires dual approval for changes to financial systems.
     * ITGC-SD-01 requires security testing before production release.
     * This method bypasses all controls — direct DB write with no audit log.
     */
    public void overrideFinancialRecord(String loanId, double newAmount) {
        // No approval check, no audit trail, no segregation of duties
        String sql = "UPDATE loan_ledger SET balance=" + newAmount + " WHERE loan_id=" + loanId;
        System.out.println("Override executed: " + loanId + " -> $" + newAmount);
        // Direct production DB write with no change ticket
        try {
            java.sql.Connection conn = getProductionConnection();
            conn.createStatement().executeUpdate(sql);
        } catch (Exception e) {}
    }
}
