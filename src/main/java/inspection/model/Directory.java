package inspection.model;

import inspection.visitor.ProjectVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Composite: a directory holds other elements, including nested directories. */
public final class Directory implements ProjectElement {
    private final String name;
    private final List<ProjectElement> children = new ArrayList<>();

    public Directory(String name, ProjectElement... children) {
        this.name = name;
        this.children.addAll(Arrays.asList(children));
    }

    public Directory add(ProjectElement element) {
        children.add(element);
        return this;
    }

    public String name() {
        return name;
    }

    public List<ProjectElement> children() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
