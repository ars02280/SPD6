package inspection.model;

import inspection.visitor.ProjectVisitor;

/** @param testedClass file name of the source file this test covers */
public record TestFile(String name, int linesOfCode, int testCount, String testedClass) implements ProjectElement {
    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
