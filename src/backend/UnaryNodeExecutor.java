package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import backend.Util.nestInfo;
import frontend.CGrammarInitializer;
import frontend.Symbol;

public class UnaryNodeExecutor extends BaseExecutor {

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		String text;// 记录Number或Bool
		Symbol symbol;// 记录变量Name的Symbol
		ArrayList<Double> arr = new ArrayList<Double>();// 记录变量的向量值

		executeChildren(root, path);
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);

		if (Util.exeValid) {
			switch (production) {
			case CGrammarInitializer.Minus_Unary_TO_Unary:
			case CGrammarInitializer.Number_TO_Unary:
				text = (String) root.getAttribute(ICodeKey.TEXT);
				root.setAttribute(ICodeKey.VALUE, text);
				Util.setConstant(Double.parseDouble(text));
				break;

			case CGrammarInitializer.Name_TO_Unary:
				symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
				if (symbol != null) {
					Object v = symbol.getValue();
					root.setAttribute(ICodeKey.VALUE, v);
					root.setAttribute(ICodeKey.TEXT, symbol.getName());
					if (Util.isAssign == 1) {
						Util.wss.add(symbol);// 赋值等号右边
					} else {
						Util.lives.add(symbol);// 其他
					}

					if (v != null) {
						arr = Util.varMap.get(symbol);
						if (arr != null) {
							for (int i = 0; i < arr.size(); i++) {
								Util.setCoeff(i, arr.get(i));
							}
						} else {
							System.err.println(symbol.getName() + "Unassigned!");
						}
					}
				}
				break;

			case CGrammarInitializer.Bool_TO_Unary:
				text = (String) root.getAttribute(ICodeKey.TEXT);
				root.setAttribute(ICodeKey.VALUE, Boolean.parseBoolean(text));
				break;

			case CGrammarInitializer.Unary_Incop_TO_Unary:
			case CGrammarInitializer.Unary_Decop_TO_Unary:
				symbol = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				Double val = (Double) symbol.getValue();
				IValueSetter setter = (IValueSetter) symbol;
				try {
					if (production == CGrammarInitializer.Unary_Incop_TO_Unary) {
						setter.setValue(val + 1);
						arr = Util.varMap.get(symbol);
						arr.set(0, Util.varMap.get(symbol).get(0) + 1);
					} else {
						setter.setValue(val - 1);
						arr = Util.varMap.get(symbol);
						arr.set(0, Util.varMap.get(symbol).get(0) - 1);
					}
					Util.varMap.put(symbol, arr);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Runtime Error: Assign Value Error");
				}
				break;

			case CGrammarInitializer.Unary_LP_RP_TO_Unary:
			case CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary:
				// 先获得函数名
				String funcName = (String) root.getChildren().get(0).getAttribute(ICodeKey.TEXT);
				if (production == CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary) {
					ICodeNode argsNode = root.getChildren().get(1);
					ArrayList<Object> argList = (ArrayList<Object>) argsNode.getAttribute(ICodeKey.VALUE);
					FunctionArgumentList.getFunctionArgumentList().setFuncArgList(argList);

					for (int i = 0; i < argList.size(); i++) {
						Util.setConstant(-1 * Double.parseDouble(argList.get(i) + ""));
					}
				}
				// 找到函数执行树头节点
				ICodeNode func = CodeTreeBuilder.getCodeTreeBuilder().getFunctionNodeByName(funcName);

				if (func != null) {
					Executor executor = ExecutorFactory.getExecutorFactory().getExecutor(func);
					Util.lives.remove(func.getAttribute(ICodeKey.SYMBOL));
					executor.Execute(func, path);
					Object returnVal = func.getAttribute(ICodeKey.VALUE);
					if (returnVal != null) {
						System.out.println("function call with name " + funcName + " has return value that is "
								+ returnVal.toString());
						root.setAttribute(ICodeKey.VALUE, returnVal);
					}
				} else {
					ClibCall libCall = ClibCall.getInstance();
					if (libCall.isAPICall(funcName)) {
						// 移除live/ws中的函数名
						Iterator<Symbol> iterator;
						if (Util.isAssign == 1) {
							iterator = Util.wss.iterator();
						} else {
							iterator = Util.lives.iterator();
						}
						while (iterator.hasNext()) {
							if (iterator.next().getName() == funcName) {
								iterator.remove();
								break;
							}
						}
						// 调用函数
						Object obj = libCall.invokeAPI(funcName);
						if (Util.debug) {
							System.out.println(obj);
						}
						root.setAttribute(ICodeKey.VALUE, obj);
						// ClibCall.sym.setValue(obj);
						// ClibCall.sym.setProb(true);
						// root.setAttribute(ICodeKey.SYMBOL, ClibCall.sym);
					}
				}
				break;
			}
			return root;
		} else {
			switch (production) {
			case CGrammarInitializer.Name_TO_Unary:
				symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
				if (symbol != null) {
					if (Util.isAssign == 1) {
						Util.wss.add(symbol);// 赋值等号右边
					} else {
						Util.lives.add(symbol);// 其他
					}
				}
				break;
			case CGrammarInitializer.Unary_LP_RP_TO_Unary:
			case CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary:
				// 先获得函数名
				String funcName = (String) root.getChildren().get(0).getAttribute(ICodeKey.TEXT);
				// 找到函数执行树头节点
				ICodeNode func = CodeTreeBuilder.getCodeTreeBuilder().getFunctionNodeByName(funcName);

				if (func != null) {
					Util.lives.remove(func.getAttribute(ICodeKey.SYMBOL));
				} else {
					ClibCall libCall = ClibCall.getInstance();
					if (libCall.isAPICall(funcName)) {
						// 移除live/ws中的函数名
						Iterator<Symbol> iterator;
						if (Util.isAssign == 1) {
							iterator = Util.wss.iterator();
						} else {
							iterator = Util.lives.iterator();
						}
						while (iterator.hasNext()) {
							if (iterator.next().getName() == funcName) {
								iterator.remove();
								break;
							}
						}
					}
				}
				break;
			}
			return root;
		}
	}

	@Override
	public Object Execute(ICodeNode root, boolean isReadVar) {
		String text;// 记录Number或Bool
		Symbol symbol;// 记录变量Name的Symbol

		executeChildren(root, isReadVar);
		int production = (Integer) root.getAttribute(ICodeKey.PRODUCTION);
		if (isReadVar) {
			switch (production) {
			case CGrammarInitializer.Name_TO_Unary:
				symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
				if (symbol != null) {
					if (!Util.vars.containsKey(symbol.getName()))
						Util.vars.put(symbol.getName(), new Util.R(Util.vars.size() + 1));
				}
				break;
			case CGrammarInitializer.Unary_LP_RP_TO_Unary:
			case CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary:
				String funcName = (String) root.getChildren().get(0).getAttribute(ICodeKey.TEXT);
				// 将函数名从vars中移除
				Util.vars.remove(funcName);
			}
		} else {
			switch (production) {
			case CGrammarInitializer.Minus_Unary_TO_Unary:
			case CGrammarInitializer.Number_TO_Unary:
				text = (String) root.getAttribute(ICodeKey.TEXT);
				root.setAttribute(ICodeKey.VALUE, text);
				if (Util.locType == 1) {
					if (Util.flag == 0) {
						Util.addInv(0, Double.valueOf(text));
					} else {
						Util.addInv(0, -1 * Double.valueOf(text));
					}
				}
				break;

			case CGrammarInitializer.Name_TO_Unary:
				symbol = (Symbol) root.getAttribute(ICodeKey.SYMBOL);
				if (symbol != null) {
					if (isReadVar) {
						if (!Util.vars.containsKey(symbol.getName()))
							Util.vars.put(symbol.getName(), new Util.R(Util.vars.size() + 1));
					}

					Object v = symbol.getValue();
					root.setAttribute(ICodeKey.VALUE, v);
					root.setAttribute(ICodeKey.TEXT, symbol.getName());
					if (Util.locType == 1) {
						if (Util.flag == 0) {
							Util.addInv(Util.vars.get(symbol.getName()).id, 1.0);
						} else {
							Util.addInv(Util.vars.get(symbol.getName()).id, -1.0);
						}
					}
				}
				break;

			case CGrammarInitializer.Bool_TO_Unary:
				text = (String) root.getAttribute(ICodeKey.TEXT);
				root.setAttribute(ICodeKey.VALUE, Boolean.parseBoolean(text));
				break;

			case CGrammarInitializer.Unary_Incop_TO_Unary:
			case CGrammarInitializer.Unary_Decop_TO_Unary:
				symbol = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				Double val = (Double) symbol.getValue();
				IValueSetter setter = (IValueSetter) symbol;
				try {
					if (production == CGrammarInitializer.Unary_Incop_TO_Unary) {
						setter.setValue(val + 1);
					} else {
						setter.setValue(val - 1);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Runtime Error: Assign Value Error");
				}
				break;

			case CGrammarInitializer.Unary_LP_RP_TO_Unary:
			case CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary:
				// 先获得函数名
				String funcName = (String) root.getChildren().get(0).getAttribute(ICodeKey.TEXT);
				if (production == CGrammarInitializer.Unary_LP_ARGS_RP_TO_Unary) {
					ICodeNode argsNode = root.getChildren().get(1);
					ArrayList<Object> argList = (ArrayList<Object>) argsNode.getAttribute(ICodeKey.VALUE);
					FunctionArgumentList.getFunctionArgumentList().setFuncArgList(argList);

					for (int i = 0; i < argList.size(); i++) {
						Util.setConstant(-1 * Double.parseDouble(argList.get(i) + ""));
					}
				}
				// 找到函数执行树头节点
				ICodeNode func = CodeTreeBuilder.getCodeTreeBuilder().getFunctionNodeByName(funcName);

				if (func != null) {
					// 将函数名从vars中移除
					Util.vars.remove(funcName);
					Executor executor = ExecutorFactory.getExecutorFactory().getExecutor(func);
					Util.lives.remove(func.getAttribute(ICodeKey.SYMBOL));
					executor.Execute(func, isReadVar);
					Object returnVal = func.getAttribute(ICodeKey.VALUE);
					if (returnVal != null) {
						System.out.println("function call with name " + funcName + " has return value that is "
								+ returnVal.toString());
						root.setAttribute(ICodeKey.VALUE, returnVal);
					}
				} else {
					ClibCall libCall = ClibCall.getInstance();
					if (libCall.isAPICall(funcName)) {
						// 将函数名从vars中移除
						Util.vars.remove(funcName);

						// 调用函数
						Object obj = libCall.invokeAPI(funcName);
						if (Util.debug) {
							System.out.println(obj);
						}
						root.setAttribute(ICodeKey.VALUE, obj);
						// ClibCall.sym.setValue(obj);
						// ClibCall.sym.setProb(true);
						root.setAttribute(ICodeKey.SYMBOL, ClibCall.sym);
					}
				}
				break;
			}
		}
		return root;
	}
}
