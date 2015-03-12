// Generated from /Users/trevorallred/projects/tantalim/scala/modules/filterCompiler/src/main/scala/com/tantalim/filter/compiler/Filter.g4 by ANTLR 4.5
package com.tantalim.filter.compiler.src;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, TRUE=33, FALSE=34, AND=35, OR=36, NOW=37, FIELD=38, INT=39, 
		FLOAT=40, STRING=41, SPACE=42;
	public static final int
		RULE_start = 0, RULE_phrase = 1, RULE_andOrs = 2, RULE_atom = 3, RULE_basicAtom = 4, 
		RULE_futureDate = 5, RULE_field = 6, RULE_comparators = 7, RULE_dateMeasure = 8;
	public static final String[] ruleNames = {
		"start", "phrase", "andOrs", "atom", "basicAtom", "futureDate", "field", 
		"comparators", "dateMeasure"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'-'", "','", "'='", "'!='", "'Equals'", "'NotEquals'", 
		"'In'", "'NotIn'", "'BeginsWith'", "'EndsWith'", "'Contains'", "'>'", 
		"'>='", "'GreaterThan'", "'GreaterThanOrEqual'", "'<'", "'<='", "'LessThan'", 
		"'LessThanOrEqual'", "'Before'", "'OnOrBefore'", "'After'", "'OnOrAfter'", 
		"'s'", "'m'", "'h'", "'D'", "'W'", "'M'", "'Y'", "'true'", "'false'", 
		null, null, "'NOW'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, "TRUE", "FALSE", 
		"AND", "OR", "NOW", "FIELD", "INT", "FLOAT", "STRING", "SPACE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override
	@NotNull
	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Filter.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FilterParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StartContext extends ParserRuleContext {
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18); 
			phrase(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PhraseContext extends ParserRuleContext {
		public PhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phrase; }
	 
		public PhraseContext() { }
		public void copyFrom(PhraseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AndPhraseContext extends PhraseContext {
		public PhraseContext left;
		public AndOrsContext andor;
		public PhraseContext right;
		public List<PhraseContext> phrase() {
			return getRuleContexts(PhraseContext.class);
		}
		public PhraseContext phrase(int i) {
			return getRuleContext(PhraseContext.class,i);
		}
		public AndOrsContext andOrs() {
			return getRuleContext(AndOrsContext.class,0);
		}
		public AndPhraseContext(PhraseContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterAndPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitAndPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitAndPhrase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StatementPhraseContext extends PhraseContext {
		public FieldContext left;
		public ComparatorsContext comparator;
		public AtomContext right;
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public ComparatorsContext comparators() {
			return getRuleContext(ComparatorsContext.class,0);
		}
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public StatementPhraseContext(PhraseContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterStatementPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitStatementPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitStatementPhrase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesisPhraseContext extends PhraseContext {
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public ParenthesisPhraseContext(PhraseContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterParenthesisPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitParenthesisPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitParenthesisPhrase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PhraseContext phrase() throws RecognitionException {
		return phrase(0);
	}

	private PhraseContext phrase(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PhraseContext _localctx = new PhraseContext(_ctx, _parentState);
		PhraseContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_phrase, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			switch (_input.LA(1)) {
			case T__0:
				{
				_localctx = new ParenthesisPhraseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(21); 
				match(T__0);
				setState(22); 
				phrase(0);
				setState(23); 
				match(T__1);
				}
				break;
			case FIELD:
				{
				_localctx = new StatementPhraseContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(25); 
				((StatementPhraseContext)_localctx).left = field();
				setState(26); 
				((StatementPhraseContext)_localctx).comparator = comparators();
				setState(27); 
				((StatementPhraseContext)_localctx).right = atom();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(37);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new AndPhraseContext(new PhraseContext(_parentctx, _parentState));
					((AndPhraseContext)_localctx).left = _prevctx;
					pushNewRecursionContext(_localctx, _startState, RULE_phrase);
					setState(31);
					if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
					setState(32); 
					((AndPhraseContext)_localctx).andor = andOrs();
					setState(33); 
					((AndPhraseContext)_localctx).right = phrase(4);
					}
					} 
				}
				setState(39);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class AndOrsContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(FilterParser.AND, 0); }
		public TerminalNode OR() { return getToken(FilterParser.OR, 0); }
		public AndOrsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andOrs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterAndOrs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitAndOrs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitAndOrs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndOrsContext andOrs() throws RecognitionException {
		AndOrsContext _localctx = new AndOrsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_andOrs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==OR) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtomContext extends ParserRuleContext {
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
	 
		public AtomContext() { }
		public void copyFrom(AtomContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class BasicAtmContext extends AtomContext {
		public BasicAtomContext basicAtom() {
			return getRuleContext(BasicAtomContext.class,0);
		}
		public BasicAtmContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterBasicAtm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitBasicAtm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitBasicAtm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FieldAtomContext extends AtomContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public FieldAtomContext(AtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterFieldAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitFieldAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitFieldAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_atom);
		try {
			setState(44);
			switch (_input.LA(1)) {
			case FIELD:
				_localctx = new FieldAtomContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(42); 
				field();
				}
				break;
			case T__0:
			case T__2:
			case TRUE:
			case FALSE:
			case NOW:
			case INT:
			case FLOAT:
			case STRING:
				_localctx = new BasicAtmContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(43); 
				basicAtom();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BasicAtomContext extends ParserRuleContext {
		public BasicAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basicAtom; }
	 
		public BasicAtomContext() { }
		public void copyFrom(BasicAtomContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PastDateAtomContext extends BasicAtomContext {
		public FutureDateContext futureDate() {
			return getRuleContext(FutureDateContext.class,0);
		}
		public PastDateAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterPastDateAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitPastDateAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitPastDateAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ListAtomContext extends BasicAtomContext {
		public List<BasicAtomContext> basicAtom() {
			return getRuleContexts(BasicAtomContext.class);
		}
		public BasicAtomContext basicAtom(int i) {
			return getRuleContext(BasicAtomContext.class,i);
		}
		public ListAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterListAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitListAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitListAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FutureDateAtomContext extends BasicAtomContext {
		public FutureDateContext futureDate() {
			return getRuleContext(FutureDateContext.class,0);
		}
		public FutureDateAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterFutureDateAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitFutureDateAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitFutureDateAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringAtomContext extends BasicAtomContext {
		public TerminalNode STRING() { return getToken(FilterParser.STRING, 0); }
		public StringAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterStringAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitStringAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitStringAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumberAtomContext extends BasicAtomContext {
		public TerminalNode INT() { return getToken(FilterParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(FilterParser.FLOAT, 0); }
		public NumberAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterNumberAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitNumberAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitNumberAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DateNowContext extends BasicAtomContext {
		public TerminalNode NOW() { return getToken(FilterParser.NOW, 0); }
		public DateNowContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterDateNow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitDateNow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitDateNow(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanAtomContext extends BasicAtomContext {
		public TerminalNode TRUE() { return getToken(FilterParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(FilterParser.FALSE, 0); }
		public BooleanAtomContext(BasicAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterBooleanAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitBooleanAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitBooleanAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BasicAtomContext basicAtom() throws RecognitionException {
		BasicAtomContext _localctx = new BasicAtomContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_basicAtom);
		int _la;
		try {
			setState(64);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new NumberAtomContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(46);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==FLOAT) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			case 2:
				_localctx = new BooleanAtomContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			case 3:
				_localctx = new StringAtomContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(48); 
				match(STRING);
				}
				break;
			case 4:
				_localctx = new DateNowContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(49); 
				match(NOW);
				}
				break;
			case 5:
				_localctx = new PastDateAtomContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(50); 
				match(T__2);
				setState(51); 
				futureDate();
				}
				break;
			case 6:
				_localctx = new FutureDateAtomContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(52); 
				futureDate();
				}
				break;
			case 7:
				_localctx = new ListAtomContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(53); 
				match(T__0);
				{
				setState(54); 
				basicAtom();
				}
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(55); 
					match(T__3);
					setState(56); 
					basicAtom();
					}
					}
					setState(61);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(62); 
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FutureDateContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(FilterParser.INT, 0); }
		public DateMeasureContext dateMeasure() {
			return getRuleContext(DateMeasureContext.class,0);
		}
		public FutureDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_futureDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterFutureDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitFutureDate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitFutureDate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FutureDateContext futureDate() throws RecognitionException {
		FutureDateContext _localctx = new FutureDateContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_futureDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66); 
			match(INT);
			setState(67); 
			dateMeasure();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldContext extends ParserRuleContext {
		public TerminalNode FIELD() { return getToken(FilterParser.FIELD, 0); }
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_field);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); 
			match(FIELD);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparatorsContext extends ParserRuleContext {
		public ComparatorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparators; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterComparators(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitComparators(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitComparators(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparatorsContext comparators() throws RecognitionException {
		ComparatorsContext _localctx = new ComparatorsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_comparators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateMeasureContext extends ParserRuleContext {
		public DateMeasureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateMeasure; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).enterDateMeasure(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterListener ) ((FilterListener)listener).exitDateMeasure(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterVisitor ) return ((FilterVisitor<? extends T>)visitor).visitDateMeasure(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateMeasureContext dateMeasure() throws RecognitionException {
		DateMeasureContext _localctx = new DateMeasureContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_dateMeasure);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__25) | (1L << T__26) | (1L << T__27) | (1L << T__28) | (1L << T__29) | (1L << T__30) | (1L << T__31))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1: 
			return phrase_sempred((PhraseContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean phrase_sempred(PhraseContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: 
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3,N\4\2\t\2\4\3\t\3"+
		"\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3 \n\3\3\3\3\3\3\3\3\3\7\3&\n\3\f\3\16"+
		"\3)\13\3\3\4\3\4\3\5\3\5\5\5/\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\7\6<\n\6\f\6\16\6?\13\6\3\6\3\6\5\6C\n\6\3\7\3\7\3\7\3\b\3\b"+
		"\3\t\3\t\3\n\3\n\3\n\2\3\4\13\2\4\6\b\n\f\16\20\22\2\7\3\2%&\3\2)*\3\2"+
		"#$\3\2\7\33\3\2\34\"N\2\24\3\2\2\2\4\37\3\2\2\2\6*\3\2\2\2\b.\3\2\2\2"+
		"\nB\3\2\2\2\fD\3\2\2\2\16G\3\2\2\2\20I\3\2\2\2\22K\3\2\2\2\24\25\5\4\3"+
		"\2\25\3\3\2\2\2\26\27\b\3\1\2\27\30\7\3\2\2\30\31\5\4\3\2\31\32\7\4\2"+
		"\2\32 \3\2\2\2\33\34\5\16\b\2\34\35\5\20\t\2\35\36\5\b\5\2\36 \3\2\2\2"+
		"\37\26\3\2\2\2\37\33\3\2\2\2 \'\3\2\2\2!\"\f\5\2\2\"#\5\6\4\2#$\5\4\3"+
		"\6$&\3\2\2\2%!\3\2\2\2&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(\5\3\2\2\2)\'\3"+
		"\2\2\2*+\t\2\2\2+\7\3\2\2\2,/\5\16\b\2-/\5\n\6\2.,\3\2\2\2.-\3\2\2\2/"+
		"\t\3\2\2\2\60C\t\3\2\2\61C\t\4\2\2\62C\7+\2\2\63C\7\'\2\2\64\65\7\5\2"+
		"\2\65C\5\f\7\2\66C\5\f\7\2\678\7\3\2\28=\5\n\6\29:\7\6\2\2:<\5\n\6\2;"+
		"9\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>@\3\2\2\2?=\3\2\2\2@A\7\4\2\2"+
		"AC\3\2\2\2B\60\3\2\2\2B\61\3\2\2\2B\62\3\2\2\2B\63\3\2\2\2B\64\3\2\2\2"+
		"B\66\3\2\2\2B\67\3\2\2\2C\13\3\2\2\2DE\7)\2\2EF\5\22\n\2F\r\3\2\2\2GH"+
		"\7(\2\2H\17\3\2\2\2IJ\t\5\2\2J\21\3\2\2\2KL\t\6\2\2L\23\3\2\2\2\7\37\'"+
		".=B";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}