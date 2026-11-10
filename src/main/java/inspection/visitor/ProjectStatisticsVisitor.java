package inspection.visitor;

import inspection.model.*;
import java.util.Locale;

public class ProjectStatisticsVisitor extends BaseVisitor {
    private int directories, sourceFiles, testFiles, configFiles, dependencyFiles;
    private int dependencies, linesOfCode, tests, complexitySum;

    @Override public void visit(SourceCodeFile f) {
        sourceFiles++; linesOfCode += f.linesOfCode(); complexitySum += f.complexity();
    }
    @Override public void visit(TestFile f) {
        testFiles++; linesOfCode += f.linesOfCode(); tests += f.testCount();
    }
    @Override public void visit(ConfigurationFile f) { configFiles++; }
    @Override public void visit(DependencyFile f) {
        dependencyFiles++; dependencies += f.dependencies().size();
    }
    @Override public void visit(Directory d) {
        directories++;
        super.visit(d);
    }

    public int totalFiles() { return sourceFiles + testFiles + configFiles + dependencyFiles; }
    public int directories() { return directories; }
    public int sourceFiles() { return sourceFiles; }
    public int testFiles() { return testFiles; }
    public int linesOfCode() { return linesOfCode; }
    public int testCount() { return tests; }
    public int dependencyCount() { return dependencies; }
    public double averageComplexity() { return sourceFiles == 0 ? 0 : (double) complexitySum / sourceFiles; }
    /** Test files per source file; 0 when there are no sources (avoids division by zero). */
    public double testSourceRatio() { return sourceFiles == 0 ? 0 : (double) testFiles / sourceFiles; }

    public String report() {
        return String.format(Locale.ROOT, """
                === Project Statistics ===
                Directories:        %d
                Total files:        %d (source %d, test %d)
                Lines of code:      %d
                Test cases:         %d
                Dependencies:       %d
                Avg complexity:     %.2f
                Test/source ratio:  %.2f
                """, directories, totalFiles(), sourceFiles, testFiles, linesOfCode, tests,
                dependencies, averageComplexity(), testSourceRatio());
    }
}
