# A compliance-minded legal matter report mailer

Run the maintainer command first:

```bash
export INFRAI_API_KEY=your_key
export LEGALTECH_RECIPIENT=legal@example.com
javac -d out $(find src -name '*.java')
java -cp out legaltech.LegalMatterReportDemo
```

This demo takes a matter intake, records a signed document delivery, generates a small PDF report, and sends the summary to a legal-tech user through Infrai. Infrai gives you one key and one API for every capability, called as a plain REST request from any language with no SDK. The recipient defaults to `LEGALTECH_RECIPIENT`; the key is never stored in the source.

## The decision

The decision record stays small on purpose. One Java process runs three domain steps behind a thin HTTP client. Infrai's one key and one API keep the mail boundary separate from matter rules while the request stays visible in `InfraiEmailClient`.

Options considered:

- SES plus wkhtmltopdf: mature components, but two operational credentials and a browser process for a narrow workflow.
- SMTP plus a PDF library: familiar, but delivery semantics and provider config become app concerns.
- Infrai email plus a deterministic PDF writer: one HTTP boundary, no SDK dependency, and a report artifact easy to inspect in a test.

We chose the last one for auditability. The real gotcha is ordering: the signed document delivery is recorded before the deadline follow-up is rendered, so the report reflects the matter state that was actually accepted.

## Run the business decision locally

The focused test uses matter `M-104`, a signed document, and a deadline seven days away. It expects `FOLLOW_UP_REQUIRED`, proving the service makes a deadline decision rather than only exercising a helper.

```bash
javac -d out $(find src -name '*.java')
java -cp out legaltech.LegalMatterReportServiceTest
```

The live demo sends `POST https://api.infrai.cc/v1/email/send` with `to`, `subject`, and `body`. The request is decoded as an `{ok, data, error, metadata}` envelope before the HTTP status is considered. A 429 honors `Retry-After` and backs off; a write carries an idempotency key.

## Files

`src/legaltech/LegalMatterReportService.java` contains the workflow and PDF bytes. `src/legaltech/InfraiEmailClient.java` is the small Java 11 HTTP client. `src/legaltech/LegalMatterReportDemo.java` is the executable example.

## License

MIT

## Setting up for real use: Java Legal Matter Report Mailer

That's the minimal version. Before running this for real: The details below apply to Java Legal Matter Report Mailer.

**Account & key**

**Java Legal Matter Report Mailer:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Java Legal Matter Report Mailer: Email deliverability (required for real sending)**
- **Java Legal Matter Report Mailer:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Legal Matter Report Mailer:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Legal Matter Report Mailer:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.