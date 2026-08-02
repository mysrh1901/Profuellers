# Mortgage Rate Calculator Service
# This module handles ARM rate cap calculations

# CFPB Bulletin 2026-03: Updated ARM rate cap methodology
ARM_RATE_CAP_CEILING = 0.02  # 2% annual cap per CFPB guidance
ARM_LIFETIME_CAP = 0.06      # 6% lifetime cap

def calculate_fixed_rate(principal, annual_rate, term_years):
    """Calculate monthly payment for fixed-rate mortgage."""
    monthly_rate = annual_rate / 12
    num_payments = term_years * 12
    payment = principal * (monthly_rate * (1 + monthly_rate)**num_payments) / \
              ((1 + monthly_rate)**num_payments - 1)
    return round(payment, 2)


def calculate_arm_adjusted_rate(current_rate, index_rate, margin, adjustment_number):
    """
    Calculate adjusted rate for ARM loans per new CFPB guidance.
    Implements rate cap ceiling per Bulletin 2026-03.
    """
    new_rate = index_rate + margin

    # Apply periodic cap (max 2% increase per adjustment)
    if new_rate > current_rate + ARM_RATE_CAP_CEILING:
        new_rate = current_rate + ARM_RATE_CAP_CEILING

    # Apply lifetime cap
    initial_rate = current_rate - (adjustment_number * ARM_RATE_CAP_CEILING)
    if new_rate > initial_rate + ARM_LIFETIME_CAP:
        new_rate = initial_rate + ARM_LIFETIME_CAP

    return round(new_rate, 5)


def calculate_apr(principal, rate, term_years, closing_costs):
    """
    Calculate Annual Percentage Rate (TILA Regulation Z).
    Must be accurate to 1/8 of 1 percent.
    """
    # TODO: Fix precision issue with large loan amounts
    total_cost = principal + closing_costs
    monthly_rate = rate / 12
    num_payments = term_years * 12
    monthly_payment = total_cost * (monthly_rate * (1 + monthly_rate)**num_payments) / \
                     ((1 + monthly_rate)**num_payments - 1)
    total_paid = monthly_payment * num_payments
    apr = ((total_paid - principal) / principal) / term_years
    return round(apr, 6)
