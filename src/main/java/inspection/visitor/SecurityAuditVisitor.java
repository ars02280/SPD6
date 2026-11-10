package inspection.visitor;

import inspection.model.*;
import java.util.ArrayList;
import java.util.List;

public class SecurityAuditVisitor extends BaseVisitor {
    public enum Severity { CRITICAL, HIGH, MEDIUM }

    public record Finding(Severity severity, String location, String message) { }

    private final List<Finding> findings = new ArrayList<>();

    @Override public void visit(SourceCodeFile f) {
        if (f.hardcodedCredentials()) add(Severity.CRITICAL, f.name(), "Hardcoded credentials in source");
        if (f.highRisk()) add(Severity.HIGH, f.name(), "High-risk source file (unsafe API usage)");
    }

    @Override public void visit(ConfigurationFile f) {
        for (String key : SecretScanner.plaintextSecretKeys(f))
            add(Severity.CRITICAL, f.name(), "Plaintext secret in key '" + key + "'");
        for (String s : SecretScanner.insecureSettings(f))
            add(Severity.MEDIUM, f.name(), "Insecure configuration: " + s);
    }

    @Override public void visit(DependencyFile f) {
        for (Dependency d : f.dependencies())
            if (d.knownVulnerable())
                add(Severity.CRITICAL, f.name(), "Vulnerable dependency " + d.name() + ":" + d.version());
    }

    private void add(Severity s, String loc, String msg) { findings.add(new Finding(s, loc, msg)); }

    public List<Finding> findings() { return List.copyOf(findings); }

    public long count(Severity s) { return findings.stream().filter(f -> f.severity() == s).count(); }

    /** 100 minus penalties: critical 25, high 10, medium 5 (floored at 0). */
    public int score() {
        return (int) Math.max(0, 100 - 25 * count(Severity.CRITICAL) - 10 * count(Severity.HIGH) - 5 * count(Severity.MEDIUM));
    }

    public String report() {
        StringBuilder sb = new StringBuilder("=== Security Audit ===\n");
        if (findings.isEmpty()) sb.append("No issues found.\n");
        findings.forEach(f -> sb.append(String.format("[%s] %s: %s%n", f.severity(), f.location(), f.message())));
        sb.append("Security score: ").append(score()).append("/100\n");
        return sb.toString();
    }
}
