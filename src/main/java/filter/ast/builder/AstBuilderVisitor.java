package filter.ast.builder;

import filter.FilterBaseVisitor;
import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AstBuilderVisitor extends FilterBaseVisitor<Void> {

    private final Deque<Expr> exprStack = new ArrayDeque<>();
    private final Deque<Value> valueStack = new ArrayDeque<>();
    private final Deque<List<Value>> valueListStack = new ArrayDeque<>();

    // Public entry point
    public Expr translate(FilterParser.QueryContext ctx) {
        visit(ctx);
        return exprStack.pop();
    }

    // query : expr EOF
    @Override
    public Void visitQuery(FilterParser.QueryContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // expr: orExpr
    @Override
    public Void visitExpr(FilterParser.ExprContext ctx) {
        visit(ctx.orExpr());
        return null;
    }

    // orExpr : andExpr (OR andExpr)*
    @Override
    public Void visitOrExpr(FilterParser.OrExprContext ctx) {
        List<Expr> parts = new ArrayList<>();

        visit(ctx.andExpr(0));
        parts.add(exprStack.pop());

        for (int i = 1; i < ctx.andExpr().size(); i++) {
            visit(ctx.andExpr(i));
            parts.add(exprStack.pop());
        }

        Expr current = parts.getFirst();
        for (int i = 1; i < parts.size(); i++) {
            current = new Expr.Or(current, parts.get(i));
        }

        exprStack.push(current);
        return null;
    }

    // andExpr: notExpr (AND notExpr)*
    @Override
    public Void visitAndExpr(FilterParser.AndExprContext ctx) {
        List<Expr> parts = new ArrayList<>();

        visit(ctx.notExpr(0));
        parts.add(exprStack.pop());

        for (int i = 1; i < ctx.notExpr().size(); i++) {
            visit(ctx.notExpr(i));
            parts.add(exprStack.pop());
        }

        Expr current = parts.getFirst();
        for (int i = 1; i < parts.size(); i++) {
            current = new Expr.And(current, parts.get(i));
        }

        exprStack.push(current);
        return null;
    }

    // notExpr: NOT notExpr | primary
    @Override
    public Void visitNotExpr(FilterParser.NotExprContext ctx) {
        if (ctx.NOT() != null) {
            visit(ctx.notExpr());
            Expr inner = exprStack.pop();
            exprStack.push(new Expr.Not(inner));
        } else {
            visit(ctx.primary());
        }
        return null;
    }

    // primary: comparison | '(' expr ')'
    @Override
    public Void visitPrimary(FilterParser.PrimaryContext ctx) {
        if (ctx.comparison() != null) {
            visit(ctx.comparison());
        } else {
            visit(ctx.expr());
        }
        return null;
    }

    // comparison
    //   : IDENTIFIER op=COMPOP value=literal
    //   | IDENTIFIER IN '(' literalList ')'
    @Override
    public Void visitComparison(FilterParser.ComparisonContext ctx) {
        String field = ctx.IDENTIFIER().getText();

        if (ctx.COMPOP() != null) {
            visit(ctx.literal());
            Value value = valueStack.pop();

            CompOp op = switch (ctx.COMPOP().getText()) {
                case "==" -> CompOp.EQ;
                case "!=" -> CompOp.NE;
                case "<" -> CompOp.LT;
                case "<=" -> CompOp.LE;
                case ">" -> CompOp.GT;
                case ">=" -> CompOp.GE;
                default -> throw new IllegalStateException("Unknown op");
            };

            exprStack.push(new Expr.Comparison(field, op, value));
        } else {
            visit(ctx.literalList());
            List<Value> values = valueListStack.pop();
            exprStack.push(new Expr.InList(field, values));
        }

        return null;
    }

    // literalList: literal (',' literal)*
    @Override
    public Void visitLiteralList(FilterParser.LiteralListContext ctx) {
        List<Value> values = new ArrayList<>();

        for (FilterParser.LiteralContext lit : ctx.literal()) {
            visit(lit);
            values.add(valueStack.pop());
        }

        valueListStack.push(values);
        return null;
    }

    // literal: STRING | NUMBER
    @Override
    public Void visitLiteral(FilterParser.LiteralContext ctx) {
        if (ctx.STRING() != null) {
            String text = ctx.STRING().getText();
            text = text.substring(1, text.length() - 1); // remove quotes
            valueStack.push(new Value.Str(text));
        } else {
            valueStack.push(new Value.Num(Integer.parseInt(ctx.NUMBER().getText())));
        }
        return null;
    }
}
