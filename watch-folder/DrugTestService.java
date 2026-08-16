package com.biotech.lims;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  BIOTECH / PHARMA DEMO — FDA 21 CFR Part 11 Violations         ║
 * ║  Simulates a LIMS (Lab Information Management System)           ║
 * ║  Used by: Thermo Fisher, Pfizer, J&J for drug testing           ║
 * ║                                                                  ║
 * ║  Uncomment blocks to trigger FDA compliance violations          ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class DrugTestService {

    // ─── CLEAN BASELINE ─────────────────────────────────────────────
    public String getTestStatus(String sampleId) {
        return "PENDING";
    }


    // ─── BLOCK A: FDA 21 CFR 11.10(b) — Audit Trail Gap ────────────
    // Violation: overriding test result with no audit log

    // public void overrideResult(String sampleId, String newResult) {
    //     System.out.println("Override result: " + sampleId + " = " + newResult);
    // }


    // ─── BLOCK B: FDA 21 CFR 11.10(c) — Shared Account ─────────────
    // Violation: generic shared user account for lab analysts

    // private static final String lab_analyst_generic = "Sh@redL@b!2026Acc";
    //
    // public void submitWithSharedAccount(String sampleId) {
    //     System.out.println("Submitted by shared user account: " + sampleId);
    // }


    // ─── BLOCK C: FDA ALCOA+ — Manual Date Entry (Backdating) ───────
    // Violation: accepting manual timestamp instead of system-generated

    // public void recordResult(String sampleId, String result, String date) {
    //     // manual date input allows backdating of test records
    //     System.out.println("Recorded: " + sampleId + " date=" + date);
    // }


    // ─── BLOCK D: FDA 21 CFR 11.50 — E-Signature Without Auth ──────
    // Violation: batch release signed with just a name string, no verification

    // public void signBatchRelease(String batchId, String signerName) {
    //     System.out.println("Batch released, signature: " + signerName);
    // }

}
