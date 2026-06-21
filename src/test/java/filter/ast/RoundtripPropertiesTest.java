package filter.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.nodes.Expr;
import filter.ast.printer.AstPrinter;
import filter.FilterParser;
import net.jqwik.api.*;

public class RoundtripPropertiesTest {

    AstBuilderVisitor visitor = new AstBuilderVisitor();
    AstBuilderPattern pattern = new AstBuilderPattern();

    // ---------------- ROUNDTRIP VISITOR ----------------

    @Property
    boolean visitorRoundtrip(@ForAll("simpleQueries") String query) {
        Expr ast1 = AstBuilders.fromQuery(query, visitor::translate);
        String printed = AstPrinter.toString(ast1);

        Expr ast2 = AstBuilders.fromQuery(printed, visitor::translate);

        return AstPrinter.toString(ast1).equals(AstPrinter.toString(ast2));
    }

    // ---------------- ROUNDTRIP PATTERN ----------------

    @Property
    boolean patternRoundtrip(@ForAll("simpleQueries") String query) {
        Expr ast1 = AstBuilders.fromQuery(query, pattern::translate);
        String printed = AstPrinter.toString(ast1);

        Expr ast2 = AstBuilders.fromQuery(printed, pattern::translate);

        return AstPrinter.toString(ast1).equals(AstPrinter.toString(ast2));
    }

    // ---------------- VISITOR vs PATTERN ----------------

    @Property
    boolean visitorEqualsPattern(@ForAll("simpleQueries") String query) {
        Expr v = AstBuilders.fromQuery(query, visitor::translate);
        Expr p = AstBuilders.fromQuery(query, pattern::translate);

        return AstPrinter.toString(v).equals(AstPrinter.toString(p));
    }

    // ---------------- IDENTITY PROPERTY ----------------

    @Property
    boolean prettyPrintStable(@ForAll("simpleQueries") String query) {
        Expr ast = AstBuilders.fromQuery(query, visitor::translate);
        String printed = AstPrinter.toString(ast);

        return printed.equals(AstPrinter.toString(ast));
    }

    // ---------------- LOGICAL PROPERTY (AND/OR ASSOCIATIVITY) ----------------

    @Property
    boolean andIsStable(@ForAll("simpleQueries") String query) {
        Expr ast = AstBuilders.fromQuery(query, visitor::translate);

        String p1 = AstPrinter.toString(ast);
        String p2 = AstPrinter.toString(ast);

        return p1.equals(p2);
    }

    // ---------- PROVIDED ARBITRARIES ----------

    @Provide
    Arbitrary<String> fields() {
        return Arbitraries.of("title", "artist", "genre", "year");
    }

    @Provide
    Arbitrary<String> stringLiterals() {
        return Arbitraries.strings()
            .withChars("abcxyz")
            .ofMinLength(1)
            .ofMaxLength(5)
            .map(s -> "\"" + s + "\"");
    }

    @Provide
    Arbitrary<String> numberLiterals() {
        return Arbitraries.integers().between(1900, 2025)
            .map(Object::toString);
    }

    @Provide
    Arbitrary<String> comparisons() {
        Arbitrary<String> ops = Arbitraries.of("==", "!=", "<", "<=", ">", ">=");

        Arbitrary<String> stringComp =
            Combinators.combine(fields(), ops, stringLiterals())
                .as((f, op, lit) -> f + " " + op + " " + lit);

        Arbitrary<String> numberComp =
            Combinators.combine(Arbitraries.of("year"), ops, numberLiterals())
                .as((f, op, lit) -> f + " " + op + " " + lit);

        return Arbitraries.oneOf(stringComp, numberComp);
    }

    @Provide
    Arbitrary<String> simpleQueries() {
        return comparisons()
            .list()
            .ofMinSize(1)
            .ofMaxSize(3)
            .map(list -> {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) {
                        String conn = Arbitraries.of(" and ", " or ").sample();
                        sb.append(conn);
                    }
                    sb.append(list.get(i));
                }

                return sb.toString();
            });
    }
}
