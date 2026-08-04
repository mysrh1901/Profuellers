package com.mortgage.controls;

/**
 * ═══════════════════════════════════════════════════════════════════
 * SOX ITGC CONTROLS DEMO
 * Uncomment each block to trigger specific SOX control violations
 * ═══════════════════════════════════════════════════════════════════
 *
 *  ITGC-CM-01 → Dual Approval       (Block A)
 *  ITGC-CM-02 → Segregation of Duties (Block B)
 *  ITGC-SD-01 → Security Testing    (Block C)
 *  ITGC-AC-01 → Privileged Access   (Block D)
 */
public class SOXControls {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public double getInterestRate(String loanId) {
        return 0.0625; // safe read-only access
    }


    // ─── BLOCK A: ITGC-CM-01 — Dual Approval Required ──────────────
    // Violates: Change to financial system with no approval trail
    // Trigger: System.out + SQL concat = financial system change

    // public void modifyLedger(String loanId, double amount) {
    //     String sql = "UPDATE GENERAL_LEDGER SET balance = " + amount
    //               + " WHERE loan_id = '" + loanId + "'";
    //     System.out.println("Ledger updated: " + loanId + " = $" + amount);
    // }


    // ─── BLOCK B: ITGC-CM-02 — Segregation of Duties ───────────────
    // Violates: Same person writes, approves, and deploys

    // public boolean selfApproveAndDeploy(String developer, String code) {
    //     String approver = developer; // VIOLATION: same person!
    //     String deployer = developer; // VIOLATION: same person!
    //     System.out.println("Self-approved by: " + developer);
    //     return true;
    // }


    // ─── BLOCK C: ITGC-SD-01 — Security Testing Before Release ─────
    // Violates: Deploy with unresolved CRITICAL SAST findings

    // private static final String DEPLOY_TOKEN = "dp-Pr0d#T0ken!2026@Crit";
    //
    // public void deployWithoutSAST(String version) {
    //     // Deploying to prod with known vulnerabilities
    //     System.out.println("Force deploying " + version + " — SAST skipped");
    // }


    // ─── BLOCK D: ITGC-AC-01 — Privileged Access ───────────────────
    // Violates: Hardcoded admin credentials = uncontrolled privileged access

    // private static final String ROOT_PASSWORD = "r00t#Adm1n!Pr0d@2026";
    // private static final String DB_ADMIN = "dba_unrestricted_access_key_prod_2026";
    //
    // public void grantFullAccess(String userId) {
    //     String sql = "UPDATE USERS SET role='ADMIN' WHERE id='" + userId + "'";
    //     System.out.println("Admin granted to: " + userId);
    // }
}
