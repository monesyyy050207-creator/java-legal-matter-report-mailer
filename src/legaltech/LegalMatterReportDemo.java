package legaltech;

import java.time.LocalDate;

public final class LegalMatterReportDemo {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        String recipient = System.getenv().getOrDefault("LEGALTECH_RECIPIENT", "legal@example.com");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");

        LegalMatterReportService service = new LegalMatterReportService();
        LegalMatterReportService.Matter matter = new LegalMatterReportService.Matter(
                "M-104", recipient, "Vendor agreement review", LocalDate.now().plusDays(5));
        LegalMatterReportService.SignedDocument signed = new LegalMatterReportService.SignedDocument(
                "vendor-agreement-signed.pdf", LocalDate.now());
        LegalMatterReportService.Report report = service.prepare(matter, signed, LocalDate.now());
        String response = new InfraiEmailClient(key).send(recipient,
                "Matter " + matter.id() + " report", service.emailBody(report));
        System.out.println("Report generated: " + report.pdf().length + " bytes");
        System.out.println("Email envelope: " + response);
    }
}
