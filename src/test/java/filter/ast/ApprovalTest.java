package filter.ast;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.printer.AstPrinter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

public class ApprovalTest {

    private final AstBuilderVisitor visitor = new AstBuilderVisitor();
    private final AstBuilderPattern pattern = new AstBuilderPattern();

    private String buildVisitor(String query) {
        return AstPrinter.toString(
            AstBuilders.fromQuery(query, visitor::translate)
        );
    }

    private String buildPattern(String query) {
        return AstPrinter.toString(
            AstBuilders.fromQuery(query, pattern::translate)
        );
    }

    private String compare(String query) {
        return
            "QUERY:\n" + query +
                "\n\n=== VISITOR ===\n" + buildVisitor(query) +
                "\n\n=== PATTERN ===\n" + buildPattern(query);
    }

    @Test
    void simpleComparison() {
        Approvals.verify(compare("artist == \"Beatles\""));
    }

    @Test
    void numberComparison() {
        Approvals.verify(compare("year <= 1965"));
    }

    @Test
    void inList() {
        Approvals.verify(compare("genre in (\"rock\", \"jazz\")"));
    }

    @Test
    void notExpression() {
        Approvals.verify(compare("not artist == \"Beatles\""));
    }

    @Test
    void andExpression() {
        Approvals.verify(compare("artist == \"Beatles\" and year == 1965"));
    }

    @Test
    void orExpression() {
        Approvals.verify(compare("artist == \"Beatles\" or year == 1965"));
    }

    @Test
    void precedenceTest() {
        Approvals.verify(compare("a == 1 or b == 2 and c == 3"));
    }

    @Test
    void parenthesesTest() {
        Approvals.verify(compare("(a == 1 or b == 2) and c == 3"));
    }

    @Test
    void complexQuery() {
        Approvals.verify(
            compare("genre in (\"rock\", \"jazz\") or year <= 1990 and not artist == \"Beatles\"")
        );
    }
}
