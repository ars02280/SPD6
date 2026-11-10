package inspection.visitor;

import inspection.model.*;

/** No-op defaults + recursive traversal of directories, so concrete visitors override only what they need. */
public abstract class BaseVisitor implements ProjectVisitor {
    @Override public void visit(SourceCodeFile file) { }
    @Override public void visit(TestFile file) { }
    @Override public void visit(ConfigurationFile file) { }
    @Override public void visit(DependencyFile file) { }

    @Override
    public void visit(Directory directory) {
        for (ProjectElement child : directory.children()) {
            child.accept(this);
        }
    }
}
