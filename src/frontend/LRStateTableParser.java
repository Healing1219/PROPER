package frontend;

import java.util.HashMap;
import java.util.Stack;

import backend.CodeTreeBuilder;
import backend.Util;

public class LRStateTableParser {
	private Lexer lexer;
	int lexerInput = 0;
	int nestingLevel = 0;
	String text = "";
	public static final String GLOBAL_SCOPE = "global";
	public String symbolScope = GLOBAL_SCOPE;

	private Object attributeForParentNode = null;
	private TypeSystem typeSystem = TypeSystem.getTypeSystem();
	private Stack<Object> valueStack;
	private Stack<Integer> parseStack;
	private Stack<Integer> statusStack;
	HashMap<Integer, HashMap<Integer, Integer>> lrStateTable = null;
	CodeTreeBuilder codeTreeBuilder = CodeTreeBuilder.getCodeTreeBuilder();

	public LRStateTableParser(Lexer lexer) {
		this.lexer = lexer;
		this.statusStack = new Stack<Integer>();
		this.parseStack = new Stack<Integer>();
		this.valueStack = new Stack<Object>();
		statusStack.push(0);
		valueStack.push(null);
		lexer.advance();
		lexerInput = CTokenType.EXT_DEF_LIST.ordinal();
		lrStateTable = GrammarStateManager.getGrammarManager().getLRStateTable();
		codeTreeBuilder.setParser(this);
	}

