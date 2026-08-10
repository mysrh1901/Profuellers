package com.mortgage.service;

/**
 * KAVACH AI — Clean baseline service
 * No violations in this file. Score stays green.
 */
public class Regulith {

    public double calculateMonthlyPayment(double principal, double rate, int years) {
        double monthlyRate = rate / 12.0;
        int payments = years * 12;
        return principal * (monthlyRate * Math.pow(1 + monthlyRate, payments))
                / (Math.pow(1 + monthlyRate, payments) - 1);
    }

    public double calculateDTI(double monthlyDebt, double monthlyIncome) {
        return monthlyDebt / monthlyIncome;
    }
}
