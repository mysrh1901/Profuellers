package com.regulith.controller;

import com.regulith.model.*;
import com.regulith.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ClientEngagementRepository clientRepo;
    private final ChainReactionResultRepository chainRepo;
    private final AuditNarrativeRepository narrativeRepo;

    @GetMapping("/")
    @ResponseBody
    public String home() {
        List<ClientEngagement> clients = clientRepo.findAll();
        List<ChainReactionResult> chains = chainRepo.findAll();
        List<AuditNarrative> narratives = narrativeRepo.findAll();

        StringBuilder cards = new StringBuilder();
        for (ClientEngagement c : clients) {
            String color = c.getComplianceScore() >= 80 ? "#4CAF50" : c.getComplianceScore() >= 60 ? "#FF9800" : "#f44336";
            // Calculate actual debt from findings
            List<ChainReactionResult> clientChains = chainRepo.findByEngagementIdOrderByTimestampDesc(c.getEngagementId());
            long crit = clientChains.stream().filter(x -> "CRITICAL".equals(x.getSeverity())).count();
            long high = clientChains.stream().filter(x -> "HIGH".equals(x.getSeverity())).count();
            long block = clientChains.stream().filter(ChainReactionResult::isBlocking).count();
            long debtK = (crit * 100) + (high * 25) + (c.isSoxApplicable() ? 500 : 0) + 50 + (block * 25);

            cards.append("<div class='card' onclick=\"window.location='/detail/").append(c.getEngagementId()).append("'\">");
            cards.append("<div class='card-header'><span class='client-name'>").append(c.getClientName()).append("</span>");
            cards.append("<span class='tier' style='background:").append(color).append("'>").append(c.getRiskTier()).append("</span></div>");
            cards.append("<div class='score' style='color:").append(color).append("'>").append(String.format("%.0f", c.getComplianceScore())).append("<span class='score-sub'>/100</span></div>");
            cards.append("<div class='meta'>");
            cards.append("<div><span class='label'>Open Risks</span><span class='val'>").append(clientChains.size()).append("</span></div>");
            cards.append("<div><span class='label'>Debt</span><span class='val'>$").append(debtK).append("K</span></div>");
            cards.append("<div><span class='label'>Trend</span><span class='val'>").append(c.getTrend()).append("</span></div>");
            cards.append("<div><span class='label'>Auditor</span><span class='val'>").append(c.getAuditor()).append("</span></div>");
            cards.append("</div>");
            cards.append("<div class='frameworks'>").append(c.getApplicableFrameworks()).append("</div>");
            cards.append("</div>");
        }

        StringBuilder chainHtml = new StringBuilder();
        if (chains.isEmpty()) {
            chainHtml.append("<p style='color:#888'>No events processed yet. Simulate one using the buttons above.</p>");
        } else {
            for (ChainReactionResult cr : chains) {
                String sevColor = "CRITICAL".equals(cr.getSeverity()) ? "#f44336" :
                        "HIGH".equals(cr.getSeverity()) ? "#FF9800" : "#FFC107";
                String blockTag = cr.isBlocking() ? " <span style='color:#f44336'>[BLOCKING]</span>" : "";
                chainHtml.append("<div class='chain-item'>");
                chainHtml.append("<span class='domain-badge' style='border-color:").append(sevColor).append("'>").append(cr.getDomain()).append("</span>");
                chainHtml.append("<span class='sev' style='color:").append(sevColor).append("'>[").append(cr.getSeverity()).append("]</span>");
                chainHtml.append(blockTag);
                chainHtml.append("<span class='reason'>").append(cr.getReason()).append("</span>");
                chainHtml.append("<div class='action'>Action: ").append(cr.getActionRequired()).append("</div>");
                chainHtml.append("</div>");
            }
        }

        StringBuilder narrativeHtml = new StringBuilder();
        if (narratives.isEmpty()) {
            narrativeHtml.append("<p style='color:#888'>No narratives generated yet.</p>");
        } else {
            for (AuditNarrative n : narratives) {
                narrativeHtml.append("<div class='nar-card'>");
                narrativeHtml.append("<div class='nar-head'>Event #").append(n.getEventId()).append(" | ").append(n.getEventType()).append("</div>");
                narrativeHtml.append("<pre class='nar-body'>").append(n.getNarrativeText().replace("<", "&lt;")).append("</pre>");
                narrativeHtml.append("</div>");
            }
        }
        // Build the page using StringBuilder (avoids String.format issues with % in content)
        StringBuilder page = new StringBuilder();
        page.append("<!DOCTYPE html><html><head><title>KAVACH AI</title><style>");
        page.append("*{margin:0;padding:0;box-sizing:border-box}");
        page.append("body{font-family:-apple-system,sans-serif;background:#0a0e1a;color:#e0e0e0}");
        page.append(".header{background:linear-gradient(135deg,#1a1f36,#2d1b69);padding:25px 40px;border-bottom:3px solid #6c63ff}");
        page.append(".header h1{color:#fff;font-size:1.8em}.header p{color:#a0a0cc;margin-top:5px}");
        page.append(".container{max-width:1200px;margin:0 auto;padding:30px 20px}");
        page.append(".section{margin-bottom:40px}.section h2{color:#fff;font-size:1.3em;margin-bottom:5px}");
        page.append(".section .sub{color:#888;font-size:0.9em;margin-bottom:15px}");
        page.append(".agent-tag{display:inline-block;background:rgba(108,99,255,0.15);border:1px solid #6c63ff;padding:3px 10px;border-radius:12px;font-size:0.72em;color:#b8b4ff;margin-left:10px}");
        page.append(".cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(340px,1fr));gap:20px}");
        page.append(".card{background:#161b30;border:1px solid #2a2f4a;border-radius:10px;padding:20px;cursor:pointer}");
        page.append(".card:hover{border-color:#6c63ff}");
        page.append(".card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:10px}");
        page.append(".client-name{color:#fff;font-weight:600}.tier{padding:3px 10px;border-radius:10px;font-size:0.7em;color:#fff;font-weight:bold}");
        page.append(".score{font-size:2.5em;font-weight:bold;margin:8px 0}.score-sub{font-size:0.35em;color:#666}");
        page.append(".meta{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin:12px 0}");
        page.append(".meta div{background:#1a1f36;padding:6px 10px;border-radius:5px}");
        page.append(".label{display:block;font-size:0.7em;color:#888}.val{font-size:0.95em;color:#fff}");
        page.append(".frameworks{font-size:0.75em;color:#6c63ff;margin-top:8px}");
        page.append(".chain-item{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:12px 16px;margin-bottom:10px}");
        page.append(".domain-badge{border:2px solid;padding:2px 8px;border-radius:5px;font-size:0.8em;font-weight:600;color:#fff;margin-right:8px}");
        page.append(".sev{font-size:0.8em;font-weight:bold;margin-right:8px}.reason{font-size:0.85em;color:#ccc}.action{font-size:0.8em;color:#4CAF50;margin-top:6px}");
        page.append(".nar-card{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:15px;margin-bottom:10px}");
        page.append(".nar-head{color:#6c63ff;font-size:0.8em;margin-bottom:8px}");
        page.append(".nar-body{color:#aaa;font-size:0.75em;white-space:pre-wrap;font-family:monospace;line-height:1.5;max-height:200px;overflow-y:auto}");
        page.append(".btn{display:inline-block;background:#6c63ff;color:#fff;padding:10px 20px;border-radius:6px;font-size:0.9em;margin-right:10px;margin-bottom:10px;cursor:pointer;border:none}");
        page.append(".btn:hover{background:#5a52e0}.btn-warn{background:#FF9800}");
        page.append(".info-bar{background:#161b30;border:1px solid #2a2f4a;border-radius:8px;padding:15px 20px;margin-bottom:25px;display:flex;gap:30px;flex-wrap:wrap}");
        page.append(".info-item{text-align:center}.info-val{font-size:1.5em;font-weight:bold;color:#6c63ff}.info-label{font-size:0.75em;color:#888}");
        page.append("</style></head><body>");
        page.append("<div class='header'><h1>KAVACH AI</h1><p>Knowledge-driven Audit, Vulnerability Analysis & Compliance Health (Spring Boot + ActiveMQ + H2)</p></div>");
        page.append("<div class='container'>");

        // Info bar
        page.append("<div class='info-bar'>");
        page.append("<div class='info-item'><div class='info-val'>").append(clients.size()).append("</div><div class='info-label'>Engagements</div></div>");
        page.append("<div class='info-item'><div class='info-val'>").append(chains.size()).append("</div><div class='info-label'>Chain Reactions</div></div>");
        page.append("<div class='info-item'><div class='info-val'>").append(narratives.size()).append("</div><div class='info-label'>Narratives</div></div>");
        page.append("<div class='info-item'><div class='info-val'>ActiveMQ</div><div class='info-label'>Message Queue</div></div>");
        page.append("<div class='info-item'><div class='info-val'>JMS</div><div class='info-label'>Event Bus</div></div>");
        page.append("</div>");

        // Simulate section
        page.append("<div class='section'><h2>Simulate Events</h2>");
        page.append("<p class='sub'>Click to push events through ActiveMQ — page reloads after 2 seconds to show results</p>");
        page.append("<button class='btn' onclick=\"simulate('code-commit')\">Code Commit (Financial)</button>");
        page.append("<button class='btn btn-warn' onclick=\"simulateWebhook('jenkins-deploy-blocked')\">Jenkins Deploy (SAST Failed)</button>");
        page.append("<button class='btn' onclick=\"simulateWebhook('docker-push')\">Docker Push</button>");
        page.append("<button class='btn btn-warn' onclick=\"simulateWebhook('aws-data-residency-violation')\">AWS Data Residency</button>");
        page.append("<button class='btn' onclick=\"simulateWebhook('aws-iam-change')\">IAM Change</button>");
        page.append("</div>");

        // Cards
        page.append("<div class='section'><h2>Compliance Digital Twins <span class='agent-tag'>Digital Twin Agent</span></h2>");
        page.append("<p class='sub'>Click any card to drill down into risks, debt breakdown, and audit narratives</p>");
        page.append("<div class='cards'>").append(cards).append("</div></div>");

        // Chain reactions
        page.append("<div class='section'><h2>Chain Reaction Results <span class='agent-tag'>Chain Reactor Agent via ActiveMQ</span></h2>");
        page.append("<p class='sub'>Cross-domain compliance impact from processed events</p>");
        page.append(chainHtml).append("</div>");

        // Narratives
        page.append("<div class='section'><h2>Audit Narratives <span class='agent-tag'>Audit Narrator Agent</span></h2>");
        page.append("<p class='sub'>Auto-generated evidence for auditors</p>");
        page.append(narrativeHtml).append("</div>");

        page.append("</div>");
        page.append("<script>");
        page.append("function simulate(t){fetch('/api/events/simulate/'+t,{method:'POST'}).then(()=>setTimeout(()=>location.reload(),2000))}");
        page.append("function simulateWebhook(t){fetch('/api/webhooks/simulate/'+t,{method:'POST'}).then(()=>setTimeout(()=>location.reload(),2000))}");
        page.append("</script></body></html>");

        return page.toString();
    }
}
