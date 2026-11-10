package inspection.visitor;

import inspection.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Estimates remediation effort in minutes. Added without touching any ProjectElement class. */
public class TechnicalDebtVisitor extends BaseVisitor {
    public record DebtItem(String location, String reason, int minutes) { }

    private final List<DebtItem> items = new ArrayList<>();

    @Override public void visit(SourceCodeFile f) {
        if (f.complexity() > 10) add(f.name(), "Complexity " + f.complexity() + " > 10", (f.complexity() - 10) * 30);
        if (f.linesOfCode() > 300) add(f.name(), "Large file (" + f.linesOfCode() + " LOC)", 60);
        if (f.highRisk()) add(f.name(), "High-risk code needs rework", 90);
        if (f.hardcodedCredentials()) add(f.name(), "Hardcoded credentials", 120);
    }

    @Override public void visit(DependencyFile f) {
        for (Dependency d : f.dependencies()) {
            if (!d.hasVersion()) add(f.name(), d.name() + ": pin a version", 15);
            if (d.knownVulnerable()) add(f.name(), d.name() + ": upgrade vulnerable dependency", 60);
        }
    }

    private void add(String loc, String reason, int min) { items.add(new DebtItem(loc, reason, min)); }

    public List<DebtItem> items() { return List.copyOf(items); }

    public int totalMinutes() { return items.stream().mapToInt(DebtItem::minutes).sum(); }

    public String report() {
        StringBuilder sb = new StringBuilder("=== Technical Debt ===\n");
        items.forEach(i -> sb.append(String.format("%s: %s (%d min)%n", i.location(), i.reason(), i.minutes())));
        sb.append(String.format(Locale.ROOT, "Total debt: %d min (%.2f h)%n", totalMinutes(), totalMinutes() / 60.0));
        return sb.toString();
    }
}
