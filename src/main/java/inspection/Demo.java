package inspection;

import inspection.model.*;
import inspection.visitor.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Demo {
    public static Directory buildFintechProject() {
        SourceCodeFile payment = new SourceCodeFile("PaymentService.java", 420, 14, false, true);
        SourceCodeFile user = new SourceCodeFile("UserService.java", 180, 9, false, false);
        TestFile paymentTest = new TestFile("PaymentServiceTest.java", 150, 12, "PaymentService.java");
        ConfigurationFile config = new ConfigurationFile("application.properties", new TreeMap<>(Map.of(
                "db.url", "jdbc:postgresql://localhost/pay",
                "db.password", "admin123",
                "debug", "true")));
        DependencyFile pom = new DependencyFile("pom.xml", List.of(
                new Dependency("org.springframework:spring-core", "5.3.20", false),
                new Dependency("log4j:log4j", "1.2.17", true),
                new Dependency("com.fasterxml.jackson.core:jackson-databind", null, false)));

        return new Directory("fintech-project",
                new Directory("src", payment, user),
                new Directory("tests", paymentTest),
                new Directory("config", config),
                pom);
    }

    public static void main(String[] args) {
        Directory project = buildFintechProject();

        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor stats = new ProjectStatisticsVisitor();
        QualityGateVisitor gate = new QualityGateVisitor();
        ComplianceVisitor compliance = new ComplianceVisitor();
        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();

        // the same structure, four different operations
        for (ProjectVisitor v : List.<ProjectVisitor>of(security, stats, gate, compliance, debt)) {
            project.accept(v);
        }

        System.out.println(security.report());
        System.out.println(stats.report());
        System.out.println(gate.report());
        System.out.println(compliance.report());
        System.out.println(debt.report());
    }
}
