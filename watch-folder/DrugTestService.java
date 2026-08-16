package com.biotech.lims;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  BIOTECH DEMO FILE — FDA 21 CFR Part 11 Violations             ║
 * ║                                                                  ║
 * ║  This simulates a Lab Information Management System (LIMS)      ║
 * ║  used by companies like Thermo Fisher for drug testing.         ║
 * ║                                                                  ║
 * ║  Uncomment blocks to trigger FDA compliance violations:         ║
 * ║    Block A → Audit Trail Gap (CFR11-11.10b)                     ║
 * ║    Block B → Shared Account (CFR11-11.10c)                      ║
 * ║    Block C → Data Integrity (CFR11-DI-01)                       ║
 * ║    Block D → E-Signature Violation (CFR11-11.50)                ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class DrugTestService {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public String getTestResult(String sampleId) {
        return "PASSED";
    }

    public boolean isSampleValid(String batchId) {
        return true;
    }


    // ─── BLOCK A: CFR11-11.10b — Audit Trail Missing ────────────────
    // FDA requires: every data change must have immutable audit trail
    // Violation: modifying test results without logging who/when/why

    // public void overrideTestResult(String sampleId, String newResult) {
    //     // FDA VIOLATION: No audit trail — who changed this? when? why?
    //     // 21 CFR 11.10(b) requires computer-generated audit trail
    //     String sql = "UPDATE TEST_RESULTS SET result = '" + newResult
    //               + "' WHERE sample_id = '" + sampleId + "'";
    //     System.out.println("Result overridden: " + sampleId + " = " + newResult);
    //     // No timestamp, no user ID, no reason for change recorded
    // }


    // ─── BLOCK B: CFR11-11.10c — Shared/Generic Account ────────────
    // FDA requires: unique accounts per individual, no sharing
    // Violation: generic lab account used by multiple analysts

    // private static final String LAB_USER = "lab_analyst_generic";
    // private static final String LAB_PASS = "Lab#Shared!2026@Access";
    //
    // public void submitAnalysis(String sampleId, String result) {
    //     // FDA VIOLATION: Shared account — cannot attribute to individual
    //     // 21 CFR 11.10(c) requires access limited to authorized INDIVIDUALS
    //     System.out.println("Submitted by: " + LAB_USER + " sample: " + sampleId);
    //     // Which analyst actually ran this test? Audit cannot determine.
    // }


    // ─── BLOCK C: CFR11-DI-01 — Data Integrity (ALCOA+) ────────────
    // FDA requires: data must be Attributable, Legible, Contemporaneous,
    //               Original, Accurate (ALCOA+)
    // Violation: backdating results and no original record preserved

    // public void recordTestResult(String sampleId, String result, String date) {
    //     // FDA VIOLATION: Allowing manual date entry = backdating risk
    //     // ALCOA requires: Contemporaneous (recorded at time of activity)
    //     System.out.println("Test recorded: " + sampleId + " on " + date);
    //     // Original electronic record can be overwritten — no preservation
    //     // No hash/checksum to verify data wasn't altered
    // }


    // ─── BLOCK D: CFR11-11.50 — E-Signature Violation ──────────────
    // FDA requires: e-signatures unique to one individual, not shared
    // Violation: one person signs on behalf of another

    // public void signBatchRelease(String batchId, String signerName) {
    //     // FDA VIOLATION: No verification that signer is the actual person
    //     // 21 CFR 11.50 requires signature unique to individual
    //     // No two-factor auth for signing action
    //     System.out.println("Batch " + batchId + " released by: " + signerName);
    //     // Anyone can type any name — no identity verification
    //     // Signature not linked to specific electronic record
    // }
}
