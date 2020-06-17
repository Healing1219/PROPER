package backend;

import java.util.HashMap;
import java.util.Stack;

import frontend.CGrammarInitializer;
import frontend.CTokenType;
import frontend.LRStateTableParser;

import frontend.Symbol;
import frontend.TypeSystem;

public class CodeTreeBuilder {
	private Stack<ICodeNode> codeNodeStack = new Stack<ICodeNode>();
	private LRStateTableParser parser = null;
	private TypeSystem typeSystem = null;
	private String functionName;
    private HashMap<String, ICodeNode> funcMap = new HashMap<String , ICodeNode>();
    int path_no=0;

	private static CodeTreeBuilder treeBuilder = null;

	public static CodeTreeBuilder getCodeTreeBuilder() {
		if (treeBuilder == null) {
			treeBuilder = new CodeTreeBuilder();
		}

		return treeBuilder;
	}

	public ICodeNode getFunctionNodeByName(String name) {
    	return funcMap.get(name);
    }
	
	public void setParser(LRStateTableParser parser) {
		this.parser = parser;
		typeSystem = parser.getTypeSystem();
	}

	public ICodeNode buildCodeTree(int production, String text) {
		ICodeNode node = null;
		Symbol symbol = null;
		
		switch (production) {
		case CGrammarInitializer.True_TO_Bool:
		case CGrammarInitializer.False_TO_Bool:
			node = ICodeFactory.createICodeNode(CTokenType.BOOL);
			node.setAttribute(ICodeKey.TEXT, text);
			break;
		
		case CGrammarInitializer.Bool_TO_Unary:
			node = ICodeFactory.createICodeNode(CTokenType.UNARY);
			ICodeNode child = codeNodeStack.pop();
			String t = (String) child.getAttribute(ICodeKey.TEXT);
			node.setAttribute(ICodeKey.TEXT, t);
			break;
			
		case CGrammarInitializer.Number_TO_Unary:
		case CGrammarInitializer.Name_TO_Unary:
			node = ICodeFactory.createICodeNode(CTokenType.UNARY);
			if (production == CGrammarInitializer.Name_TO_Unary) {
				symbol =typeSystem.getSymbolByText(text, parser.getCurrentLevel());
    			node.setAttribute(ICodeKey.SYMBOL, symbol);
			}
			node.setAttribute(ICodeKey.TEXT, text);
			break;
		
		case CGrammarInitializer.Minus_Unary_TO_Unary:
			node = ICodeFactory.createICodeNode(CTokenType.UNARY);
			child = codeNodeStack.pop();
			t = (String) child.getAttribute(ICodeKey.TEXT);
			boolean isFloat = t.indexOf('.') != -1;
    		if (isFloat) {
    			node.setAttribute(ICodeKey.VALUE, Float.valueOf(t)*(-1));	
    			node.setAttribute(ICodeKey.TEXT, Float.valueOf(t)*(-1)+"");	
    		} else {
    			node.setAttribute(ICodeKey.VALUE, Integer.parseInt(t)*(-1));	
    			node.setAttribute(ICodeKey.TEXT, Integer.parseInt(t)*(-1)+"");	
    		}
			break;
		
		case CGrammarInitializer.Unary_TO_Binary:
			node = ICodeFactory.createICodeNode(CTokenType.BINARY);
			child = codeNodeStack.pop();
			t = (String) child.getAttribute(ICodeKey.TEXT);
			node.setAttribute(ICodeKey.TEXT, child.getAttribute(ICodeKey.TEXT));
			node.addChild(child);
			break;

		case CGrammarInitializer.Binary_TO_NoCommaExpr:
			node = ICodeFactory.createICodeNode(CTokenType.NO_COMMA_EXPR);
			child = codeNodeStack.pop();
			t = (String) child.getAttribute(ICodeKey.TEXT);
			node.addChild(child);
			break;
			
		case CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr:
			node = ICodeFactory.createICodeNode(CTokenType.EXPR);
			child = codeNodeStack.pop();
			t = (String) child.getAttribute(ICodeKey.TEXT);
			node.addChild(child);
			ICodeNode operator = ICodeFactory.createICodeNode(CTokenType.EQUAL);
			node.addChild(operator);
			child = codeNodeStack.pop();
			t = (String) child.getAttribute(ICodeKey.TEXT);
			node.addChild(child);
			break;
			
		case CGrammarInitializer.Unary_Incop_TO_Unary:
    		node = ICodeFactory.createICodeNode(CTokenType.UNARY);
    		node.addChild(codeNodeStack.pop());
    		break;
    	
		case CGrammarInitializer.Unary_Decop_TO_Unary:
    		node = ICodeFactory.createICodeNode(CTokenType.UNARY);
    		node.addChild(codeNodeStack.pop());
    		break;

		case CGrammarInitializer.NoCommaExpr_PLUS_BINARY_TO_NoCommaExpr:
			node = ICodeFactory.createICodeNode(CTokenType.NO_COMMA_EXPR);
			node.addChild(codeNodeStack.pop());
			node.addChild(codeNodeStack.pop());
			break;
		
		case CGrammarInitializer.NoCommaExpr_MINUS_BINARY_TO_NoCommaExpr:
			node = ICodeFactory.createICodeNode(CTokenType.NO_COMMA_EXPR);
			node.addChild(codeNodeStack.pop());
			node.addChild(codeNodeStack.pop());
			break;
		
		case CGrammarInitializer.Binary_DivOP_Unary_TO_Binary:
			node = ICodeFactory.createICodeNode(CTokenType.BINARY);
			node.addChild(codeNodeStack.pop());
			node.addChild(codeNodeStack.pop());
			break;
		
		case CGrammarInitializer.Binary_Star_Unary_TO_Binary:
			node = ICodeFactory.createICodeNode(CTokenType.BINARY);
			node.addChild(codeNodeStack.pop());
			node.addChild(codeNodeStack.pop());
			break;

		case CGrammarInitializer.NoCommaExpr_RelOP_NoCommaExpr_TO_NoCommaExpr:
			node = ICodeFactory.createICodeNode(CTokenType.NO_COMMA_EXPR);
			node.addChild(codeNodeStack.pop());

			operator = ICodeFactory.createICodeNode(CTokenType.RELOP);
			operator.setAttribute(ICodeKey.TEXT, parser.getRelOperatorText());
			node.addChild(operator);
			node.addChild(codeNodeStack.pop());
			break;

		case CGrammarInitializer.NoCommaExpr_TO_Expr:
			node = ICodeFactory.createICodeNode(CTokenType.EXPR);
			node.addChild(codeNodeStack.pop());
			break;
		case CGrammarInitializer.Expr_Semi_TO_Statement:
			node = ICodeFactory.createICodeNode(CTokenType.STATEMENT);
			node.addChild(codeNodeStack.pop());
			node.setNo(++path_no);//com
			break;

		case CGrammarInitializer.Def_TO_Statement:
			node = ICodeFactory.createICodeNode(CTokenType.STATEMENT);
			break;

		case CGrammarInitializer.Statement_TO_StmtList:
			node = ICodeFactory.createICodeNode(CTokenType.STMT_LIST);
			if (codeNodeStack.size() > 0) {
				node.addChild(codeNodeStack.pop());
			}
			break;

		case CGrammarInitializer.StmtList_Statement_TO_StmtList:
			node = ICodeFactory.createICodeNode(CTokenType.STMT_LIST);
			node.addChild(codeNodeStack.pop());
			node.addChild(codeNodeStack.pop());
			break;

		case CGrammarInitializer.No_Comma_Expr_TO_Test:
			node = ICodeFactory.createICodeNode(CTokenType.TEST);
			node.addChild(codeNodeStack.pop());
			node.setNo(++path_no);//test
			break;

		case CGrammarInitializer.If_Test_Statement_TO_IFStatement:
			node = ICodeFactory.createICodeNode(CTokenType.IF_STATEMENT);
			node.addChild(codeNodeStack.pop()); 
			node.addChild(codeNodeStack.pop()); 
			break;

		case CGrammarInitializer.IfElseStatemnt_Else_Statemenet_TO_IfElseStatement:
			node = ICodeFactory.createICodeNode(CTokenType.IF_ELSE_STATEMENT);
			node.addChild(codeNodeStack.pop()); // IfStatement
			node.addChild(codeNodeStack.pop()); // statement
			break;
		
		case CGrammarInitializer.While_LP_Test_RP_TO_Statement:
    		node = ICodeFactory.createICodeNode(CTokenType.STATEMENT);
    		node.addChild(codeNodeStack.pop());
    		node.addChild(codeNodeStack.pop());
    		break;
		
		case CGrammarInitializer.VarDecl_LP_RP_TO_FunctDecl:
		case CGrammarInitializer.VarDecl_LP_VarList_RP_TO_FunctDecl:
    		node = ICodeFactory.createICodeNode(CTokenType.FUNCT_DECL);
    		node.addChild(codeNodeStack.pop());
    		child =  node.getChildren().get(0);
    		functionName = (String)child.getAttribute(ICodeKey.TEXT);
    		symbol = assignSymbolToNode(node, functionName);
    		break;
    	
		case CGrammarInitializer.TypeSpecifier_VarDecl_TO_ParamDecl:
		case CGrammarInitializer.VarDecl_TO_VarDeclList:
		case CGrammarInitializer.VarDeclList_COMMA_VarDecl_TO_VarDeclList:
    		//我们暂时不处理变量声明语句
    		codeNodeStack.pop();
    		break;
    		
    	case CGrammarInitializer.NAME_TO_VarDecl:
    		node = ICodeFactory.createICodeNode(CTokenType.VAR_DECL);
    		node.setAttribute(ICodeKey.TEXT, text);
    		break;
    		
    	case CGrammarInitializer.TypeSpecifier_FunctDecl_CompoundStmt_TO_ExtDef:
    		node = ICodeFactory.createICodeNode(CTokenType.EXT_DEF);
    		node.addChild(codeNodeStack.pop());
    		node.addChild(codeNodeStack.pop());
    		funcMap.put(functionName, node);
    		break;
    		
    	case CGrammarInitializer.Unary_LP_RP_TO_Unary:
    		node = ICodeFactory.createICodeNode(CTokenType.UNARY);
    		node.addChild(codeNodeStack.pop());
    		break;
    	
    	case CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary:
    		node = ICodeFactory.createICodeNode(CTokenType.UNARY);
    		node.addChild(codeNodeStack.pop());
    		node.addChild(codeNodeStack.pop());
    		break;
    		
    	case CGrammarInitializer.NoCommaExpr_TO_Args:
    		node = ICodeFactory.createICodeNode(CTokenType.ARGS);
    		node.addChild(codeNodeStack.pop());
    		break;
    		
    	case CGrammarInitializer.NoCommaExpr_Comma_Args_TO_Args:
    		node = ICodeFactory.createICodeNode(CTokenType.ARGS);
    		node.addChild(codeNodeStack.pop());
    		node.addChild(codeNodeStack.pop());
    		break;
    	
    	case CGrammarInitializer.Return_Expr_Semi_TO_Statement:
    		node = ICodeFactory.createICodeNode(CTokenType.STATEMENT);
    		node.addChild(codeNodeStack.pop());
    		node.setNo(++path_no);//return
    		break;
		}

		if (node != null) {
			node.setAttribute(ICodeKey.PRODUCTION, production);
			codeNodeStack.push(node);
		}
		return node;
	}

	private Symbol assignSymbolToNode(ICodeNode node, String text) {
    	Symbol symbol = typeSystem.getSymbolByText(text, parser.getCurrentLevel());
		node.setAttribute(ICodeKey.SYMBOL, symbol);
	    node.setAttribute(ICodeKey.TEXT, text);
	    
	    return symbol;
    }

	public ICodeNode getCodeTreeRoot() {
    	ICodeNode mainNode = funcMap.get("main");
    	return mainNode;
    }
}
