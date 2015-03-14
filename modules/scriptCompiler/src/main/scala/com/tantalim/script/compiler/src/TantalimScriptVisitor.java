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
	 * Visit a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
}