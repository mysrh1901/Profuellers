# Borrower Eligibility Service
# Determines if a borrower qualifies for a loan product

def check_dti_ratio(monthly_income, monthly_debt, proposed_payment):
    """Check Debt-to-Income ratio for loan eligibility."""
    total_debt = monthly_debt + proposed_payment
    dti = total_debt / monthly_income
    return dti <= 0.43  # Standard DTI limit


def check_borrower_eligibility(borrower_profile, loan_product):
    """
    Full eligibility check combining DTI, credit score, and LTV.
    Uses borrower income and employment data for qualification.
    """
    # Credit score threshold
    if borrower_profile['credit_score'] < loan_product['min_credit_score']:
        return False, "Credit score below minimum"

    # DTI check using borrower income
    dti_ok = check_dti_ratio(
        borrower_profile['monthly_income'],
        borrower_profile['monthly_debt'],
        loan_product['estimated_payment']
    )
    if not dti_ok:
        return False, "DTI ratio exceeds 43%"

    # LTV check
    ltv = loan_product['loan_amount'] / borrower_profile['property_value']
    if ltv > loan_product['max_ltv']:
        return False, "LTV exceeds maximum"

    return True, "Eligible"
