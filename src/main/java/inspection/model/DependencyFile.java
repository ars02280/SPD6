package inspection.model;

import inspection.visitor.ProjectVisitor;
import java.util.List;

public record DependencyFile(String name, List<Dependency> dependencies) implements ProjectElement {
    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
