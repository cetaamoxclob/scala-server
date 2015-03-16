// Generated from /Users/trevorallred/projects/tantalim/scala/modules/scriptCompiler/src/main/scala/com/tantalim/script/compiler/TantalimScript.g4 by ANTLR 4.5
package com.tantalim.script.compiler.src;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TantalimScriptParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TantalimScriptVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(@NotNull TantalimScriptParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(@NotNull TantalimScriptParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(@NotNull TantalimScriptParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#print}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrint(@NotNull TantalimScriptParser.PrintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code idAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdAssignment(@NotNull TantalimScriptParser.IdAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fieldAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAssignment(@NotNull TantalimScriptParser.FieldAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#returnStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStat(@NotNull TantalimScriptParser.ReturnStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#ifStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStat(@NotNull TantalimScriptParser.IfStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#conditionBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionBlock(@NotNull TantalimScriptParser.ConditionBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#statBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatBlock(@NotNull TantalimScriptParser.StatBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParExpr(@NotNull TantalimScriptParser.ParExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(@NotNull TantalimScriptParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinusExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinusExpr(@NotNull TantalimScriptParser.UnaryMinusExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code relationalExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpr(@NotNull TantalimScriptParser.RelationalExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(@NotNull TantalimScriptParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(@NotNull TantalimScriptParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code equalityExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpr(@NotNull TantalimScriptParser.EqualityExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicationExpr(@NotNull TantalimScriptParser.MultiplicationExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(@NotNull TantalimScriptParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpr(@NotNull TantalimScriptParser.NotExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParAtom(@NotNull TantalimScriptParser.ParAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberAtom(@NotNull TantalimScriptParser.NumberAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanAtom(@NotNull TantalimScriptParser.BooleanAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code idAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdAtom(@NotNull TantalimScriptParser.IdAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringAtom(@NotNull TantalimScriptParser.StringAtomContext ctx);
}