package inspection.visitor;

import inspection.model.*;

/** Visitor side: one overload per concrete element type (no instanceof anywhere). */
public interface ProjectVisitor {
    void visit(SourceCodeFile file);
    void visit(TestFile file);
    void visit(ConfigurationFile file);
    void visit(DependencyFile file);
    void visit(Directory directory);
}