	public LRStateTableParser(Lexer lexer, String code) {
		this.lexer = lexer;
		this.statusStack = new Stack<Integer>();
		this.parseStack = new Stack<Integer>();
		this.valueStack = new Stack<Object>();
		typeSystem.symbolTable.clear();
		statusStack.push(0);
		valueStack.push(null);
		lexer.advance(code);
		lexerInput = CTokenType.EXT_DEF_LIST.ordinal();
		lrStateTable = GrammarStateManager.getGrammarManager().getLRStateTable();
		codeTreeBuilder.setParser(this);
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public int getCurrentLevel() {
		return nestingLevel;
	}

	private String relOperatorText;

	public String getRelOperatorText() {
		return relOperatorText;
	}

	private void showCurrentStateInfo(int stateNum) {
		System.out.println("current input is :" + CTokenType.getSymbolStr(lexerInput));

		System.out.println("current state is: ");
		GrammarState state = GrammarStateManager.getGrammarManager().getGrammarState(stateNum);
		state.print();
	}

	public boolean parse() {

		while (true) {

			Integer action = getAction(statusStack.peek(), lexerInput);

			if (action == null) {
				// 解析出错
				System.out.println("Shift for input: " + CTokenType.values()[lexerInput].toString());
				System.err.println("The input is denied");
				return false;
			}

			if (action > 0) {
				// showCurrentStateInfo(action);

				// shift 操作
				statusStack.push(action);
				text = lexer.yytext;
				if (lexerInput == CTokenType.RELOP.ordinal()) {
					relOperatorText = text;
				}
				parseStack.push(lexerInput);

				if (CTokenType.isTerminal(lexerInput)) {
					if (Util.debug) {
						System.out.println("Shift for input: " + CTokenType.values()[lexerInput].toString());
					}
					Object obj = takeActionForShift(lexerInput);

					lexer.advance();
					lexerInput = lexer.lookAhead;
					valueStack.push(obj);
				} else {
					lexerInput = lexer.lookAhead;
				}

			} else {
				if (action == 0) {
					System.out.println("The input can be accepted.");
					return true;
				}

				int reduceProduction = -action;
				Production product = ProductionManager.getProductionManager().getProductionByIndex(reduceProduction);
				if (Util.debug) {
					System.out.println("reduce by product: ");
					product.print();
				}

				takeActionForReduce(reduceProduction);

				int rightSize = product.getRight().size();
				while (rightSize > 0) {
					parseStack.pop();
					valueStack.pop();
					statusStack.pop();
					rightSize--;
				}

				lexerInput = product.getLeft();
				parseStack.push(lexerInput);
				valueStack.push(attributeForParentNode);
			}
		}
	}

	private Object takeActionForShift(int token) {
		if (token == CTokenType.LP.ordinal() || token == CTokenType.LC.ordinal()) {
			nestingLevel++;
		}
		if (token == CTokenType.RP.ordinal() || token == CTokenType.RC.ordinal()) {
			nestingLevel--;
		}
		return null;
	}

	private void takeActionForReduce(int productNum) {
		switch (productNum) {
		case CGrammarInitializer.TYPE_TO_TYPE_SPECIFIER:
			attributeForParentNode = typeSystem.newType(text);
			break;
		case CGrammarInitializer.NAME_TO_VarDecl:
		case CGrammarInitializer.NAME_TO_Unary:
			attributeForParentNode = typeSystem.newSymbol(text, nestingLevel);
			break;
		case CGrammarInitializer.VarDeclList_COMMA_VarDecl_TO_VarDeclList:
		case CGrammarInitializer.VarList_COMMA_ParamDecl_TO_VarList: {
			Symbol currentSym = (Symbol) attributeForParentNode;
			Symbol lastSym = (Symbol) valueStack.get(valueStack.size() - 3);
			currentSym.setNextSymbol(lastSym);
		}
			break;
		// case CGrammarInitializer.Var_VarDeclList_Colon_TypeSpecifier_Semi_TO_ExtDef:
		case CGrammarInitializer.Var_DeclList_Colon_TypeSpecifier_Semi_TO_Def:
			TypeLink specifier = (TypeLink) attributeForParentNode;
			Symbol symbol = (Symbol) (valueStack.get(valueStack.size() - 4));
			typeSystem.addSpecifierToDeclaration(specifier, symbol);
			typeSystem.addSymbolsToTable(symbol, symbolScope);
			break;
		case CGrammarInitializer.TypeSpecifier_VarDecl_TO_ParamDecl:
			Symbol symbol1 = (Symbol) attributeForParentNode;
			TypeLink specifier1 = (TypeLink) (valueStack.get(valueStack.size() - 2));
			typeSystem.addSpecifierToDeclaration(specifier1, symbol1);
			typeSystem.addSymbolsToTable(symbol1, symbolScope);
			break;
		case CGrammarInitializer.VarDecl_LP_VarList_RP_TO_FunctDecl:
			setFunctionSymbol(true);
			Symbol argList = (Symbol) valueStack.get(valueStack.size() - 2);
			((Symbol) attributeForParentNode).args = argList;
			typeSystem.addSymbolsToTable((Symbol) attributeForParentNode, symbolScope);
			// 遇到函数定义，变量的scope名称要改为函数名,并把函数参数的scope改为函数名
			symbolScope = ((Symbol) attributeForParentNode).getName();
			Symbol sym = argList;
			while (sym != null) {
				sym.addScope(symbolScope);
				sym = sym.getNextSymbol();
			}
			break;
		case CGrammarInitializer.VarDecl_LP_RP_TO_FunctDecl:
			setFunctionSymbol(false);
			typeSystem.addSymbolsToTable((Symbol) attributeForParentNode, symbolScope);
			// 遇到函数定义，变量的scope名称要改为函数名
			symbolScope = ((Symbol) attributeForParentNode).getName();
			break;
		case CGrammarInitializer.TypeSpecifier_FunctDecl_CompoundStmt_TO_ExtDef:
			symbol = (Symbol) valueStack.get(valueStack.size() - 2);
			specifier = (TypeLink) (valueStack.get(valueStack.size() - 3));
			typeSystem.addSpecifierToDeclaration(specifier, symbol);

			// 函数定义结束后，接下来的变量作用范围应该改为global
			symbolScope = GLOBAL_SCOPE;
			break;
		}

		codeTreeBuilder.buildCodeTree(productNum, text);
	}

	private void setFunctionSymbol(boolean hasArgs) {
		Symbol funcSymbol = null;
		if (hasArgs) {
			funcSymbol = (Symbol) valueStack.get(valueStack.size() - 4);
		} else {
			funcSymbol = (Symbol) valueStack.get(valueStack.size() - 3);
		}

		typeSystem.addDeclarator(funcSymbol, Declarator.FUNCTION);
		attributeForParentNode = funcSymbol;
	}

	private Integer getAction(Integer currentState, Integer currentInput) {
		HashMap<Integer, Integer> jump = lrStateTable.get(currentState);
		if (jump != null) {
			Integer next = jump.get(currentInput);
			if (next != null) {
				return next;
			}
		}

		return null;
	}

}
