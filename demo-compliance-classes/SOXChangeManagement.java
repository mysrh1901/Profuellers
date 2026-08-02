package com.mortgage.compliance.sox;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: SOX ITGC — Change Management Violation
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: SOX Section 404 (ITGC Change Management)
 * CONTROL: ITGC-CM-01 — Dual Approval Required
 * 
 * VIOLATION SCENARIO:
 *   A developer pushes a change to a financially-significant system
 *   (loan interest calculation) WITHOUT dual approval. This bypasses
 *   the SOX ITGC control requiring both Dev Lead and Release Manager
 *   sign-off before production deployment.
 * 
 * WHAT REGULITH AI DETECTS:
 *   → Change to financial calculation logic
 *   → No approval trail (missing PR reviewers)
 *   → Direct commit to main branch (bypasses gate)
 *   → SOX Material Weakness risk flagged
 * 
 * CHAIN REACTION:
 *   Code Commit → SOX Control Breach → Audit Finding → Material Weakness
 * ══════════════════════════════════════════════════════════════════════
 */
public class SOXChangeManagement {

    // VIOLATION: Hardcoded configuration for a financially-significant system
    // SOX requires all changes be tracked and approved
    private static final String DB_CONNECTION = "jdbc:oracle:thin:@prod-financial-db:1521:LOANCORE";
    private static final String ADMIN_PASS = "Fin@ncial#2026!Prod";

    // This rate directly impacts loan APR calculations (SOX-critical)
    private double baseInterestRate = 0.0675;
    private double adjustmentFactor = 1.025;

    /**
     * VIOLATION: Modifying financially-significant calculation without approval.
     * SOX ITGC-CM-01 requires dual approval for changes to systems that
     * affect financial reporting.
     * 
     * This method calculates interest accrual that flows directly into
     * quarterly financial statements (10-Q/10-K filings).
     */
    public double calculateInterestAccrual(double principal, int daysElapsed) {
        // Changed from 365 to 360 (banker's year) — this changes ALL loan calculations
        // NO CHANGE TICKET, NO APPROVAL, NO TESTING
        double dailyRate = baseInterestRate / 360;  // Was /365 — material financial impact
        double accrual = principal * dailyRate * daysElapsed * adjustmentFactor;

        // VIOLATION: System.out bypasses audit trail
        System.out.println("Accrual calculated: " + accrual + " for principal: " + principal);

        return accrual;
    }

    /**
     * VIOLATION: Direct database update to financial system without change record.
     * SOX requires segregation of duties — developer cannot deploy own changes.
     */
    public void updateRateTable(String rateType, double newRate) {
        // Direct SQL manipulation of production rate table
        String sql = "UPDATE INTEREST_RATES SET rate_value = " + newRate +
                     " WHERE rate_type = '" + rateType + "'";

        System.out.println("Executing rate update: " + sql);

        // No approval check, no audit log, no rollback capability
        // This would trigger SOX Material Weakness finding in annual audit
    }

    /**
     * VIOLATION: Bypass of approval workflow.
     * In production, this would skip the dual-approval gate entirely.
     */
    public boolean deployToProduction(String changeId, String deployerName) {
        // SOX requires: Developer != Approver != Deployer
        // Here: same person does all three
        boolean approved = true; // Hardcoded bypass!
        
        if (approved) {
            System.out.println("DEPLOYED by " + deployerName + " — no secondary approval");
        }
        return approved;
    }
}
