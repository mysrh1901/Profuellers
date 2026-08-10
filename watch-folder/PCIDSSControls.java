package com.mortgage.controls;

/**
 * PCI-DSS Controls — Clean baseline
 * Uncomment blocks during demo to trigger PCI-DSS findings.
 */
public class PCIDSSControls {

    public String getPaymentStatus(String transactionId) {
        return "COMPLETED";
    }

    // ─── UNCOMMENT TO TRIGGER: CVV Storage + Weak Crypto ────────

    // private String storedCVV;
    //
    // public void capturePayment(String pan, String cvv, double amount) {
    //     this.storedCVV = cvv;
    //     System.out.println("Payment: card=" + pan + " cvv=" + cvv);
    //     String hash = "DES:" + pan.hashCode();
    // }
}
