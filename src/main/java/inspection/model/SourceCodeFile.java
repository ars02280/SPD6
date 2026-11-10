package inspection.model;

import inspection.visitor.ProjectVisitor;

public record SourceCodeFile(String name, int linesOfCode, int complexity,
                             boolean hardcodedCredentials, boolean highRisk) implements ProjectElement {
    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this); // static type is SourceCodeFile -> picks visit(SourceCodeFile): double dispatch
    }
}
