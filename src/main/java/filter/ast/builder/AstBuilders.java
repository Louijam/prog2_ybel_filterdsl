package filter.ast.builder;

import filter.FilterLexer;
import filter.FilterParser;
import filter.ast.nodes.Expr;
import java.util.function.Function;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class AstBuilders {

  public static Expr fromQuery(String query, Function<FilterParser.QueryContext, Expr> translator) {
    return simplify(translator.apply(parse(query)));
  }

    public static Expr simplify(Expr e) {
        return switch (e) {

            case Expr.Not(var inner) -> {
                Expr simplifiedInner = simplify(inner);

                // doppelte Negation: not (not x) -> x
                if (simplifiedInner instanceof Expr.Not(var inner2)) {
                    yield simplify(inner2);
                }

                yield new Expr.Not(simplifiedInner);
            }

            case Expr.And(var left, var right) ->
                new Expr.And(simplify(left), simplify(right));

            case Expr.Or(var left, var right) ->
                new Expr.Or(simplify(left), simplify(right));

            case Expr.Comparison(var field, var op, var value) ->
                new Expr.Comparison(field, op, value);

            case Expr.InList(var field, var values) ->
                new Expr.InList(field, values);

            default -> e;
        };
    }

  public static FilterParser.QueryContext parse(String query) {
    var cs = CharStreams.fromString(query);
    var lexer = new FilterLexer(cs);
    var tokens = new CommonTokenStream(lexer);
    var parser = new FilterParser(tokens);

    var ctx = parser.query();
    if (parser.getNumberOfSyntaxErrors() > 0)
      throw new IllegalStateException("Syntax errors in query: " + query);

    return ctx;
  }
}
