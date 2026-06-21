package filter.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilders;
import filter.ast.printer.AstPrinter;
import org.junit.jupiter.api.Test;

public class AstTest {
    //TODO

    @Test
    void comparisonString() {
        var ast = AstBuilders.fromQuery(
            "artist == \"Beatles\"",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(artist == \"Beatles\")",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void comparisonNumber() {
        var ast = AstBuilders.fromQuery(
            "year <= 1965",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(year <= 1965)",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void inList() {
        var ast = AstBuilders.fromQuery(
            "genre in (\"rock\", \"jazz\")",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(genre in (\"rock\", \"jazz\"))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void notExpression() {
        var ast = AstBuilders.fromQuery(
            "not artist == \"Beatles\"",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(not (artist == \"Beatles\"))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void andExpression() {
        var ast = AstBuilders.fromQuery(
            "artist == \"Beatles\" and year == 1965",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "((artist == \"Beatles\") and (year == 1965))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void orExpression() {
        var ast = AstBuilders.fromQuery(
            "artist == \"Beatles\" or year == 1965",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "((artist == \"Beatles\") or (year == 1965))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void multipleAnds() {
        var ast = AstBuilders.fromQuery(
            "a == 1 and b == 2 and c == 3",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(((a == 1) and (b == 2)) and (c == 3))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void multipleOrs() {
        var ast = AstBuilders.fromQuery(
            "a == 1 or b == 2 or c == 3",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(((a == 1) or (b == 2)) or (c == 3))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void precedenceAndBeforeOr() {
        var ast = AstBuilders.fromQuery(
            "a == 1 or b == 2 and c == 3",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "((a == 1) or ((b == 2) and (c == 3)))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void parenthesesOverridePrecedence() {
        var ast = AstBuilders.fromQuery(
            "(a == 1 or b == 2) and c == 3",
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "(((a == 1) or (b == 2)) and (c == 3))",
            AstPrinter.toString(ast)
        );
    }

    @Test
    void complexExpression() {
        var ast = AstBuilders.fromQuery(
            """
            genre in ("rock", "jazz")
            or year <= 1990
            and not artist == "Beatles"
            """,
            new AstBuilderPattern()::translate
        );

        assertEquals(
            "((genre in (\"rock\", \"jazz\")) or ((year <= 1990) and (not (artist == \"Beatles\"))))",
            AstPrinter.toString(ast)
        );
    }
}
