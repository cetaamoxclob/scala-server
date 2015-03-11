// Generated from /Users/trevorallred/projects/tantalim/scala/modules/filterCompiler/src/main/scala/com/tantalim/filter/compiler/Filter.g4 by ANTLR 4.5
package com.tantalim.filter.compiler.src;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FilterParser}.
 */
public interface FilterListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FilterParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(@NotNull FilterParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(@NotNull FilterParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code operatorExpr}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterOperatorExpr(@NotNull FilterParser.OperatorExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code operatorExpr}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitOperatorExpr(@NotNull FilterParser.OperatorExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#anyValue}.
	 * @param ctx the parse tree
	 */
	void enterAnyValue(@NotNull FilterParser.AnyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#anyValue}.
	 * @param ctx the parse tree
	 */
	void exitAnyValue(@NotNull FilterParser.AnyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#simpleValue}.
	 * @param ctx the parse tree
	 */
	void enterSimpleValue(@NotNull FilterParser.SimpleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#simpleValue}.
	 * @param ctx the parse tree
	 */
	void exitSimpleValue(@NotNull FilterParser.SimpleValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#andOrs}.
	 * @param ctx the parse tree
	 */
	void enterAndOrs(@NotNull FilterParser.AndOrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#andOrs}.
	 * @param ctx the parse tree
	 */
	void exitAndOrs(@NotNull FilterParser.AndOrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(@NotNull FilterParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(@NotNull FilterParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(@NotNull FilterParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(@NotNull FilterParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(@NotNull FilterParser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(@NotNull FilterParser.DateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterParser#comparators}.
	 * @param ctx the parse tree
	 */
	void enterComparators(@NotNull FilterParser.ComparatorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#comparators}.
	 * @param ctx the parse tree
	 */
	void exitComparators(@NotNull FilterParser.ComparatorsContext ctx);
}