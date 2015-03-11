// Generated from /Users/trevorallred/projects/tantalim/scala/modules/filterCompiler/src/main/scala/com/tantalim/filter/compiler/Filter.g4 by ANTLR 4.5
package com.tantalim.filter.compiler.src;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FilterParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FilterVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FilterParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(@NotNull FilterParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code operatorExpr}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorExpr(@NotNull FilterParser.OperatorExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#anyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyValue(@NotNull FilterParser.AnyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#simpleValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleValue(@NotNull FilterParser.SimpleValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#andOrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndOrs(@NotNull FilterParser.AndOrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(@NotNull FilterParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(@NotNull FilterParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#date}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDate(@NotNull FilterParser.DateContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#comparators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparators(@NotNull FilterParser.ComparatorsContext ctx);
}