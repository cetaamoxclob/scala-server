// Generated from /Users/trevorallred/projects/tantalim/scala/modules/scriptCompiler/src/main/scala/com/tantalim/script/compiler/TantalimScript.g4 by ANTLR 4.5
package com.tantalim.script.compiler.src;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TantalimScriptParser}.
 */
public interface TantalimScriptListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(@NotNull TantalimScriptParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(@NotNull TantalimScriptParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(@NotNull TantalimScriptParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(@NotNull TantalimScriptParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(@NotNull TantalimScriptParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(@NotNull TantalimScriptParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#print}.
	 * @param ctx the parse tree
	 */
	void enterPrint(@NotNull TantalimScriptParser.PrintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#print}.
	 * @param ctx the parse tree
	 */
	void exitPrint(@NotNull TantalimScriptParser.PrintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterIdAssignment(@NotNull TantalimScriptParser.IdAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitIdAssignment(@NotNull TantalimScriptParser.IdAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fieldAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterFieldAssignment(@NotNull TantalimScriptParser.FieldAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fieldAssignment}
	 * labeled alternative in {@link TantalimScriptParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitFieldAssignment(@NotNull TantalimScriptParser.FieldAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#returnStat}.
	 * @param ctx the parse tree
	 */
	void enterReturnStat(@NotNull TantalimScriptParser.ReturnStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#returnStat}.
	 * @param ctx the parse tree
	 */
	void exitReturnStat(@NotNull TantalimScriptParser.ReturnStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#ifStat}.
	 * @param ctx the parse tree
	 */
	void enterIfStat(@NotNull TantalimScriptParser.IfStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#ifStat}.
	 * @param ctx the parse tree
	 */
	void exitIfStat(@NotNull TantalimScriptParser.IfStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#conditionBlock}.
	 * @param ctx the parse tree
	 */
	void enterConditionBlock(@NotNull TantalimScriptParser.ConditionBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#conditionBlock}.
	 * @param ctx the parse tree
	 */
	void exitConditionBlock(@NotNull TantalimScriptParser.ConditionBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#statBlock}.
	 * @param ctx the parse tree
	 */
	void enterStatBlock(@NotNull TantalimScriptParser.StatBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#statBlock}.
	 * @param ctx the parse tree
	 */
	void exitStatBlock(@NotNull TantalimScriptParser.StatBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 */
	void enterForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 */
	void exitForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(@NotNull TantalimScriptParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(@NotNull TantalimScriptParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinusExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinusExpr(@NotNull TantalimScriptParser.UnaryMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinusExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinusExpr(@NotNull TantalimScriptParser.UnaryMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code relationalExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpr(@NotNull TantalimScriptParser.RelationalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code relationalExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpr(@NotNull TantalimScriptParser.RelationalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(@NotNull TantalimScriptParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(@NotNull TantalimScriptParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(@NotNull TantalimScriptParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(@NotNull TantalimScriptParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalityExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(@NotNull TantalimScriptParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalityExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(@NotNull TantalimScriptParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicationExpr(@NotNull TantalimScriptParser.MultiplicationExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicationExpr(@NotNull TantalimScriptParser.MultiplicationExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(@NotNull TantalimScriptParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(@NotNull TantalimScriptParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(@NotNull TantalimScriptParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link TantalimScriptParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(@NotNull TantalimScriptParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterParExpr(@NotNull TantalimScriptParser.ParExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitParExpr(@NotNull TantalimScriptParser.ParExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterNumberAtom(@NotNull TantalimScriptParser.NumberAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitNumberAtom(@NotNull TantalimScriptParser.NumberAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterBooleanAtom(@NotNull TantalimScriptParser.BooleanAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitBooleanAtom(@NotNull TantalimScriptParser.BooleanAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterIdAtom(@NotNull TantalimScriptParser.IdAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitIdAtom(@NotNull TantalimScriptParser.IdAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterStringAtom(@NotNull TantalimScriptParser.StringAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringAtom}
	 * labeled alternative in {@link TantalimScriptParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitStringAtom(@NotNull TantalimScriptParser.StringAtomContext ctx);
}