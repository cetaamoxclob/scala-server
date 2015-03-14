// Generated from /Users/trevorallred/projects/tantalim/scala/modules/scriptCompiler/src/main/scala/com/tantalim/script/compiler/TantalimScript.g4 by ANTLR 4.5
package com.tantalim.script.compiler.src;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TantalimScriptLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, AND=2, EQ=3, NEQ=4, GT=5, LT=6, GTEQ=7, LTEQ=8, PLUS=9, MINUS=10, 
		MULT=11, DIV=12, MOD=13, POW=14, NOT=15, ASSIGN=16, OPAR=17, CPAR=18, 
		OBRACE=19, CBRACE=20, TRUE=21, FALSE=22, IF=23, ELSE=24, PRINT=25, FOR=26, 
		IN=27, ID=28, INT=29, FLOAT=30, STRING=31, COMMENT=32, SPACE=33, OTHER=34;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"OR", "AND", "EQ", "NEQ", "GT", "LT", "GTEQ", "LTEQ", "PLUS", "MINUS", 
		"MULT", "DIV", "MOD", "POW", "NOT", "ASSIGN", "OPAR", "CPAR", "OBRACE", 
		"CBRACE", "TRUE", "FALSE", "IF", "ELSE", "PRINT", "FOR", "IN", "ID", "INT", 
		"FLOAT", "STRING", "COMMENT", "SPACE", "OTHER"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'or'", "'and'", "'=='", "'!='", "'>'", "'<'", "'>='", "'<='", "'+'", 
		"'-'", "'*'", "'/'", "'%'", "'^'", "'!'", "'='", "'('", "')'", "'{'", 
		"'}'", "'true'", "'false'", "'if'", "'else'", "'print'", "'for'", "'in'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "OR", "AND", "EQ", "NEQ", "GT", "LT", "GTEQ", "LTEQ", "PLUS", "MINUS", 
		"MULT", "DIV", "MOD", "POW", "NOT", "ASSIGN", "OPAR", "CPAR", "OBRACE", 
		"CBRACE", "TRUE", "FALSE", "IF", "ELSE", "PRINT", "FOR", "IN", "ID", "INT", 
		"FLOAT", "STRING", "COMMENT", "SPACE", "OTHER"
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


	public TantalimScriptLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TantalimScript.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2$\u00d0\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5"+
		"\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r"+
		"\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24"+
		"\3\24\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\35\3\35\7\35\u0099\n\35\f\35\16"+
		"\35\u009c\13\35\3\36\6\36\u009f\n\36\r\36\16\36\u00a0\3\37\6\37\u00a4"+
		"\n\37\r\37\16\37\u00a5\3\37\3\37\7\37\u00aa\n\37\f\37\16\37\u00ad\13\37"+
		"\3\37\3\37\6\37\u00b1\n\37\r\37\16\37\u00b2\5\37\u00b5\n\37\3 \3 \3 \3"+
		" \7 \u00bb\n \f \16 \u00be\13 \3 \3 \3!\3!\7!\u00c4\n!\f!\16!\u00c7\13"+
		"!\3!\3!\3\"\3\"\3\"\3\"\3#\3#\2\2$\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30"+
		"/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$\3\2\b\5\2C\\aac|\6\2\62"+
		";C\\aac|\3\2\62;\5\2\f\f\17\17$$\4\2\f\f\17\17\5\2\13\f\17\17\"\"\u00d8"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3"+
		"\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\3G\3\2\2"+
		"\2\5J\3\2\2\2\7N\3\2\2\2\tQ\3\2\2\2\13T\3\2\2\2\rV\3\2\2\2\17X\3\2\2\2"+
		"\21[\3\2\2\2\23^\3\2\2\2\25`\3\2\2\2\27b\3\2\2\2\31d\3\2\2\2\33f\3\2\2"+
		"\2\35h\3\2\2\2\37j\3\2\2\2!l\3\2\2\2#n\3\2\2\2%p\3\2\2\2\'r\3\2\2\2)t"+
		"\3\2\2\2+v\3\2\2\2-{\3\2\2\2/\u0081\3\2\2\2\61\u0084\3\2\2\2\63\u0089"+
		"\3\2\2\2\65\u008f\3\2\2\2\67\u0093\3\2\2\29\u0096\3\2\2\2;\u009e\3\2\2"+
		"\2=\u00b4\3\2\2\2?\u00b6\3\2\2\2A\u00c1\3\2\2\2C\u00ca\3\2\2\2E\u00ce"+
		"\3\2\2\2GH\7q\2\2HI\7t\2\2I\4\3\2\2\2JK\7c\2\2KL\7p\2\2LM\7f\2\2M\6\3"+
		"\2\2\2NO\7?\2\2OP\7?\2\2P\b\3\2\2\2QR\7#\2\2RS\7?\2\2S\n\3\2\2\2TU\7@"+
		"\2\2U\f\3\2\2\2VW\7>\2\2W\16\3\2\2\2XY\7@\2\2YZ\7?\2\2Z\20\3\2\2\2[\\"+
		"\7>\2\2\\]\7?\2\2]\22\3\2\2\2^_\7-\2\2_\24\3\2\2\2`a\7/\2\2a\26\3\2\2"+
		"\2bc\7,\2\2c\30\3\2\2\2de\7\61\2\2e\32\3\2\2\2fg\7\'\2\2g\34\3\2\2\2h"+
		"i\7`\2\2i\36\3\2\2\2jk\7#\2\2k \3\2\2\2lm\7?\2\2m\"\3\2\2\2no\7*\2\2o"+
		"$\3\2\2\2pq\7+\2\2q&\3\2\2\2rs\7}\2\2s(\3\2\2\2tu\7\177\2\2u*\3\2\2\2"+
		"vw\7v\2\2wx\7t\2\2xy\7w\2\2yz\7g\2\2z,\3\2\2\2{|\7h\2\2|}\7c\2\2}~\7n"+
		"\2\2~\177\7u\2\2\177\u0080\7g\2\2\u0080.\3\2\2\2\u0081\u0082\7k\2\2\u0082"+
		"\u0083\7h\2\2\u0083\60\3\2\2\2\u0084\u0085\7g\2\2\u0085\u0086\7n\2\2\u0086"+
		"\u0087\7u\2\2\u0087\u0088\7g\2\2\u0088\62\3\2\2\2\u0089\u008a\7r\2\2\u008a"+
		"\u008b\7t\2\2\u008b\u008c\7k\2\2\u008c\u008d\7p\2\2\u008d\u008e\7v\2\2"+
		"\u008e\64\3\2\2\2\u008f\u0090\7h\2\2\u0090\u0091\7q\2\2\u0091\u0092\7"+
		"t\2\2\u0092\66\3\2\2\2\u0093\u0094\7k\2\2\u0094\u0095\7p\2\2\u00958\3"+
		"\2\2\2\u0096\u009a\t\2\2\2\u0097\u0099\t\3\2\2\u0098\u0097\3\2\2\2\u0099"+
		"\u009c\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b:\3\2\2\2"+
		"\u009c\u009a\3\2\2\2\u009d\u009f\t\4\2\2\u009e\u009d\3\2\2\2\u009f\u00a0"+
		"\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1<\3\2\2\2\u00a2"+
		"\u00a4\t\4\2\2\u00a3\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a3\3\2"+
		"\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00ab\7\60\2\2\u00a8"+
		"\u00aa\t\4\2\2\u00a9\u00a8\3\2\2\2\u00aa\u00ad\3\2\2\2\u00ab\u00a9\3\2"+
		"\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00b5\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ae"+
		"\u00b0\7\60\2\2\u00af\u00b1\t\4\2\2\u00b0\u00af\3\2\2\2\u00b1\u00b2\3"+
		"\2\2\2\u00b2\u00b0\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b5\3\2\2\2\u00b4"+
		"\u00a3\3\2\2\2\u00b4\u00ae\3\2\2\2\u00b5>\3\2\2\2\u00b6\u00bc\7$\2\2\u00b7"+
		"\u00bb\n\5\2\2\u00b8\u00b9\7$\2\2\u00b9\u00bb\7$\2\2\u00ba\u00b7\3\2\2"+
		"\2\u00ba\u00b8\3\2\2\2\u00bb\u00be\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bd"+
		"\3\2\2\2\u00bd\u00bf\3\2\2\2\u00be\u00bc\3\2\2\2\u00bf\u00c0\7$\2\2\u00c0"+
		"@\3\2\2\2\u00c1\u00c5\7%\2\2\u00c2\u00c4\n\6\2\2\u00c3\u00c2\3\2\2\2\u00c4"+
		"\u00c7\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c8\3\2"+
		"\2\2\u00c7\u00c5\3\2\2\2\u00c8\u00c9\b!\2\2\u00c9B\3\2\2\2\u00ca\u00cb"+
		"\t\7\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00cd\b\"\2\2\u00cdD\3\2\2\2\u00ce"+
		"\u00cf\13\2\2\2\u00cfF\3\2\2\2\f\2\u009a\u00a0\u00a5\u00ab\u00b2\u00b4"+
		"\u00ba\u00bc\u00c5\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}