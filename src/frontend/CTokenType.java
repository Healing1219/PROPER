package frontend;

public enum CTokenType {

	// non-termals

	PROGRAM, EXT_DEF_LIST, EXT_DEF, VAR_DECL_LIST, VAR_DECL, TYPE_SPECIFIER, // 6

	FUNCT_DECL, COMPOUND_STMT, STMT_LIST, VAR_LIST, PARAM_DECL, DEF,
	NO_COMMA_EXPR, EXPR, UNARY, BINARY, STATEMENT, // 

	IF_STATEMENT, IF_ELSE_STATEMENT, TEST, BOOL, ARGS,

	NAME_NT,

	// terminals
	NAME, VAR, TYPE, AND, OR, CLASS, LP, RP, LC, RC, 
	LB, RB, NUMBER, RETURN, IF, ELSE, WHILE, STAR, COMMA, 
	COLON, SEMI, RELOP, WHITE_SPACE, PLUS, MINUS, DIVOP, EQUAL, INCOP, DECOP, TRUE, FALSE, PROB, UNKNOWN_TOKEN;

	public static final int FIRST_TERMINAL_INDEX = NAME.ordinal();
	public static final int LAST_TERMINAL_INDEX = UNKNOWN_TOKEN.ordinal();

	public static final int FIRST_NON_TERMINAL_INDEX = PROGRAM.ordinal();
	public static final int LAST_NON_TERMINAL_INDEX = NAME_NT.ordinal();

	public static String getSymbolStr(int val) {
		return CTokenType.values()[val].toString();
	}

	public static boolean isTerminal(int val) {
		if (FIRST_TERMINAL_INDEX <= val && val <= LAST_TERMINAL_INDEX) {
			return true;
		}

		return false;
	}

}
