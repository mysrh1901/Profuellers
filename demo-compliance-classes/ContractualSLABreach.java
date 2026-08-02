package com.mortgage.compliance.contractual;

/**
 * ══════════════════════════════════════════════════════════════════════
 * DEMO CLASS: Contractual — MSA §7.2 Critical Vulnerability SLA
 * ══════════════════════════════════════════════════════════════════════
 * 
 * COMPLIANCE DOMAIN: Contractual — Master Service Agreement
 * CLAUSE: MSA §7.2 — Critical Vulnerability Remediation
 * SLA: Critical vulnerabilities must be remediated within 48 hours
 * PENALTY: $50,000 per incident + right to terminate engagement
 * 
 * VIOLATION SCENARIO:
 *   A critical SQL injection vulnerability is discovered by Snyk in
 *   the loan processing module. The 48-hour SLA clock starts. This
 *   class demonstrates the vulnerable code that would trigger the SLA.
 *   If not fixed within 48h, Hexaware faces $50K penalty AND the
 *   client can terminate the $4.2M/year engagement.
 * 
 * WHAT REGULITH AI DETECTS:
 *   → Critical vulnerability (SQL injection) — SLA clock starts
 *   → MSA §7.2 48-hour remediation window activated
 *   → $50,000 penalty exposure per incident
 *   → Client auto-notification triggered
 *   → Engagement termination risk flagged
 * 
 * CHAIN REACTION:
 *   Critical Finding → MSA §7.2 SLA Starts (48h) → Client Notified →
 *   Penalty Clock Running ($50K) → Escalation to Engagement Director →
 *   If breach: Termination Right Activated
 * ══════════════════════════════════════════════════════════════════════
 */
public class ContractualSLABreach {

    // VIOLATION: Production database credentials hardcoded
    // MSA §9.1 requires all credentials in approved vault
    private static final String PROD_DB_URL = "jdbc:oracle:thin:@10.0.1.50:1521:LOANPROD";
    private static final String PROD_DB_USER = "loan_svc_prod";
    private static final String PROD_DB_PASS = "Pr0d#L0an$2026!Critical";

    // MSA §12.1 Data Residency: US-only (AWS us-east-1, us-west-2)
    // VIOLATION: This endpoint is in eu-west-1
    private static final String ANALYTICS_ENDPOINT = "http://analytics.eu-west-1.internal.com/api/v2";

    /**
     * CRITICAL VULNERABILITY: SQL Injection
     * 
     * MSA §7.2 STATES:
     *   "All critical security vulnerabilities must be remediated
     *    within 48 hours of discovery. Failure to remediate within
     *    the SLA window shall result in a penalty of $50,000 per
     *    incident and shall grant Client the right to terminate
     *    this Agreement with 30 days notice."
     * 
     * This SQL injection in the loan search allows:
     *   - Unauthorized access to ALL loan records
     *   - Data exfiltration of borrower PII (SSN, income, assets)
     *   - Potential modification of loan terms
     * 
     * SLA CLOCK: 48 HOURS FROM DISCOVERY
     * PENALTY: $50,000
     * RISK: ENGAGEMENT TERMINATION ($4.2M/year revenue)
     */
    public String searchLoans(String borrowerName, String loanStatus) {
        // CRITICAL: SQL string concatenation — injection vulnerability
        String query = "SELECT * FROM LOANS WHERE borrower_name = '" + borrowerName +
                      "' AND status = '" + loanStatus + "'" +
                      " ORDER BY origination_date DESC";

        System.out.println("Executing loan search: " + query);

        // An attacker can input: borrowerName = "' OR 1=1; DROP TABLE LOANS; --"
        // This exposes ALL loan data and can destroy the database
        return query;
    }

    /**
     * VIOLATION: Missing encryption for data in transit.
     * 
     * MSA §8.3: "All data transmitted between systems must be
     * encrypted using TLS 1.2 or higher."
     * 
     * This method sends loan data over unencrypted HTTP.
     */
    public void sendLoanDataToAnalytics(String loanId, double amount, String borrowerSSN) {
        // VIOLATION: HTTP (not HTTPS) — data in transit unencrypted
        // MSA §8.3 requires TLS 1.2+
        String url = "http://analytics.internal.com/ingest";

        // VIOLATION: PII (SSN) sent in URL parameter — visible in logs
        String payload = url + "?loan=" + loanId + "&amount=" + amount + "&ssn=" + borrowerSSN;

        System.out.println("Sending to analytics: " + payload);

        // Also violates: PCI-DSS 4.1, GLBA Safeguards Rule
    }

    /**
     * VIOLATION: Logging sensitive data.
     * 
     * MSA §10.2: "Service Provider shall not store or log
     * personally identifiable information (PII) in application
     * logs, debug output, or error messages."
     */
    public void processLoanApplication(String applicantSSN, double income,
                                       String employer, double loanAmount) {

        // VIOLATION: Logging ALL PII — SSN, income, employer
        System.out.println("Processing application: SSN=" + applicantSSN +
                          " income=$" + income + " employer=" + employer +
                          " amount=$" + loanAmount);

        // MSA §10.2 breach: PII in logs
        // If these logs are in Splunk/ELK: data residency violation too
        // Cascading impact: GLBA, CCPA, potential GDPR (if EU borrower)
    }

    /**
     * VIOLATION: Data residency breach.
     * 
     * MSA §12.1: "All client data must remain within US boundaries
     * (AWS us-east-1 or us-west-2 only). Any data processing or
     * storage outside US territory is grounds for immediate termination."
     * 
     * This method sends data to EU region — contract termination trigger.
     */
    public void replicateToBackup(String loanData) {
        // VIOLATION: Sending to eu-west-1 — US-only requirement in MSA
        String destination = ANALYTICS_ENDPOINT; // EU endpoint!

        System.out.println("Replicating loan data to: " + destination);

        // MSA §12.1: Immediate termination right
        // Also triggers: GLBA data residency, State privacy laws
    }
}
