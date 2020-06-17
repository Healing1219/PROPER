package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import backend.Util.R;
import backend.Util.invExp;
import frontend.CGrammarInitializer;
import frontend.Symbol;

public class ExprExecutor extends BaseExecutor {
	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		executeChildren(root, path);
		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		Object value;
		ICodeNode child;
		if (Util.exeValid) {
			switch (production) {
			case CGrammarInitializer.NoCommaExpr_TO_Expr:
				copyChild(root, root.getChildren().get(0));
				break;

			case CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr:
				child = root.getChildren().get(0);
				IValueSetter setter;
				setter = (IValueSetter) child.getAttribute(ICodeKey.SYMBOL);
				child = root.getChildren().get(2);
				value = child.getAttribute(ICodeKey.VALUE);
				Symbol sym = (Symbol) child.getAttribute(ICodeKey.SYMBOL);
				Util.isAssign = 0;

				ArrayList<Double> arr = Util.varMap.get(setter);
				if (arr != null) {
					for (int i = 0; i < arr.size(); i++) {
						Util.setCoeff(i, -1 * arr.get(i));// 为什么-1？
					}
				}
//				if(!Util.varMap.containsKey(setter)) {
//					Util.vars.add(Util.vars.size()+1,(Symbol) setter);
//				}
				Util.varMap.put((Symbol) setter, Util.constrain);
				Util.constrain = new ArrayList<Double>();

				/*
				 * if (value != null) { boolean isFloat = value.toString().indexOf('.') != -1;
				 * if (isFloat || (sym != null && sym.getSpecifier() == 1)) { if (((Symbol)
				 * root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL)).getSpecifier() == 0)
				 * { System.err.println("Error: 实数不能赋值给整形变量！"); break; } } }
				 */

				try {
					setter.setValue(value);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Runtime Error: Assign Value Error");
				}

				child = root.getChildren().get(0);
				child.setAttribute(ICodeKey.VALUE, value);
				copyChild(root, root.getChildren().get(0));

				break;
			}
		} else {
			switch (production) {
			case CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr:
				Util.isAssign = 0;
			}
		}
		return root;
	}

	@Override
	public Object Execute(ICodeNode root, boolean isReadVar) {
		if (isReadVar) {
			executeChildren(root, isReadVar);
			if(Util.isWhile&&(int)root.getAttribute(ICodeKey.PRODUCTION)==CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr) {
				Util.loc++;
			}
		} else {
			if (Util.locType != 0 && root.getAttribute(ICodeKey.PRODUCTION)
					.equals(CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr)) {
				Util.locType = 2;
			}
			executeChildren(root, isReadVar);
			int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
			Object value;
			ICodeNode child;

			switch (production) {
			case CGrammarInitializer.NoCommaExpr_TO_Expr:
				copyChild(root, root.getChildren().get(0));
				break;

			case CGrammarInitializer.NoCommaExpr_Equal_NoCommaExpr_TO_Expr:
				child = root.getChildren().get(0);
				IValueSetter setter;
				setter = (IValueSetter) child.getAttribute(ICodeKey.SYMBOL);
				child = root.getChildren().get(2);
				value = child.getAttribute(ICodeKey.VALUE);
				Symbol sym = (Symbol) child.getAttribute(ICodeKey.SYMBOL);
				Util.isAssign = 0;

				try {
					setter.setValue(value);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Runtime Error: Assign Value Error");
				}

				if (Util.locType == 0) {
					Util.initV.put(((Symbol) setter).getName(), Double.valueOf(value.toString()));
				} else if (Util.locType == 2) {
					int n=Util.vars.get(((Symbol) setter).getName()).id;
					if (child.getAttribute(ICodeKey.PRODUCTION).equals(18)||(sym != null && sym.isR())) {	
						Util.putInvR((Symbol) setter, sym);
						Util.putInvariant(Util.previous, Util.loc, Util.loc + 1);
						Util.putPre1(n, sym, Double.parseDouble(value+""));
						Util.copyScope(0);
					}
					Util.previous = Util.loc;
					Util.loc++;
				}
				child = root.getChildren().get(0);
				child.setAttribute(ICodeKey.VALUE, value);
				copyChild(root, root.getChildren().get(0));

				break;
			}
		}
		return root;
	}
}
