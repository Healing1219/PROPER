package frontend;

import java.util.HashMap;
import java.util.Scanner;

public class Lexer {

	public int lookAhead = CTokenType.UNKNOWN_TOKEN.ordinal();

	public String yytext = "";
	public int yyleng = 0;
	public int yylineno = 0;

	private String input_buffer = "";
	private String current = "";
	private HashMap<String, Integer> keywordMap = new HashMap<String, Integer>();

	public Lexer() {
		initKeyWordMap();
	}

	private void initKeyWordMap() {
		keywordMap.put("int", CTokenType.TYPE.ordinal());
		keywordMap.put("bool", CTokenType.TYPE.ordinal());
		keywordMap.put("void", CTokenType.TYPE.ordinal());
		keywordMap.put("var", CTokenType.VAR.ordinal());
		keywordMap.put("real", CTokenType.TYPE.ordinal());
		keywordMap.put("if", CTokenType.IF.ordinal());
		keywordMap.put("else", CTokenType.ELSE.ordinal());
		keywordMap.put("true", CTokenType.TRUE.ordinal());
		keywordMap.put("false", CTokenType.FALSE.ordinal());
		keywordMap.put("while", CTokenType.WHILE.ordinal());
		keywordMap.put("return", CTokenType.RETURN.ordinal());
	}

	private boolean isAlnum(char c) {
		if (Character.isAlphabetic(c) == true || Character.isDigit(c) == true||c=='.') {
			return true;
		}

		return false;
	}

	private int lex() {
		while (true) {
			while (current == "") {
				Scanner s = new Scanner(System.in);
				while (true) {
					String line = s.nextLine();
					if (line.equals("end")) {
						break;
					}
					input_buffer += line;
				}
				s.close();

				if (input_buffer.length() == 0) {
					current = "";
					return CTokenType.SEMI.ordinal();
				}

				current = input_buffer;
				++yylineno;
				current.trim();
			} // while (current == "")

			if (current.isEmpty()) {
				return CTokenType.SEMI.ordinal();
			}

			for (int i = 0; i < current.length(); i++) {
				yyleng = 0;
				yytext = current.substring(i, i + 1);
				switch (current.charAt(i)) {
				case ';':
					current = current.substring(1);
					return CTokenType.SEMI.ordinal();
				case '+':
					if (current.charAt(i + 1) == '+') {
		    			current= current.substring(2);
		    			return CTokenType.INCOP.ordinal();
		    		}
					current = current.substring(1);
					return CTokenType.PLUS.ordinal();
				case '-':
					if (current.charAt(i+1) == '-') {
		    			current = current.substring(2);
		    			return CTokenType.DECOP.ordinal();
		    		}
					current = current.substring(1);
					return CTokenType.MINUS.ordinal();
				case '*':
					current = current.substring(1);
					return CTokenType.STAR.ordinal();
				case '/':
					current = current.substring(1);
					return CTokenType.DIVOP.ordinal();
				case '&':
					current = current.substring(1);
					return CTokenType.AND.ordinal();
				case '|':
					current = current.substring(1);
					return CTokenType.OR.ordinal();
				case '(':
					current = current.substring(1);
					return CTokenType.LP.ordinal();
				case ')':
					current = current.substring(1);
					return CTokenType.RP.ordinal();
				case '[':
					current = current.substring(1);
					return CTokenType.LB.ordinal();
				case ']':
					current = current.substring(1);
					return CTokenType.RB.ordinal();
				case '{':
					current = current.substring(1);
					return CTokenType.LC.ordinal();
				case '}':
					current = current.substring(1);
					return CTokenType.RC.ordinal();
				case '=':
					if (current.charAt(i + 1) == '=') {
						current = current.substring(2);
						yytext="==";
						return CTokenType.RELOP.ordinal();
					}
					current = current.substring(1);
					return CTokenType.EQUAL.ordinal();
				case ',':
					current = current.substring(1);
					return CTokenType.COMMA.ordinal();
				case ':':
					current = current.substring(1);
					return CTokenType.COLON.ordinal();
				case '>':
				case '<':
					if (current.charAt(i + 1) == '=') {
						yytext+='=';
						current = current.substring(2);
					} else {
						current = current.substring(1);
					}
					return CTokenType.RELOP.ordinal();
				case '\n':
				case '\t':
				case ' ':
					current = current.substring(1);
					return CTokenType.WHITE_SPACE.ordinal();

				default:
					if (isAlnum(current.charAt(i)) == false) {
						return CTokenType.UNKNOWN_TOKEN.ordinal();
					} else {

						while (i < current.length() && isAlnum(current.charAt(i))) {
							i++;
							yyleng++;
						} // while (isAlnum(current.charAt(i)))

						yytext = current.substring(0, yyleng);
						current = current.substring(yyleng);
						return id_keyword_or_number();
					}

				} // switch (current.charAt(i))
			} // for (int i = 0; i < current.length(); i++)

		} // while (true)
	}// lex()
	
