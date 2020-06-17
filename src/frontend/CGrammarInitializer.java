package frontend;

import java.util.ArrayList;
import java.util.HashMap;

public class CGrammarInitializer {
	//public static final int Var_VarDeclList_Colon_TypeSpecifier_Semi_TO_ExtDef = 3;
	public static final int VarDeclList_COMMA_VarDecl_TO_VarDeclList = 4;
	public static final int NAME_TO_VarDecl = 5;
	public static final int TYPE_TO_TYPE_SPECIFIER = 6;

	public static final int VarDecl_LP_VarList_RP_TO_FunctDecl = 8;
	public static final int VarDecl_LP_RP_TO_FunctDecl = 9;
	public static final int VarList_COMMA_ParamDecl_TO_VarList = 13;
	public static final int TypeSpecifier_VarDecl_TO_ParamDecl = 14;
	public static final int Var_DeclList_Colon_TypeSpecifier_Semi_TO_Def = 15;
	public static final int NAME_TO_Unary = 24;

	public static final int Unary_TO_Binary = 19;
	public static final int NoCommaExpr_PLUS_BINARY_TO_NoCommaExpr = 39;
	public static final int NoCommaExpr_MINUS_BINARY_TO_NoCommaExpr = 40;
	public static final int Binary_Star_Unary_TO_Binary = 41;
	public static final int Binary_DivOP_Unary_TO_Binary = 42;
	public static final int NoCommaExpr_RelOP_NoCommaExpr_TO_NoCommaExpr = 20;
	public static final int Binary_OR_Binary_TO_Binary=43;
	public static final int Binary_AND_Binary_TO_Binary = 44;

	public static final int NoCommaExpr_TO_Expr = 16;

	public static final int NoCommaExpr_Equal_NoCommaExpr_TO_Expr = 17;
	public static final int Binary_TO_NoCommaExpr = 18;

	public static final int True_TO_Bool = 25;
	public static final int False_TO_Bool = 26;
	public static final int Number_TO_Unary = 22;
	public static final int Name_TO_Unary = 23;
	public static final int Bool_TO_Unary = 24;
	public static final int Expr_Semi_TO_Statement = 28;
	public static final int Statement_TO_StmtList = 31;
	public static final int Def_TO_Statement = 27;
	public static final int StmtList_Statement_TO_StmtList = 30;
	public static final int No_Comma_Expr_TO_Test = 37;
	//public static final int Bool_TO_Test = 39;
	public static final int If_Test_Statement_TO_IFStatement = 33;
	public static final int IfElseStatemnt_Else_Statemenet_TO_IfElseStatement = 35;
	public static final int While_LP_Test_RP_TO_Statement = 38;

	public static final int TypeSpecifier_FunctDecl_CompoundStmt_TO_ExtDef = 7;
	public static final int Unary_LP_RP_TO_Unary = 48;
	public static final int Unary_LP_ARGS_RP_TO_Unary = 47;
	public static final int VarDecl_TO_VarDeclList = 3;
	public static final int NoCommaExpr_TO_Args = 49;
	public static final int NoCommaExpr_Comma_Args_TO_Args = 50;
	public static final int Return_Expr_Semi_TO_Statement = 29;
	public static final int Unary_Incop_TO_Unary = 51;
	public static final int Incop_Unary_TO_Unary = 52;
	public static final int Unary_Decop_TO_Unary = 53;
	public static final int Decop_Unary_TO_Unary = 54;
	public static final int Minus_Unary_TO_Unary = 45;

	private int productionNum = 0;

	private static CGrammarInitializer instance = null;
	private HashMap<Integer, ArrayList<Production>> productionMap = new HashMap<Integer, ArrayList<Production>>();
	private HashMap<Integer, Symbols> symbolMap = new HashMap<Integer, Symbols>();
	private ArrayList<Symbols> symbolArray = new ArrayList<Symbols>();

	public static CGrammarInitializer getInstance() {
		if (instance == null) {
			instance = new CGrammarInitializer();
		}
		return instance;
	}

