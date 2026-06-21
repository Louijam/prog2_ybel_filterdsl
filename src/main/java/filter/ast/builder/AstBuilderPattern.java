package filter.ast.builder;

import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;

import java.util.ArrayList;
import java.util.List;

public class AstBuilderPattern {

    // Public entry point
    // query : expr EOF
    public Expr translate(FilterParser.QueryContext ctx) {
        return buildExpr(ctx.expr());
    }

    // expr: orExpr
    private Expr buildExpr(FilterParser.ExprContext ctx) {
        return buildOrExpr(ctx.orExpr());
    }

    // orExpr : andExpr (OR andExpr)*
    private Expr buildOrExpr(FilterParser.OrExprContext ctx) {
        Expr result = buildAndExpr(ctx.andExpr(0));

        for (int i = 1; i < ctx.andExpr().size(); i++) {
            result = new Expr.Or(result, buildAndExpr(ctx.andExpr(i)));
        }

        return result;
    }

    // andExpr: notExpr (AND notExpr)*
    private Expr buildAndExpr(FilterParser.AndExprContext ctx) {
        Expr result = buildNotExpr(ctx.notExpr(0));

        for (int i = 1; i < ctx.notExpr().size(); i++) {
            result = new Expr.And(result, buildNotExpr(ctx.notExpr(i)));
        }

        return result;
    }

    // notExpr: NOT notExpr | primary
    private Expr buildNotExpr(FilterParser.NotExprContext ctx) {
        if (ctx.NOT() != null) {
            return new Expr.Not(buildNotExpr(ctx.notExpr()));
        }
        return buildPrimary(ctx.primary());
    }

    // primary: comparison | '(' expr ')'
    private Expr buildPrimary(FilterParser.PrimaryContext ctx) {
        if (ctx.comparison() != null) {
            return buildComparison(ctx.comparison());
        }
        return buildExpr(ctx.expr());
    }

    // comparison
    //   : IDENTIFIER op=COMPOP value=literal
    //   | IDENTIFIER IN '(' literalList ')'
    private Expr buildComparison(FilterParser.ComparisonContext ctx) {
        String field = ctx.IDENTIFIER().getText();

        if (ctx.COMPOP() != null) {
            Value value = buildLiteral(ctx.literal());

            CompOp op = switch (ctx.COMPOP().getText()) {
                case "==" -> CompOp.EQ;
                case "!=" -> CompOp.NE;
                case "<" -> CompOp.LT;
                case "<=" -> CompOp.LE;
                case ">" -> CompOp.GT;
                case ">=" -> CompOp.GE;
                default -> throw new IllegalStateException("Unknown operator: " + ctx.COMPOP().getText());
            };

            return new Expr.Comparison(field, op, value);
        }

        List<Value> values = buildLiteralList(ctx.literalList());
        return new Expr.InList(field, values);
    }

    // literalList: literal (',' literal)*
    private List<Value> buildLiteralList(FilterParser.LiteralListContext ctx) {
        List<Value> values = new ArrayList<>();

        for (FilterParser.LiteralContext lit : ctx.literal()) {
            values.add(buildLiteral(lit));
        }

        return values;
    }

    // literal: STRING | NUMBER
    private Value buildLiteral(FilterParser.LiteralContext ctx) {
        if (ctx.STRING() != null) {
            String text = ctx.STRING().getText();
            text = text.substring(1, text.length() - 1); // quotes entfernen
            return new Value.Str(text);
        }

        return new Value.Num(Integer.parseInt(ctx.NUMBER().getText()));
    }
}
