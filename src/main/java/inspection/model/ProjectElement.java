package inspection.model;

import inspection.visitor.ProjectVisitor;

/** Element side of the Visitor pattern. */
public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}
