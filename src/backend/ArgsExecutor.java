package backend;

import java.util.ArrayList;

import frontend.CGrammarInitializer;

public class ArgsExecutor extends BaseExecutor {

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);
		ArrayList<Object> argList = new ArrayList<Object>();
		ICodeNode child;

		switch (production) {
		case CGrammarInitializer.NoCommaExpr_TO_Args:
			child = (ICodeNode) executeChild(root, 0, path);
			Object objVal = child.getAttribute(ICodeKey.VALUE);
			argList.add(objVal);
			break;

		case CGrammarInitializer.NoCommaExpr_Comma_Args_TO_Args:
			child = executeChild(root, 0, path);
			objVal = child.getAttribute(ICodeKey.VALUE);
			argList.add(objVal);

			child = (ICodeNode) executeChild(root, 1, path);
			ArrayList<Object> list = (ArrayList<Object>) child.getAttribute(ICodeKey.VALUE);
			argList.addAll(list);
			break;
		}

		root.setAttribute(ICodeKey.VALUE, argList);
		return root;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);
		ArrayList<Object> argList = new ArrayList<Object>();
		ICodeNode child;

		switch (production) {
		case CGrammarInitializer.NoCommaExpr_TO_Args:
			child = (ICodeNode) executeChild(root, 0,isReadVar);
			Object objVal = child.getAttribute(ICodeKey.VALUE);
			argList.add(objVal);
			break;

		case CGrammarInitializer.NoCommaExpr_Comma_Args_TO_Args:
			child = executeChild(root, 0,isReadVar);
			objVal = child.getAttribute(ICodeKey.VALUE);
			argList.add(objVal);

			child = (ICodeNode) executeChild(root, 1,isReadVar);
			ArrayList<Object> list = (ArrayList<Object>) child.getAttribute(ICodeKey.VALUE);
			argList.addAll(list);
			break;
		}

		root.setAttribute(ICodeKey.VALUE, argList);
		return root;
	}

}
