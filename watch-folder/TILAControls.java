package com.mortgage.controls;

/**
 * TILA / Regulation Z Controls — Clean baseline
 * Uncomment blocks during demo to trigger TILA findings.
 */
public class TILAControls {

    public double getBaseRate() {
        return 0.0675;
    }

    // ─── UNCOMMENT TO TRIGGER: APR Calculation Error ────────────

    // public double calculateAPR(double principal, double rate, int years, double fees) {
    //     double apr = (principal * rate * years + fees) / principal / years;
    //     System.out.println("APR = " + apr);
    //     return apr;
    // }
}
