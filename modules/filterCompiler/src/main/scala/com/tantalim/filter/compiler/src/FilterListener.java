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
	 * Enter a parse tree produced by the {@code AndPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatementPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterStatementPhrase(@NotNull FilterParser.StatementPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatementPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitStatementPhrase(@NotNull FilterParser.StatementPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
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
	 * Enter a parse tree produced by the {@code fieldAtom}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterFieldAtom(@NotNull FilterParser.FieldAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fieldAtom}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitFieldAtom(@NotNull FilterParser.FieldAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code basicAtm}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterBasicAtm(@NotNull FilterParser.BasicAtmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code basicAtm}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitBasicAtm(@NotNull FilterParser.BasicAtmContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void enterNumberAtom(@NotNull FilterParser.NumberAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void exitNumberAtom(@NotNull FilterParser.NumberAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void enterBooleanAtom(@NotNull FilterParser.BooleanAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void exitBooleanAtom(@NotNull FilterParser.BooleanAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void enterStringAtom(@NotNull FilterParser.StringAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void exitStringAtom(@NotNull FilterParser.StringAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code listAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void enterListAtom(@NotNull FilterParser.ListAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code listAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 */
	void exitListAtom(@NotNull FilterParser.ListAtomContext ctx);
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