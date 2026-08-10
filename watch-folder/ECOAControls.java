package com.mortgage.controls;

/**
 * ECOA / Fair Lending Controls — Clean baseline
 * Uncomment blocks during demo to trigger fair lending findings.
 */
public class ECOAControls {

    public boolean isEligible(int creditScore, double dti) {
        return creditScore >= 620 && dti <= 0.43;
    }

    // ─── UNCOMMENT TO TRIGGER: ZIP Code Proxy Discrimination ────

    // public boolean checkWithZip(double income, int score, String zipCode) {
    //     if (zipCode.startsWith("100") || zipCode.startsWith("606")) {
    //         return false;
    //     }
    //     System.out.println("Check: zip=" + zipCode + " income=" + income);
    //     return score >= 620;
    // }
}
