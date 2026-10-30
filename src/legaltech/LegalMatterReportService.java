package legaltech;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class LegalMatterReportService {
    public enum Decision { FOLLOW_UP_REQUIRED, MONITOR }

    public record Matter(String id, String userEmail, String title, LocalDate deadline) {}
    public record SignedDocument(String name, LocalDate deliveredOn) {}
    public record Report(Matter matter, SignedDocument document, Decision decision, byte[] pdf) {}

    public Report prepare(Matter matter, SignedDocument document, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, matter.deadline());
        Decision decision = days <= 7 ? Decision.FOLLOW_UP_REQUIRED : Decision.MONITOR;
        String text = "Matter: " + matter.id() + "\nTitle: " + matter.title()
                + "\nSigned document: " + document.name() + "\nDecision: " + decision;
        return new Report(matter, document, decision, pdf(text));
    }

    public String emailBody(Report report) {
        return "Legal matter report " + report.matter().id() + "\n"
                + "Signed document delivered: " + report.document().name() + "\n"
                + "Deadline decision: " + report.decision() + "\n"
                + "PDF report generated for review.";
    }

    private static byte[] pdf(String text) {
        String safe = text.replace("(", "\\(").replace(")", "\\)").replace("\n", ") Tj 0 -16 Td (");
        String body = "BT /F1 11 Tf 72 720 Td (" + safe + ") Tj ET";
        return ("%PDF-1.4\n1 0 obj<< /Type /Catalog /Pages 2 0 R>>endobj\n"
                + "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1>>endobj\n"
                + "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R>>endobj\n"
                + "4 0 obj<< /Length " + body.length() + ">>stream\n" + body
                + "\nendstream endobj\n%%EOF\n").getBytes(StandardCharsets.US_ASCII);
    }
}
