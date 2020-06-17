package backend;

import java.util.ArrayList;

import frontend.CGrammarInitializer;
import frontend.Symbol;

public class FunctDeclExecutor extends BaseExecutor {
	private ArrayList<Object> argsList = null;

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);
		Symbol symbol = null;
		switch (production) {
		case CGrammarInitializer.VarDecl_LP_RP_TO_FunctDecl:
			root.reverseChildren();
			copyChild(root, root.getChildren().get(0));
			// Util.path.add(root.getNo());
			break;

		case CGrammarInitializer.VarDecl_LP_VarList_RP_TO_FunctDecl:
			// Util.path.add(root.getNo());
			symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
			// 获得参数列表
			Symbol args = symbol.getArgList();
			initArgumentList(args);

			if (args == null || argsList == null || argsList.isEmpty()) {
				// 如果参数为空，那就是解析错误
				System.err.println("Execute function with arg list but arg list is null");
				System.exit(1);
			}
			break;
		}
		return root;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);
		Symbol symbol = null;
		switch (production) {
		case CGrammarInitializer.VarDecl_LP_RP_TO_FunctDecl:
			root.reverseChildren();
			copyChild(root, root.getChildren().get(0));
			// Util.path.add(root.getNo());
			break;

		case CGrammarInitializer.VarDecl_LP_VarList_RP_TO_FunctDecl:
			// Util.path.add(root.getNo());
			symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
			// 获得参数列表
			Symbol args = symbol.getArgList();
			initArgumentList(args);

			if (args == null || argsList == null || argsList.isEmpty()) {
				// 如果参数为空，那就是解析错误
				System.err.println("Execute function with arg list but arg list is null");
				System.exit(1);
			}
			break;
		}
		return root;
	}

	private void initArgumentList(Symbol args) {
		if (args == null) {
			return;
		}

		argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(true);
		Symbol eachSym = args;
		int count = 0;
		while (eachSym != null) {
			IValueSetter setter = (IValueSetter) eachSym;
			try {
				/*
				 * 将每个输入参数设置为对应值并加入符号表
				 */
				setter.setValue(argsList.get(count));
				count++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			eachSym = eachSym.getNextSymbol();
		}
	}

//	public void removeArgsFromTable() {
//		Symbol symbol = (Symbol)currentNode.getAttribute(ICodeKey.SYMBOL);
//		Symbol arg = symbol.getArgList();
//		TypeSystem typeSystem = TypeSystem.getTypeSystem();
//		while (arg != null) {
//			typeSystem.removeSymbolFromTable(arg);
//			arg = arg.getNextSymbol();
//		}
//	}

}
