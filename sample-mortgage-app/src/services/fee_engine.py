# Fee Calculation Engine
# Computes origination fees, closing costs

def calculate_origination_fee(loan_amount, fee_percentage=0.01):
    """Standard origination fee calculation."""
    return round(loan_amount * fee_percentage, 2)