	private int lex(String code) {

		while (true) {

			current=code;
			current.trim();
			
			if (current.isEmpty()) {
				return CTokenType.SEMI.ordinal();
			}

			for (int i = 0; i < current.length(); i++) {
				yyleng = 0;
				yytext = current.substring(i, i + 1);
				switch (current.charAt(i)) {
				case ';':
					current = current.substring(1);
					return CTokenType.SEMI.ordinal();
				case '+':
					if (current.charAt(i + 1) == '+') {
		    			current= current.substring(2);
		    			return CTokenType.INCOP.ordinal();
		    		}
					current = current.substring(1);
					return CTokenType.PLUS.ordinal();
				case '-':
					if (current.charAt(i+1) == '-') {
		    			current = current.substring(2);
		    			return CTokenType.DECOP.ordinal();
		    		}
					current = current.substring(1);
					return CTokenType.MINUS.ordinal();
				case '*':
					current = current.substring(1);
					return CTokenType.STAR.ordinal();
				case '/':
					current = current.substring(1);
					return CTokenType.DIVOP.ordinal();
				case '&':
					current = current.substring(1);
					return CTokenType.AND.ordinal();
				case '|':
					current = current.substring(1);
					return CTokenType.OR.ordinal();
				case '(':
					current = current.substring(1);
					return CTokenType.LP.ordinal();
				case ')':
					current = current.substring(1);
					return CTokenType.RP.ordinal();
				case '[':
					current = current.substring(1);
					return CTokenType.LB.ordinal();
				case ']':
					current = current.substring(1);
					return CTokenType.RB.ordinal();
				case '{':
					current = current.substring(1);
					return CTokenType.LC.ordinal();
				case '}':
					current = current.substring(1);
					return CTokenType.RC.ordinal();
				case '=':
					if (current.charAt(i + 1) == '=') {
						current = current.substring(2);
						yytext="==";
						return CTokenType.RELOP.ordinal();
					}
					current = current.substring(1);
					return CTokenType.EQUAL.ordinal();
				case ',':
					current = current.substring(1);
					return CTokenType.COMMA.ordinal();
				case ':':
					current = current.substring(1);
					return CTokenType.COLON.ordinal();
				case '>':
				case '<':
					if (current.charAt(i + 1) == '=') {
						yytext+='=';
						current = current.substring(2);
					} else {
						current = current.substring(1);
					}
					return CTokenType.RELOP.ordinal();
				case '\n':
				case '\t':
				case ' ':
					current = current.substring(1);
					return CTokenType.WHITE_SPACE.ordinal();

				default:
					if (isAlnum(current.charAt(i)) == false) {
						return CTokenType.UNKNOWN_TOKEN.ordinal();
					} else {

						while (i < current.length() && isAlnum(current.charAt(i))) {
							i++;
							yyleng++;
						} // while (isAlnum(current.charAt(i)))

						yytext = current.substring(0, yyleng);
						current = current.substring(yyleng);
						return id_keyword_or_number();
					}

				} // switch (current.charAt(i))
			} // for (int i = 0; i < current.length(); i++)

		} // while (true)
	}// lex()

	private int id_keyword_or_number() {
		int type = CTokenType.UNKNOWN_TOKEN.ordinal();
		if (Character.isAlphabetic(yytext.charAt(0))) {
			type = isKeyWord(yytext);
		} else {
			if (isNum()) {
				type = CTokenType.NUMBER.ordinal();
			}
		}

		return type;
	}

	private boolean isNum() {
		int pos = 0;
		boolean isFloat=false;
		if (yytext.charAt(0) == '-' || yytext.charAt(0) == '+') {
			pos++;
		}

		for (; pos < yytext.length(); pos++) {
			if (Character.isDigit(yytext.charAt(pos)) != true) {
				if(yytext.charAt(pos)=='.'&&isFloat==false) {
					isFloat=true;
					pos++;
					continue;
				}
				break;
			}
		}

		return pos == yytext.length();
	}

	private int isKeyWord(String str) {

		if (keywordMap.containsKey(str)) {
			return keywordMap.get(str);
		}

		return CTokenType.NAME.ordinal();
	}

	public boolean match(int token) {
		if (lookAhead == -1) {
			lookAhead = lex();
		}

		return token == lookAhead;
	}

	public void advance() {
		lookAhead = lex();
		while (lookAhead == CTokenType.WHITE_SPACE.ordinal()) {
			lookAhead = lex();
		}
	}
	
	public void advance(String code) {
		lookAhead = lex(code);
		while (lookAhead == CTokenType.WHITE_SPACE.ordinal()) {
			lookAhead = lex(code);
		}
	}

}
