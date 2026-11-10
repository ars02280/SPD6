package inspection;

import inspection.model.*;
import inspection.visitor.*;
import inspection.visitor.SecurityAuditVisitor.Severity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectVisitorTest {

    private static Directory cleanProject() {
        return new Directory("clean",
                new Directory("src", new SourceCodeFile("A.java", 100, 4, false, false)),
                new Directory("tests", new TestFile("ATest.java", 50, 5, "A.java")),
                new ConfigurationFile("app.properties", Map.of("db.password", "${DB_PASSWORD}")),
                new DependencyFile("pom.xml", List.of(new Dependency("org.x:lib", "1.0", false))));
    }

    @Test
    void doubleDispatchRoutesEachElementToItsOwnOverload() {
        List<String> calls = new ArrayList<>();
        ProjectVisitor recorder = new ProjectVisitor() {
            public void visit(SourceCodeFile f) { calls.add("source"); }
            public void visit(TestFile f) { calls.add("test"); }
            public void visit(ConfigurationFile f) { calls.add("config"); }
            public void visit(DependencyFile f) { calls.add("dependency"); }
            public void visit(Directory d) { calls.add("directory"); }
        };
        new SourceCodeFile("A.java", 1, 1, false, false).accept(recorder);
        new TestFile("T.java", 1, 1, "A.java").accept(recorder);
        new ConfigurationFile("c", Map.of()).accept(recorder);
        new DependencyFile("pom", List.of()).accept(recorder);
        new Directory("d").accept(recorder);
        assertEquals(List.of("source", "test", "config", "dependency", "directory"), calls);
    }

    @Test
    void securityDetectsAllIssueKindsInFintechProject() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        Demo.buildFintechProject().accept(v);
        assertEquals(2, v.count(Severity.CRITICAL)); // plaintext password + vulnerable log4j
        assertEquals(1, v.count(Severity.HIGH));     // PaymentService
        assertEquals(1, v.count(Severity.MEDIUM));   // debug=true
        assertEquals(35, v.score());
    }

    @Test
    void securityFlagsHardcodedCredentialsInSource() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new SourceCodeFile("Bad.java", 10, 1, true, false).accept(v);
        assertEquals(1, v.count(Severity.CRITICAL));
        assertEquals(75, v.score());
    }

    @Test
    void securityIgnoresPlaceholderSecretsAndScoresCleanProject() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        cleanProject().accept(v);
        assertTrue(v.findings().isEmpty());
        assertEquals(100, v.score());
    }

    @Test
    void securityScoreNeverGoesBelowZero() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        Directory d = new Directory("many");
        for (int i = 0; i < 6; i++) d.add(new SourceCodeFile("F" + i, 1, 1, true, false));
        d.accept(v);
        assertEquals(0, v.score());
    }

    @Test
    void statisticsOnFintechProject() {
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        Demo.buildFintechProject().accept(v);
        assertEquals(5, v.totalFiles());
        assertEquals(750, v.linesOfCode());
        assertEquals(12, v.testCount());
        assertEquals(3, v.dependencyCount());
        assertEquals(11.5, v.averageComplexity(), 0.0001);
        assertEquals(0.5, v.testSourceRatio(), 0.0001);
    }

    @Test
    void statisticsTraverseDeeplyNestedDirectories() {
        Directory deep = new Directory("a", new Directory("b", new Directory("c",
                new SourceCodeFile("Deep.java", 10, 2, false, false))));
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor();
        deep.accept(v);
        assertEquals(3, v.directories());
        assertEquals(1, v.sourceFiles());
        assertEquals(10, v.linesOfCode());
    }

    @Test
    void emptyDirectoryProducesZeroStatisticsAndPerfectSecurity() {
        ProjectStatisticsVisitor stats = new ProjectStatisticsVisitor();
        SecurityAuditVisitor sec = new SecurityAuditVisitor();
        Directory empty = new Directory("empty");
        empty.accept(stats);
        empty.accept(sec);
        assertEquals(0, stats.totalFiles());
        assertEquals(1, stats.directories());
        assertEquals(100, sec.score());
    }

    @Test
    void edgeCaseNoSourceFilesAvoidsDivisionByZeroAndFailsTestGate() {
        QualityGateVisitor gate = new QualityGateVisitor();
        new Directory("tests-only", new TestFile("T.java", 10, 1, "X.java")).accept(gate);
        assertTrue(gate.complexityPassed());
        assertFalse(gate.testsPassed());
        assertFalse(gate.passed());
    }

    @Test
    void qualityGatePassesOnCleanProject() {
        QualityGateVisitor gate = new QualityGateVisitor();
        cleanProject().accept(gate);
        assertTrue(gate.passed());
        assertTrue(gate.report().contains("FINAL RESULT: PASSED"));
    }

    @Test
    void qualityGateFailsOnFintechProjectWithDetailedReasons() {
        QualityGateVisitor gate = new QualityGateVisitor();
        Demo.buildFintechProject().accept(gate);
        assertFalse(gate.securityPassed());
        assertFalse(gate.complexityPassed());
        assertTrue(gate.testsPassed());
        assertTrue(gate.report().contains("FINAL RESULT: FAILED"));
    }

    @Test
    void qualityGateFailsWhenTestRatioTooLow() {
        Directory p = new Directory("p",
                new SourceCodeFile("A.java", 10, 1, false, false),
                new SourceCodeFile("B.java", 10, 1, false, false),
                new SourceCodeFile("C.java", 10, 1, false, false),
                new TestFile("ATest.java", 10, 1, "A.java"));
        QualityGateVisitor gate = new QualityGateVisitor();
        p.accept(gate);
        assertTrue(gate.securityPassed());
        assertTrue(gate.complexityPassed());
        assertFalse(gate.testsPassed()); // 1/3 = 0.33 < 0.40
    }

    @Test
    void complianceReportsAllViolationsInFintechProject() {
        ComplianceVisitor v = new ComplianceVisitor();
        Demo.buildFintechProject().accept(v);
        List<String> violations = v.violations();
        assertEquals(4, violations.size());
        assertTrue(violations.stream().anyMatch(s -> s.startsWith("[NO_EXPOSED_CREDENTIALS]")));
        assertTrue(violations.stream().anyMatch(s -> s.startsWith("[PROHIBITED_DEPENDENCY]")));
        assertTrue(violations.stream().anyMatch(s -> s.startsWith("[VERSION_REQUIRED]")));
        assertTrue(violations.stream().anyMatch(s -> s.contains("UserService.java")));
        assertFalse(v.isCompliant());
    }

    @Test
    void complianceAcceptsCleanProject() {
        ComplianceVisitor v = new ComplianceVisitor();
        cleanProject().accept(v);
        assertTrue(v.isCompliant());
    }

    @Test
    void technicalDebtEstimatesEffortForFintechProject() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor();
        Demo.buildFintechProject().accept(v);
        // complexity 14 -> 120, large file -> 60, high risk -> 90, unpinned jackson -> 15, log4j upgrade -> 60
        assertEquals(345, v.totalMinutes());
        assertEquals(5, v.items().size());
    }

    @Test
    void technicalDebtIsZeroForCleanProject() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor();
        cleanProject().accept(v);
        assertEquals(0, v.totalMinutes());
    }
}
