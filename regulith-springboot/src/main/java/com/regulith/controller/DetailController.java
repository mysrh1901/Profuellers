package com.regulith.controller;

import com.regulith.model.*;
import com.regulith.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Detail pages — drill-down views for risks, debt, chain reactions.
 * Clickable from the main dashboard cards.
 */
@Controller
@RequiredArgsConstructor
public class DetailController {

    private final ClientEngagementRepository clientRepo;
    private final ChainReactionResultRepository chainRepo;
    private final AuditNarrativeRepository narrativeRepo;
    private final ComplianceEventRepository eventRepo;

    @GetMapping("/detail/{engagementId}")
    @ResponseBody
    public String engagementDetail(@PathVariable String engagementId) {
        ClientEngagement client = clientRepo.findById(engagementId).orElse(null);
        if (client == null) return "Engagement not found";

        List<ChainReactionResult> chains = chainRepo.findByEngagementIdOrderByTimestampDesc(engagementId);
        List<AuditNarrative> narratives = narrativeRepo.findByEngagementIdOrderByGeneratedAtDesc(engagementId);
        List<ComplianceEvent> events = eventRepo.findByEngagementIdOrderByTimestampDesc(engagementId);

        // Risks breakdown
        long blocking = chains.stream().filter(ChainReactionResult::isBlocking).count();
        long critical = chains.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long high = chains.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        long medium = chains.stream().filter(c -> "MEDIUM".equals(c.getSeverity())).count();

        // Build chain items HTML
        StringBuilder chainHtml = new StringBuilder();
        for (ChainReactionResult cr : chains) {
            String sevColor = "CRITICAL".equals(cr.getSeverity()) ? "#f44336" :
                    "HIGH".equals(cr.getSeverity()) ? "#FF9800" : "#FFC107";
            String blockLabel = cr.isBlocking() ? "<span style='color:#f44336;font-weight:bold'> [BLOCKING]</span>" : "";
            chainHtml.append(String.format("""
                <div class="risk-item">
                    <div class="risk-header">
                        <span class="sev-dot" style="background:%s"></span>
                        <span class="risk-domain">%s</span>
                        <span class="risk-sev" style="color:%s">%s</span>
                        %s
                    </div>
                    <div class="risk-reason">%s</div>
                    <div class="risk-action">Action: %s</div>
                    <div class="risk-controls">Controls: %s | SLA: %s</div>
                </div>
                """, sevColor, cr.getDomain(), sevColor, cr.getSeverity(), blockLabel,
                cr.getReason(), cr.getActionRequired(), cr.getControlsAffected(), cr.getSla()));
        }

        // Build narrative HTML
        StringBuilder narHtml = new StringBuilder();
        for (AuditNarrative n : narratives) {
            narHtml.append(String.format("""
                <div class="narrative-card">
                    <div class="nar-header">Event #%d | %s | %s</div>
                    <pre class="nar-text">%s</pre>
                </div>
                """, n.getEventId(), n.getEventType(), n.getGeneratedAt(), n.getNarrativeText()));
        }

        // Build events HTML
        StringBuilder evtHtml = new StringBuilder();
        for (ComplianceEvent e : events) {
            evtHtml.append(String.format("""
                <div class="evt-item">
                    <span class="evt-type">%s</span>
                    <span class="evt-source">%s</span>
                    <span class="evt-desc">%s</span>
                    <span class="evt-time">%s</span>
                </div>
                """, e.getEventType(), e.getSource(), e.getDescription(),
                e.getTimestamp() != null ? e.getTimestamp().toString() : ""));
        }

        // Debt explanation
        String debtExplanation = buildDebtExplanation(client, chains);

        String scoreColor = client.getComplianceScore() >= 80 ? "#4CAF50" : client.getComplianceScore() >= 60 ? "#FF9800" : "#f44336";
        String noChainMsg = chains.isEmpty() ? "<p style='color:#666'>No chain reactions yet. Trigger an event to see results.</p>" : "";
        String noNarMsg = narratives.isEmpty() ? "<p style='color:#666'>No narratives yet. Trigger an event to generate one.</p>" : "";
        String noEvtMsg = events.isEmpty() ? "<p style='color:#666'>No events yet.</p>" : "";

        String chainContent = chainHtml.length() > 0 ? chainHtml.toString() : noChainMsg;
        String narContent = narHtml.length() > 0 ? narHtml.toString() : noNarMsg;
        String evtContent = evtHtml.length() > 0 ? evtHtml.toString() : noEvtMsg;

        // Calculate debt from actual findings (not seeded value)
        long critCount = chains.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long highCount = chains.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        long blockCount = chains.stream().filter(ChainReactionResult::isBlocking).count();
        long soxRisk = client.isSoxApplicable() ? 500 : 0;
        long calculatedDebtK = (critCount * 100) + (highCount * 25) + soxRisk + 50 + (blockCount * 25);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>KAVACH AI - Detail</title>");
        html.append("<style>");
        html.append("*{margin:0;padding:0;box-sizing:border-box}");
        html.append("body{font-family:-apple-system,sans-serif;background:#0a0e1a;color:#e0e0e0}");
        html.append(".header{background:linear-gradient(135deg,#1a1f36,#2d1b69);padding:20px 30px;border-bottom:3px solid #6c63ff}");
        html.append(".header h1{color:#fff;font-size:1.5em}.header a{color:#6c63ff;text-decoration:none;font-size:0.9em}");
        html.append(".container{max-width:1200px;margin:0 auto;padding:25px 20px}");
        html.append(".section{margin-bottom:35px}.section h2{color:#fff;font-size:1.2em;margin-bottom:5px}");
        html.append(".section .sub{color:#888;font-size:0.85em;margin-bottom:15px}");
        html.append(".stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:12px;margin-bottom:25px}");
        html.append(".stat{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:15px;text-align:center}");
        html.append(".stat-val{font-size:1.8em;font-weight:bold;color:#6c63ff}.stat-label{font-size:0.75em;color:#888;margin-top:3px}");
        html.append(".risk-item{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:14px 18px;margin-bottom:10px}");
        html.append(".risk-header{display:flex;align-items:center;gap:10px;margin-bottom:6px}");
        html.append(".sev-dot{width:10px;height:10px;border-radius:50%}.risk-domain{font-weight:600;color:#fff}");
        html.append(".risk-sev{font-size:0.8em;font-weight:bold}.risk-reason{color:#ccc;font-size:0.85em;margin-bottom:4px}");
        html.append(".risk-action{color:#4CAF50;font-size:0.8em}.risk-controls{color:#666;font-size:0.75em;margin-top:4px}");
        html.append(".debt-box{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:20px}");
        html.append(".debt-item{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #1a1f36}");
        html.append(".debt-label{color:#ccc}.debt-val{color:#FF9800;font-weight:bold}");
        html.append(".narrative-card{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:15px;margin-bottom:12px}");
        html.append(".nar-header{color:#6c63ff;font-size:0.8em;margin-bottom:8px}");
        html.append(".nar-text{color:#aaa;font-size:0.78em;white-space:pre-wrap;font-family:monospace;line-height:1.5;max-height:300px;overflow-y:auto}");
        html.append(".evt-item{background:#161b30;border:1px solid #2a2f4a;border-radius:6px;padding:10px 14px;margin-bottom:6px;display:flex;gap:12px;align-items:center}");
        html.append(".evt-type{background:#6c63ff;color:#fff;padding:2px 8px;border-radius:4px;font-size:0.7em;font-weight:bold}");
        html.append(".evt-source{color:#FF9800;font-size:0.8em}.evt-desc{color:#ccc;font-size:0.82em;flex:1}.evt-time{color:#666;font-size:0.72em}");
        html.append("</style></head><body>");
        html.append("<div class='header'><a href='/'>← Back to Dashboard</a><h1>").append(client.getClientName()).append(" — Compliance Detail</h1></div>");
        html.append("<div class='container'>");

        // Stats row
        html.append("<div class='stats'>");
        html.append("<div class='stat'><div class='stat-val' style='color:").append(scoreColor).append("'>").append(String.format("%.0f", client.getComplianceScore())).append("</div><div class='stat-label'>Compliance Score</div></div>");
        html.append("<div class='stat'><div class='stat-val' style='color:#f44336'>").append(client.getOpenRisks()).append("</div><div class='stat-label'>Open Risks</div></div>");
        html.append("<div class='stat'><div class='stat-val' style='color:#FF9800'>$").append(calculatedDebtK).append("K</div><div class='stat-label'>Compliance Debt</div></div>");
        html.append("<div class='stat'><div class='stat-val'>").append(client.getTrend()).append("</div><div class='stat-label'>Trend</div></div>");
        html.append("<div class='stat'><div class='stat-val' style='color:#f44336'>").append(blocking).append("</div><div class='stat-label'>Blocking Issues</div></div>");
        html.append("<div class='stat'><div class='stat-val'>").append(narratives.size()).append("</div><div class='stat-label'>Narratives</div></div>");
        html.append("</div>");

        // Risks section
        html.append("<div class='section'><h2>Open Risks Breakdown (").append(chains.size()).append(" findings)</h2>");
        html.append("<p class='sub'>Each triggered by a compliance event, evaluated by Chain Reactor Agent</p>");
        html.append(chainContent).append("</div>");

        // Debt section
        html.append("<div class='section'><h2>Compliance Debt Explained ($").append(calculatedDebtK).append("K)</h2>");
        html.append("<p class='sub'>Financial risk exposure from unresolved compliance gaps</p>");
        html.append("<div class='debt-box'>").append(debtExplanation).append("</div></div>");

        // Narratives section
        html.append("<div class='section'><h2>Auto-Generated Audit Narratives (").append(narratives.size()).append(")</h2>");
        html.append("<p class='sub'>Generated autonomously by Audit Narrator Agent — ready for EY/Deloitte</p>");
        html.append(narContent).append("</div>");

        // Events section
        html.append("<div class='section'><h2>Event History (").append(events.size()).append(" events)</h2>");
        html.append("<p class='sub'>All events that triggered agent processing</p>");
        html.append(evtContent).append("</div>");

        html.append("</div></body></html>");
        return html.toString();
    }