	private CGrammarInitializer() {
		initVariableDecalationProductions();
		initFunctionDefinition();
		initFunctionDefinitionWithIfElse();
		initFunctionDefinitionWithLoop();
		initComputingOperation();
		addTerminalToSymbolMapAndArray();
	}

	public HashMap<Integer, ArrayList<Production>> getProductionMap() {
		return productionMap;
	}

	public HashMap<Integer, Symbols> getSymbolMap() {
		return symbolMap;
	}

	public ArrayList<Symbols> getSymbolArray() {
		return symbolArray;
	}

	public void initVariableDecalationProductions() {

		productionMap.clear();

		/*
		 * 
		 * variable declaration grammar
		 * 
		 * PROGRAM -> EXT_DEF_LIST
		 * 
		 * EXT_DEF_LIST -> EXT_DEF_LIST EXT_DEF | EXT_DEF
		 * 
		 * EXT_DEF -> VAR VAR_DECL_LIST COLON TYPE_SPECIFIER SEMI
		 * 
		 * VAR_DECL_LIST -> VAR_DECL | VAR_DECL_LIST COMMA VAR_DECL
		 * 
		 * VAR_DECL -> NAME
		 * 
		 * TYPE_SPECIFIER -> TYPE
		 */

		// PROGRAM -> EXT_DEF_LIST
		ArrayList<Integer> right = null;
		right = getProductionRight(new int[] { CTokenType.EXT_DEF_LIST.ordinal() });
		Production production = new Production(productionNum, CTokenType.PROGRAM.ordinal(), 0, right);
		productionNum++;
		addProduction(production, true);

		// EXT_DEF_LIST -> EXT_DEF_LIST EXT_DEF
		right = getProductionRight(new int[] { CTokenType.EXT_DEF_LIST.ordinal(), CTokenType.EXT_DEF.ordinal() });
		production = new Production(productionNum, CTokenType.EXT_DEF_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, true);

		// EXT_DEF_LIST -> EXT_DEF
		right = getProductionRight(new int[] { CTokenType.EXT_DEF.ordinal() });
		production = new Production(productionNum, CTokenType.EXT_DEF_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, true);

		// EXT_DEF -> VAR VAR_DECL_LIST COLON TYPE_SPECIFIER SEMI
//		right = getProductionRight(new int[] { CTokenType.VAR.ordinal(), CTokenType.VAR_DECL_LIST.ordinal(),
//				CTokenType.COLON.ordinal(), CTokenType.TYPE_SPECIFIER.ordinal(), CTokenType.SEMI.ordinal() });
//		production = new Production(productionNum, CTokenType.EXT_DEF.ordinal(), 0, right);
//		productionNum++;
//		addProduction(production, false);

		// VAR_DECL_LIST -> VAR_DECL
		right = getProductionRight(new int[] { CTokenType.VAR_DECL.ordinal() });
		production = new Production(productionNum, CTokenType.VAR_DECL_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// VAR_DECL_LIST -> VAR_DECL_LIST COMMA VAR_DECL
		right = getProductionRight(new int[] { CTokenType.VAR_DECL_LIST.ordinal(), CTokenType.COMMA.ordinal(),
				CTokenType.VAR_DECL.ordinal() });
		production = new Production(productionNum, CTokenType.VAR_DECL_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// VAR_DECL -> NAME
		right = getProductionRight(new int[] { CTokenType.NAME.ordinal() });
		production = new Production(productionNum, CTokenType.VAR_DECL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// TYPE_SPECIFIER -> TYPE
		right = getProductionRight(new int[] { CTokenType.TYPE.ordinal() });
		production = new Production(productionNum, CTokenType.TYPE_SPECIFIER.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

	}

	private void initFunctionDefinition() {
		/*
		 * 
		 * EXT_DEF -> TYPE_SPECIFIER FUNCT_DECL COMPOUND_STMT
		 * 
		 * FUNCT_DECL -> VAR_DECL LP VAR_LIST RP | VAR_DECL LP RP
		 * 
		 * COMPOUND_STMT-> LC STMT_LIST RC | LC RC
		 * 
		 * VAR_LIST -> PARAM_DECL | VAR_LIST COMMA PARAM_DECL
		 * 
		 * PARAM_DECL -> TYPE_SPECIFIER VAR_DECL
		 * 
		 * DEF -> VAR VAR_DECL_LIST COLON TYPE_SPECIFIER SEMI
		 * 
		 * EXPR -> NO_COMMA_EXPR
		 * 
		 * NO_COMMA_EXPR -> NO_COMMA_EXPR EQUAL NO_COMMA_EXPR | BINARY
		 * 
		 * BINARY -> UNARY | BINARY RELOP BINARY | BINARY EQUAL BINARY
		 * 
		 * UNARY -> NAME | NUMBER | BOOL
		 * 
		 * BOOL -> TRUE | FALSE
		 * 
		 * STATEMENT -> DEF | EXPR SEMI | RETURN EXPR SEMI
		 * 
		 * STMT_LIST -> STMT_LIST STATEMENT | STATEMENT
		 * 
		 * STATEMENT -> COMPOUND_STMT
		 * 
		 */

		ArrayList<Integer> right = null;
		Production production = null;
		// EXT_DEF -> OPT_SPECIFIERS FUNCT_DECL COMPOUND_STMT
		right = getProductionRight(new int[] { CTokenType.TYPE_SPECIFIER.ordinal(), CTokenType.FUNCT_DECL.ordinal(),
				CTokenType.COMPOUND_STMT.ordinal() });
		production = new Production(productionNum, CTokenType.EXT_DEF.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// FUNCT_DECL -> VAR_DECL LP VAR_LIST RP
		right = getProductionRight(new int[] { CTokenType.VAR_DECL.ordinal(), CTokenType.LP.ordinal(),
				CTokenType.VAR_LIST.ordinal(), CTokenType.RP.ordinal() });
		production = new Production(productionNum, CTokenType.FUNCT_DECL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// FUNCT_DECL -> VAR_DECL LP RP
		right = getProductionRight(
				new int[] { CTokenType.VAR_DECL.ordinal(), CTokenType.LP.ordinal(), CTokenType.RP.ordinal() });
		production = new Production(productionNum, CTokenType.FUNCT_DECL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// COMPOUND_STMT-> LC STMT_LIST RC
		right = getProductionRight(
				new int[] { CTokenType.LC.ordinal(), CTokenType.STMT_LIST.ordinal(), CTokenType.RC.ordinal() });
		production = new Production(productionNum, CTokenType.COMPOUND_STMT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// COMPOUND_STMT-> LC RC
		right = getProductionRight(new int[] { CTokenType.LC.ordinal(), CTokenType.RC.ordinal() });
		production = new Production(productionNum, CTokenType.COMPOUND_STMT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// VAR_LIST -> PARAM_DECLARATION
		right = getProductionRight(new int[] { CTokenType.PARAM_DECL.ordinal() });
		production = new Production(productionNum, CTokenType.VAR_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// VAR_LIST -> VAR_LIST COMMA PARAM_DECL
		right = getProductionRight(new int[] { CTokenType.VAR_LIST.ordinal(), CTokenType.COMMA.ordinal(),
				CTokenType.PARAM_DECL.ordinal() });
		production = new Production(productionNum, CTokenType.VAR_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// PARAM_DECL -> TYPE_SPECIFIER VAR_DECL
		right = getProductionRight(new int[] { CTokenType.TYPE_SPECIFIER.ordinal(), CTokenType.VAR_DECL.ordinal() });
		production = new Production(productionNum, CTokenType.PARAM_DECL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// DEF -> VAR VAR_DECL_LIST COLON TYPE_SPECIFIER SEMI
		right = getProductionRight(new int[] { CTokenType.VAR.ordinal(), CTokenType.VAR_DECL_LIST.ordinal(),
				CTokenType.COLON.ordinal(), CTokenType.TYPE_SPECIFIER.ordinal(), CTokenType.SEMI.ordinal() });
		production = new Production(productionNum, CTokenType.DEF.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// EXPR -> NO_COMMA_EXPR
		right = getProductionRight(new int[] { CTokenType.NO_COMMA_EXPR.ordinal() });
		production = new Production(productionNum, CTokenType.EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// EXPR -> NO_COMMA_EXPR EQUAL NO_COMMA_EXPR
		right = getProductionRight(new int[] { CTokenType.NO_COMMA_EXPR.ordinal(), CTokenType.EQUAL.ordinal(),
				CTokenType.NO_COMMA_EXPR.ordinal() });
		production = new Production(productionNum, CTokenType.EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// NO_COMMA_EXPR ->BINARY
		right = getProductionRight(new int[] { CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.NO_COMMA_EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> UNARY
		right = getProductionRight(new int[] { CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// NO_COMMA_EXPR -> NO_COMMA_EXPR RELOP NO_COMMA_EXPR
		right = getProductionRight(
				new int[] { CTokenType.NO_COMMA_EXPR.ordinal(), CTokenType.RELOP.ordinal(), CTokenType.NO_COMMA_EXPR.ordinal() });
		production = new Production(productionNum, CTokenType.NO_COMMA_EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> BINARY EQUAL BINARY
		right = getProductionRight(
				new int[] { CTokenType.BINARY.ordinal(), CTokenType.EQUAL.ordinal(), CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> NUMBER
		right = getProductionRight(new int[] { CTokenType.NUMBER.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> NAME
		right = getProductionRight(new int[] { CTokenType.NAME.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> BOOL
		right = getProductionRight(new int[] { CTokenType.BOOL.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BOOL -> TRUE
		right = getProductionRight(new int[] { CTokenType.TRUE.ordinal() });
		production = new Production(productionNum, CTokenType.BOOL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BOOL -> FALSE
		right = getProductionRight(new int[] { CTokenType.FALSE.ordinal() });
		production = new Production(productionNum, CTokenType.BOOL.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// STATEMENT -> DEF
		right = getProductionRight(new int[] { CTokenType.DEF.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// STATEMENT -> EXPR SEMI
		right = getProductionRight(new int[] { CTokenType.EXPR.ordinal(), CTokenType.SEMI.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// STATEMENT -> RETURN EXPR SEMI
		right = getProductionRight(
				new int[] { CTokenType.RETURN.ordinal(), CTokenType.EXPR.ordinal(), CTokenType.SEMI.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// STMT_LIST -> STMT_LIST STATEMENT
		right = getProductionRight(new int[] { CTokenType.STMT_LIST.ordinal(), CTokenType.STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.STMT_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, true);

		// STMT_LIST -> STATEMENT
		right = getProductionRight(new int[] { CTokenType.STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.STMT_LIST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, true);

		// STATEMENT -> COMPOUND_STMT
		right = getProductionRight(new int[] { CTokenType.COMPOUND_STMT.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);
	}

	private void initFunctionDefinitionWithIfElse() {

		/*
		 * 34
		 * 
		 * IF_STATEMENT -> IF LP TEST RP STATEMENT
		 * 
		 * IF_ELSE_STATEMENT -> IF_STATEMENT
		 * 
		 * IF_ELSE_STATEMENT ->IF_ELSE_STATEMENT ELSE STATEMENT
		 * 
		 * STATEMENT -> IF_ELSE_STATEMENT
		 * 
		 * TEST -> NO_COMMA_EXPR | BOOL
		 * 
		 */

		ArrayList<Integer> right = null;
		Production production = null;
		// IF_STATEMENT -> IF LP TEST RP LC STATEMENT RC
		right = getProductionRight(new int[] { CTokenType.IF.ordinal(), CTokenType.LP.ordinal(),
				CTokenType.TEST.ordinal(), CTokenType.RP.ordinal(), CTokenType.STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.IF_STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// IF_ELSE_STATEMENT -> IF_STATEMENT
		right = getProductionRight(new int[] { CTokenType.IF_STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.IF_ELSE_STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// IF_ELSE_STATEMENT ->IF_ELSE_STATEMENT ELSE LC STATEMENT RC
		right = getProductionRight(new int[] { CTokenType.IF_ELSE_STATEMENT.ordinal(), CTokenType.ELSE.ordinal(),
				CTokenType.STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.IF_ELSE_STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// STATEMENT -> IF_ELSE_STATEMENT
		right = getProductionRight(new int[] { CTokenType.IF_ELSE_STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// TEST -> NO_COMMA_EXPR
		right = getProductionRight(new int[] { CTokenType.NO_COMMA_EXPR.ordinal() });
		production = new Production(productionNum, CTokenType.TEST.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// TEST -> BOOL
//		right = getProductionRight(new int[] { CTokenType.BOOL.ordinal() });
//		production = new Production(productionNum, CTokenType.TEST.ordinal(), 0, right);
//		productionNum++;
//		addProduction(production, false);
	}

	private void initFunctionDefinitionWithLoop() {
		// STATEMENT -> WHILE LP TEST RP STATEMENT
		ArrayList<Integer> right = null;
		Production production = null;
		right = getProductionRight(new int[] { CTokenType.WHILE.ordinal(), CTokenType.LP.ordinal(),
				CTokenType.TEST.ordinal(), CTokenType.RP.ordinal(), CTokenType.STATEMENT.ordinal() });
		production = new Production(productionNum, CTokenType.STATEMENT.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);
	}

	private void initComputingOperation() {

		/*
		 * 
		 * NO_COMMA_EXPR -> NO_COMMA_EXPR PLUS BINARY
		 * 
		 * NO_COMMA_EXPR -> NO_COMMA_EXPR MINUS BINARY
		 * 
		 * BINARY -> UNARY STAR BINARY
		 * 
		 * BINARY -> UNARY STAR BINARY
		 * 
		 * BINARY -> BINARY OR BINARY
		 * 
		 * BINARY -> BINARY AND BINARY
		 * 
		 * UNARY -> MINUS UNARY
		 * 
		 * UNARY -> UNARY LB EXPR RB
		 * 
		 * UNARY -> UNARY LP ARGS RP
		 * 
		 * UNARY -> UNARY LP RP
		 * 
		 * ARGS -> NO_COMMA_EXPR
		 * 
		 * ARGS -> NO_COMMA_EXPR COMMA ARGS
		 * 
		 */

		ArrayList<Integer> right = null;
		Production production = null;
		// NO_COMMA_EXPR -> BINARY PLUS NO_COMMA_EXPR
		right = getProductionRight(
				new int[] { CTokenType.NO_COMMA_EXPR.ordinal(), CTokenType.PLUS.ordinal(), CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.NO_COMMA_EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// NO_COMMA_EXPR -> BINARY MINUS NO_COMMA_EXPR
		right = getProductionRight(
				new int[] { CTokenType.NO_COMMA_EXPR.ordinal(), CTokenType.MINUS.ordinal(), CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.NO_COMMA_EXPR.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> BINARY STAR UNARY
		right = getProductionRight(
				new int[] { CTokenType.BINARY.ordinal(), CTokenType.STAR.ordinal(), CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> BINARY DIVOP UNARY
		right = getProductionRight(
				new int[] { CTokenType.BINARY.ordinal(), CTokenType.DIVOP.ordinal(), CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> BINARY AND BINARY
		right = getProductionRight(
				new int[] { CTokenType.BINARY.ordinal(), CTokenType.AND.ordinal(), CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// BINARY -> BINARY OR BINARY
		right = getProductionRight(
				new int[] { CTokenType.BINARY.ordinal(), CTokenType.OR.ordinal(), CTokenType.BINARY.ordinal() });
		production = new Production(productionNum, CTokenType.BINARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> MINUS UNARY a = -a
		right = getProductionRight(new int[] { CTokenType.MINUS.ordinal(), CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> UNARY LB EXPR RB b = a[2];
		right = getProductionRight(new int[] { CTokenType.UNARY.ordinal(), CTokenType.LB.ordinal(),
				CTokenType.EXPR.ordinal(), CTokenType.RB.ordinal() });

		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> UNARY LP ARGS RP fun(a, b ,c)
		right = getProductionRight(new int[] { CTokenType.UNARY.ordinal(), CTokenType.LP.ordinal(),
				CTokenType.ARGS.ordinal(), CTokenType.RP.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> UNARY LP RP fun()
		right = getProductionRight(
				new int[] { CTokenType.UNARY.ordinal(), CTokenType.LP.ordinal(), CTokenType.RP.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// ARGS -> NO_COMMA_EXPR
		right = getProductionRight(new int[] { CTokenType.NO_COMMA_EXPR.ordinal() });
		production = new Production(productionNum, CTokenType.ARGS.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// ARGS -> NO_COMMA_EXPR COMMA ARGS
		right = getProductionRight(new int[] { CTokenType.NO_COMMA_EXPR.ordinal(), CTokenType.COMMA.ordinal(),
				CTokenType.ARGS.ordinal() });
		production = new Production(productionNum, CTokenType.ARGS.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> UNARY INCOP i++
		right = getProductionRight(new int[] { CTokenType.UNARY.ordinal(), CTokenType.INCOP.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> INCOP UNARY ++i
		right = getProductionRight(new int[] { CTokenType.INCOP.ordinal(), CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> UNARY DECOP i--
		right = getProductionRight(new int[] { CTokenType.UNARY.ordinal(), CTokenType.DECOP.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);

		// UNARY -> DECOP UNARY --i
		right = getProductionRight(new int[] { CTokenType.DECOP.ordinal(), CTokenType.UNARY.ordinal() });
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);
		
		// UNARY -> LP NO_COMMA_EXPR RP (a)
		right = getProductionRight(new int[] { CTokenType.LP.ordinal(), CTokenType.NO_COMMA_EXPR.ordinal() , CTokenType.RP.ordinal()});
		production = new Production(productionNum, CTokenType.UNARY.ordinal(), 0, right);
		productionNum++;
		addProduction(production, false);
	}

	// 添加语法规则
	private void addProduction(Production production, boolean nullable) {

		ArrayList<Production> productionList = productionMap.get(production.getLeft());

		if (productionList == null) {
			productionList = new ArrayList<Production>();
			productionMap.put(production.getLeft(), productionList);
		}

		if (productionList.contains(production) == false) {
			productionList.add(production);
		}

		addSymbolMapAndArray(production, nullable);

	}

	private void addSymbolMapAndArray(Production production, boolean nullable) {
		// add symbol array and symbol map
		int[] right = new int[production.getRight().size()];
		for (int i = 0; i < right.length; i++) {
			right[i] = production.getRight().get(i);
		}

		if (symbolMap.containsKey(production.getLeft())) {
			symbolMap.get(production.getLeft()).addProduction(right);
		} else {
			ArrayList<int[]> productions = new ArrayList<int[]>();
			productions.add(right);
			Symbols symObj = new Symbols(production.getLeft(), nullable, productions);
			symbolMap.put(production.getLeft(), symObj);
			symbolArray.add(symObj);
		}
	}

	private void addTerminalToSymbolMapAndArray() {
		for (int i = CTokenType.FIRST_TERMINAL_INDEX; i <= CTokenType.LAST_TERMINAL_INDEX; i++) {
			Symbols symObj = new Symbols(i, false, null);
			symbolMap.put(i, symObj);
			symbolArray.add(symObj);
		}
	}

	private ArrayList<Integer> getProductionRight(int[] arr) {
		ArrayList<Integer> right = new ArrayList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			right.add(arr[i]);
		}

		return right;
	}

}
