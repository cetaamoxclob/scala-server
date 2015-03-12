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
	 * Visit a parse tree produced by the {@code AndPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndPhrase(@NotNull FilterParser.AndPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatementPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementPhrase(@NotNull FilterParser.StatementPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenthesisPhrase}
	 * labeled alternative in {@link FilterParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesisPhrase(@NotNull FilterParser.ParenthesisPhraseContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#andOrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndOrs(@NotNull FilterParser.AndOrsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fieldAtom}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAtom(@NotNull FilterParser.FieldAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code basicAtm}
	 * labeled alternative in {@link FilterParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasicAtm(@NotNull FilterParser.BasicAtmContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberAtom(@NotNull FilterParser.NumberAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanAtom(@NotNull FilterParser.BooleanAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringAtom(@NotNull FilterParser.StringAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DateNow}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateNow(@NotNull FilterParser.DateNowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code pastDateAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPastDateAtom(@NotNull FilterParser.PastDateAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code futureDateAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFutureDateAtom(@NotNull FilterParser.FutureDateAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code listAtom}
	 * labeled alternative in {@link FilterParser#basicAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListAtom(@NotNull FilterParser.ListAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#futureDate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFutureDate(@NotNull FilterParser.FutureDateContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(@NotNull FilterParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#comparators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparators(@NotNull FilterParser.ComparatorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterParser#dateMeasure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateMeasure(@NotNull FilterParser.DateMeasureContext ctx);
}