    private String buildDebtExplanation(ClientEngagement client, List<ChainReactionResult> chains) {
        long critCount = chains.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count();
        long highCount = chains.stream().filter(c -> "HIGH".equals(c.getSeverity())).count();
        long blockCount = chains.stream().filter(ChainReactionResult::isBlocking).count();
        long soxRisk = client.isSoxApplicable() ? 500 : 0;
        long total = (critCount * 100) + (highCount * 25) + soxRisk + 50 + (blockCount * 25);

        return "<div class='debt-item'><span class='debt-label'>Critical findings x $100K</span><span class='debt-val'>" + critCount + " x $100K = $" + (critCount*100) + "K</span></div>"
             + "<div class='debt-item'><span class='debt-label'>High findings x $25K</span><span class='debt-val'>" + highCount + " x $25K = $" + (highCount*25) + "K</span></div>"
             + "<div class='debt-item'><span class='debt-label'>SOX control deficiency risk</span><span class='debt-val'>$" + soxRisk + "K</span></div>"
             + "<div class='debt-item'><span class='debt-label'>SLA breach penalty exposure</span><span class='debt-val'>$50K</span></div>"
             + "<div class='debt-item'><span class='debt-label'>Blocking issues (delay cost)</span><span class='debt-val'>" + blockCount + " x $25K = $" + (blockCount*25) + "K</span></div>"
             + "<div class='debt-item' style='border-top:2px solid #6c63ff;margin-top:8px;padding-top:12px'><span class='debt-label' style='color:#fff;font-weight:bold'>Total Compliance Debt</span><span class='debt-val' style='color:#f44336;font-size:1.2em'>$" + total + "K</span></div>";
    }
}
