package inspection.visitor;

import inspection.model.*;
import java.util.*;

public class ComplianceVisitor extends BaseVisitor {
    private final Set<String> prohibited;
    private final List<String> violations = new ArrayList<>();
    private final List<SourceCodeFile> sources = new ArrayList<>();
    private final Set<String> testedClasses = new HashSet<>();

    public ComplianceVisitor() { this(Set.of("log4j:log4j", "com.google.guava:guava-jdk5")); }

    public ComplianceVisitor(Set<String> prohibitedDependencies) { this.prohibited = prohibitedDependencies; }

    @Override public void visit(SourceCodeFile f) {
        sources.add(f);
        if (f.hardcodedCredentials())
            violations.add("[NO_EXPOSED_CREDENTIALS] " + f.name() + ": hardcoded credentials");
    }

    @Override public void visit(TestFile f) { testedClasses.add(f.testedClass()); }

    @Override public void visit(ConfigurationFile f) {
        for (String key : SecretScanner.plaintextSecretKeys(f))
            violations.add("[NO_EXPOSED_CREDENTIALS] " + f.name() + ": exposed secret '" + key + "'");
    }

    @Override public void visit(DependencyFile f) {
        for (Dependency d : f.dependencies()) {
            if (!d.hasVersion())
                violations.add("[VERSION_REQUIRED] " + f.name() + ": " + d.name() + " has no version");
            if (prohibited.contains(d.name()))
                violations.add("[PROHIBITED_DEPENDENCY] " + f.name() + ": " + d.name());
        }
    }

    /** Computed lazily: "module has tests" can only be decided after the whole tree was visited. */
    public List<String> violations() {
        List<String> all = new ArrayList<>(violations);
        for (SourceCodeFile s : sources)
            if (!testedClasses.contains(s.name()))
                all.add("[TESTS_REQUIRED] " + s.name() + ": no test file found");
        return all;
    }

    public boolean isCompliant() { return violations().isEmpty(); }

    public String report() {
        StringBuilder sb = new StringBuilder("=== Compliance Check ===\n");
        List<String> v = violations();
        if (v.isEmpty()) sb.append("Compliant: no violations.\n");
        v.forEach(x -> sb.append(x).append('\n'));
        sb.append("Violations: ").append(v.size()).append('\n');
        return sb.toString();
    }
}
