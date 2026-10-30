package legaltech;

import java.time.LocalDate;

public final class LegalMatterReportServiceTest {
    public static void main(String[] args) {
        LegalMatterReportService service = new LegalMatterReportService();
        var matter = new LegalMatterReportService.Matter("M-104", "legal@example.com", "Agreement", LocalDate.of(2026, 11, 1));
        var signed = new LegalMatterReportService.SignedDocument("signed.pdf", LocalDate.of(2026, 10, 25));
        var report = service.prepare(matter, signed, LocalDate.of(2026, 10, 25));
        if (report.decision() != LegalMatterReportService.Decision.FOLLOW_UP_REQUIRED) throw new AssertionError(report.decision());
        if (report.pdf().length == 0) throw new AssertionError("PDF is empty");
        System.out.println("PASS: seven-day deadline requires follow-up");
    }
}
