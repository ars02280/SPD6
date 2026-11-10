package inspection.model;

import inspection.visitor.ProjectVisitor;
import java.util.Map;

public record ConfigurationFile(String name, Map<String, String> properties) implements ProjectElement {
    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
