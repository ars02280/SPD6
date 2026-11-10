package inspection.visitor;

import inspection.model.*;
import java.util.Locale;

/** Release gate: composes the statistics and security visitors and applies thresholds. */
public class QualityGateVisitor extends BaseVisitor {
    private final ProjectStatisticsVisitor stats = new ProjectStatisticsVisitor();
    private final SecurityAuditVisitor security = new SecurityAuditVisitor();
    private final double maxComplexity;
    private final double minTestRatio;

    public QualityGateVisitor() { this(10.0, 0.40); }

    public QualityGateVisitor(double maxComplexity, double minTestRatio) {
        this.maxComplexity = maxComplexity;
        this.minTestRatio = minTestRatio;
    }

    @Override public void visit(SourceCodeFile f) { stats.visit(f); security.visit(f); }
    @Override public void visit(TestFile f) { stats.visit(f); security.visit(f); }
    @Override public void visit(ConfigurationFile f) { stats.visit(f); security.visit(f); }
    @Override public void visit(DependencyFile f) { stats.visit(f); security.visit(f); }
    // visit(Directory) inherited: only traverses, so nothing is counted twice.

    public boolean securityPassed() { return security.count(SecurityAuditVisitor.Severity.CRITICAL) == 0; }
    public boolean complexityPassed() { return stats.averageComplexity() <= maxComplexity; }
    public boolean testsPassed() { return stats.testSourceRatio() >= minTestRatio; }
    public boolean passed() { return securityPassed() && complexityPassed() && testsPassed(); }

    private static String s(boolean ok) { return ok ? "PASS" : "FAIL"; }

    public String report() {
        return String.format(Locale.ROOT, """
                === Quality Gate ===
                Security:   %s (critical issues: %d, max 0)
                Complexity: %s (avg %.2f, max %.1f)
                Tests:      %s (ratio %.2f, min %.2f)

                FINAL RESULT: %s
                """,
                s(securityPassed()), security.count(SecurityAuditVisitor.Severity.CRITICAL),
                s(complexityPassed()), stats.averageComplexity(), maxComplexity,
                s(testsPassed()), stats.testSourceRatio(), minTestRatio,
                passed() ? "PASSED" : "FAILED");
    }
}
