package backend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import backend.Util.R;
import backend.Util.invExp;
import frontend.CGrammarInitializer;
import frontend.Symbol;

public class NoCommaExprExecutor extends BaseExecutor {
	ExecutorFactory factory = ExecutorFactory.getExecutorFactory();

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		executeChildren(root, path);

		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		ICodeNode child;
		Symbol sym1, sym2;
		String s1, s2;
		int val1, val2;
		float val3, val4;
		BigDecimal b1,b2;
		ArrayList<Double> arr = new ArrayList<Double>();
		
		if (Util.exeValid) {
			switch (production) {
			case CGrammarInitializer.Binary_TO_NoCommaExpr:
				child = root.getChildren().get(0);
				copyChild(root, child);
				break;
				
			case CGrammarInitializer.NoCommaExpr_PLUS_BINARY_TO_NoCommaExpr:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				
				b1= new BigDecimal(s1);
				b2= new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b1.add(b2).doubleValue());
				/*if (sym1 == null && sym2 == null) {
					boolean isFloat = (s1.indexOf('.') != -1 || s2.indexOf('.') != -1);
					if (isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val3 + val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val1 + val2);
					}
				} else if (sym1 == null && sym2 != null) {
					boolean isFloat = s1.indexOf('.') != -1;
					if (sym2.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val3 + val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val1 + val2);
					}
				} else if (sym2 == null && sym1 != null) {
					boolean isFloat = s2.indexOf('.') != -1;
					if (sym1.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val3 + val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val1 + val2);
					}
				} else {
					if (sym1.getSpecifier() == 0 && sym2.getSpecifier() == 0) {
						val1 = Integer.parseInt(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val2 = Integer.parseInt(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString());
						root.setAttribute(ICodeKey.VALUE, val1 + val2);
					} else if (sym1.getSpecifier() == 1 || sym2.getSpecifier() == 1) {
						val3 = Float.valueOf(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val4 = Float.valueOf(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString());
						root.setAttribute(ICodeKey.VALUE, val3 + val4);
					} else {
						System.err.println("ERROR:不同类型无法相加！");
					}
				}*/
				//root.setAttribute(ICodeKey.SYMBOL, sym);
				if (Util.debug) {
					System.out.println("Assign sum of " + root.getChildren().get(0).getAttribute(ICodeKey.TEXT)
							+ " and " + root.getChildren().get(1).getAttribute(ICodeKey.TEXT) + " to variable "
							+ root.getAttribute(ICodeKey.VALUE));
				}
				break;

			case CGrammarInitializer.NoCommaExpr_MINUS_BINARY_TO_NoCommaExpr:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				
				b1= new BigDecimal(s1);
				b2= new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b1.subtract(b2).doubleValue());
				if (sym1 == null) {
					/*boolean isFloat = (s1.indexOf('.') != -1 || s2.indexOf('.') != -1);
					if (isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val4 - val3);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val2 - val1);
					}*/
					Util.setConstant(-2 * Double.parseDouble(s1));
				/*} else if (sym1 == null && sym2 != null) {
					boolean isFloat = s1.indexOf('.') != -1;
					if (sym2.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val4 - val3);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val2 - val1);
					}
					Util.setConstant(-2.0 * Double.parseDouble(s1));
				} else if (sym2 == null && sym1 != null) {
					boolean isFloat = s2.indexOf('.') != -1;
					if (sym1.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						root.setAttribute(ICodeKey.VALUE, val4 - val3);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						root.setAttribute(ICodeKey.VALUE, val2 - val1);
					}
					arr = Util.varMap.get(sym1);
					if (arr != null) {
						for (int i = 0; i < arr.size(); i++) {
							Util.setCoeff(i, -2 * arr.get(i));
						}
					}*/
				} else {
					/*if (sym1.getSpecifier() == 0 && sym2.getSpecifier() == 0) {
						val1 = Integer.parseInt(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val2 = Integer.parseInt(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString());
						root.setAttribute(ICodeKey.VALUE, val2 - val1);
					} else if (sym1.getSpecifier() == 1 && sym2.getSpecifier() == 1) {
						val3 = Float.valueOf(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val4 = Float.valueOf(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString());
						root.setAttribute(ICodeKey.VALUE, val4 - val3);
					} else {
						System.err.println("ERROR:不同类型无法相减！");
					}*/
					arr = Util.varMap.get(sym1);
					if (arr != null) {
						for (int i = 0; i < arr.size(); i++) {
							Util.setCoeff(i, -2 * arr.get(i));
						}
					}
				}
				//root.setAttribute(ICodeKey.SYMBOL, sym);
				if (Util.debug) {
					System.out.println("Assign difference of " + root.getChildren().get(0).getAttribute(ICodeKey.TEXT)
							+ " and " + root.getChildren().get(1).getAttribute(ICodeKey.TEXT) + " to variable "
							+ root.getAttribute(ICodeKey.VALUE));
				}
				break;

			case CGrammarInitializer.NoCommaExpr_RelOP_NoCommaExpr_TO_NoCommaExpr:
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(2).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(2).getAttribute(ICodeKey.VALUE).toString();
				String operator = (String) root.getChildren().get(1).getAttribute(ICodeKey.TEXT);

				Util.flag = 0;

				b1= new BigDecimal(s1);
				b2= new BigDecimal(s2);
				compareReal(root, operator, b1.floatValue(),b2.floatValue());
				/*if (sym1 == null && sym2 == null) {
					boolean isFloat = (s1.indexOf('.') != -1 || s2.indexOf('.') != -1);
					if (isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						compareReal(root, operator, val3, val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						compareInt(root, operator, val1, val2);
					}
				} else if (sym1 == null && sym2 != null) {
					boolean isFloat = s1.indexOf('.') != -1;
					if (sym2.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						compareReal(root, operator, val3, val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						compareInt(root, operator, val1, val2);
					}
				} else if (sym2 == null && sym1 != null) {
					boolean isFloat = s2.indexOf('.') != -1;
					if (sym1.getSpecifier() == 1 || isFloat) {
						val3 = Float.valueOf(s1);
						val4 = Float.valueOf(s2);
						compareReal(root, operator, val3, val4);
					} else {
						val1 = Integer.parseInt(s1);
						val2 = Integer.parseInt(s2);
						compareInt(root, operator, val1, val2);
					}
				} else {
					if (sym1.getSpecifier() == 0 && sym2.getSpecifier() == 0) {
						val1 = Integer.parseInt(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val2 = Integer.parseInt(root.getChildren().get(2).getAttribute(ICodeKey.VALUE).toString());
						compareInt(root, operator, val1, val2);
					} else if (sym1.getSpecifier() == 1 && sym2.getSpecifier() == 1) {
						val3 = Float.valueOf(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString());
						val4 = Float.valueOf(root.getChildren().get(2).getAttribute(ICodeKey.VALUE).toString());
						compareReal(root, operator, val3, val4);
					} else {
						System.err.println("ERROR:实数不能与整形相比较！");
					}
				}*/
				Util.constrain = new ArrayList<Double>();
				Util.constrain1 = new ArrayList<Double>();
				break;

			}
		}
		return root;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
  		executeChildren(root,isReadVar);

		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		ICodeNode child;
		Symbol sym1, sym2;
		String s1, s2;
		BigDecimal b1,b2;
		
		int n;
		ArrayList<ArrayList<Double>> tmp=new ArrayList<ArrayList<Double>>();
//		if(production!=CGrammarInitializer.Binary_TO_NoCommaExpr&&Util.previous!=0&&(Util.locType==2||Util.locType==1)) {	
//			Util.putInvariant();
//		}
		if(!isReadVar) {
		switch (production) {
			case CGrammarInitializer.Binary_TO_NoCommaExpr:
				child = root.getChildren().get(0);
				copyChild(root, child);
				break;
				
			case CGrammarInitializer.NoCommaExpr_PLUS_BINARY_TO_NoCommaExpr:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
			
				b1= new BigDecimal(s1);
				b2= new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b1.add(b2).doubleValue());
				
				if(Util.locType==2) {
					if(sym2!=null) {
						n=Util.vars.get(sym2.getName()).id;
						if(Util.preInv.containsKey(Util.previous)) {
							Util.copyScope(0);
							Util.putInv(n, sym1, b1.doubleValue(),1);
							Util.putInvariant(Util.previous,Util.loc,Util.loc+1);
							//if(Util.loop2>0) {
								Util.putMaxInv(n, sym1, b1.doubleValue(),1);
							//}
						}
						Util.putPre(n, sym1, b1.doubleValue(), 1);
					}
				}
				break;

			case CGrammarInitializer.NoCommaExpr_MINUS_BINARY_TO_NoCommaExpr:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				
				b1= new BigDecimal(s1);
				b2= new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b2.subtract(b1).doubleValue());
				if(Util.locType==1) {
					if (sym1 == null) {
						Util.addInv(0, -2*Double.parseDouble(s1));
					}else {
						Util.addInv(Util.vars.get(sym1.getName()).getId(), -2);
					}
				}else if(Util.locType==2) {
					n=Util.vars.get(sym2.getName()).id;
					if(Util.preInv.containsKey(Util.previous)) {
						Util.copyScope(0);
						Util.putInv(n, sym1, b1.doubleValue(),-1);
						Util.putInvariant(Util.previous,Util.loc,Util.loc+1);
						//if(Util.loop2>0) {
							Util.putMaxInv(n, sym1, b1.doubleValue(),-1);
						//}
					}
					Util.putPre(n, sym1, b1.doubleValue(), -1);
				}
				break;

			case CGrammarInitializer.NoCommaExpr_RelOP_NoCommaExpr_TO_NoCommaExpr:
				String operator = (String) root.getChildren().get(1).getAttribute(ICodeKey.TEXT);
				if(operator.equals("<=")||operator.equals("<")) {
					for(int i=0;i<Util.inv.size();i++) {
						Util.inv.set(i, Util.inv.get(i)*-1.0);
					}
				}
				/*else if(operator.equals("==")) {
					for(int i=0;i<Util.inv.size();i++) {
						Util.inv.set(i, Util.inv.get(i)*-1.0);
					}
				}*/
				Util.flag = 0;
				if(Util.previous!=0) {
					tmp=Util.handleOut(Util.previous);
					if(Util.isOut) {
						Util.isOut=false;
					}
				}
				//if(tmp.contains(Util.inv)) {
					tmp.remove(Util.inv);
				//}
				tmp.add(0,Util.inv);
				
				Util.comScope(Util.loc,Util.loc+1,Util.inv);
				if(operator.equals("<")) {
					Util.copyScope(-1);
				}else if(operator.equals(">")) {
					Util.copyScope(1);
				}else {
					Util.copyScope(0);
				}
				
				Util.preInv.put(Util.loc, tmp);
				Util.putInvariant(Util.loc,Util.loc,Util.loc+1);
				Util.previous=Util.loc;
				Util.loc++;
				Util.inv=new ArrayList<Double>();
				break;
			}
		}
		return root;
	}

	private void LE(ICodeNode root, ArrayList<Double> c, ArrayList<Double> c1) {
		for (int i = 0; i < c1.size(); i++) {
			Util.setCoeff(i, (-1) * c1.get(i));
		}
		Util.setConstant(-2 * c.get(0));

		if (root.getAttribute(ICodeKey.VALUE) == (Object) 0) {
			for (int i = 0; i < Util.constrain.size(); i++) {
				Util.setCoeff(i, (-2) * Util.constrain.get(i));
			}
		}

		Util.constrainList.add(Util.constrain);
	}

	private void LT_int(ICodeNode root, ArrayList<Double> c, ArrayList<Double> c1) {
		for (int i = 0; i < c1.size(); i++) {
			Util.setCoeff(i, (-1) * c1.get(i));
		}
		Util.setConstant(-2 * c.get(0) - 1);

		if (root.getAttribute(ICodeKey.VALUE) == (Object) 0) {
			for (int i = 0; i < Util.constrain.size(); i++) {
				Util.setCoeff(i, (-2) * Util.constrain.get(i));
			}
		}

		Util.constrainList.add(Util.constrain);
	}

	private void GE(ICodeNode root, ArrayList<Double> c, ArrayList<Double> c1) {
		for (int i = 0; i < c1.size(); i++) {
			Util.setCoeff(i, (-2) * c1.get(i));
		}
		for (int i = 0; i < c.size(); i++) {
			Util.setCoeff(i, c.get(i));
		}
		Util.setConstant((-2) * Util.constrain.get(0));

		if (root.getAttribute(ICodeKey.VALUE) == (Object) 0) {
			for (int i = 0; i < Util.constrain.size(); i++) {
				Util.setCoeff(i, (-2) * Util.constrain.get(i));
			}
		}

		Util.constrainList.add(Util.constrain);
	}

	private void GT_int(ICodeNode root, ArrayList<Double> c, ArrayList<Double> c1) {
		for (int i = 0; i < c1.size(); i++) {
			Util.setCoeff(i, (-2) * c1.get(i));
		}
		for (int i = 0; i < c.size(); i++) {
			Util.setCoeff(i, c.get(i));
		}
		Util.setConstant((-2) * Util.constrain.get(0) - 1);

		if (root.getAttribute(ICodeKey.VALUE) == (Object) 0) {
			for (int i = 0; i < Util.constrain.size(); i++) {
				Util.setCoeff(i, (-2) * Util.constrain.get(i));
			}
		}

		Util.constrainList.add(Util.constrain);
	}

	private void compareInt(ICodeNode root, String operator, int val1, int val2) {
		switch (operator) {
		case "==":
			root.setAttribute(ICodeKey.VALUE, val1 == val2 ? 1 : 0);
			break;
		case "<":
			root.setAttribute(ICodeKey.VALUE, val1 < val2 ? 1 : 0);
			LE(root, Util.constrain, Util.constrain1);
			break;
		case "<=":
			root.setAttribute(ICodeKey.VALUE, val1 <= val2 ? 1 : 0);
			LE(root, Util.constrain, Util.constrain1);
			break;
		case ">":
			root.setAttribute(ICodeKey.VALUE, val1 > val2 ? 1 : 0);
			GE(root, Util.constrain1, Util.constrain);
			break;
		case ">=":
			root.setAttribute(ICodeKey.VALUE, val1 >= val2 ? 1 : 0);
			GE(root, Util.constrain1, Util.constrain);
			break;
		case "!=":
			root.setAttribute(ICodeKey.VALUE, val1 != val2 ? 1 : 0);
			break;
		}
	}

	private void compareReal(ICodeNode root, String operator, float val1, float val2) {
		switch (operator) {
		case "==":
			root.setAttribute(ICodeKey.VALUE, val1 == val2 ? 1 : 0);
			break;
		case "<":
			root.setAttribute(ICodeKey.VALUE, val1 < val2 ? 1 : 0);
			LE(root, Util.constrain, Util.constrain1);
			break;
		case "<=":
			root.setAttribute(ICodeKey.VALUE, val1 <= val2 ? 1 : 0);
			LE(root, Util.constrain, Util.constrain1);
			break;
		case ">":
			root.setAttribute(ICodeKey.VALUE, val1 > val2 ? 1 : 0);
			GE(root, Util.constrain1, Util.constrain);
			break;
		case ">=":
			root.setAttribute(ICodeKey.VALUE, val1 >= val2 ? 1 : 0);
			GE(root, Util.constrain1, Util.constrain);
			break;
		case "!=":
			root.setAttribute(ICodeKey.VALUE, val1 != val2 ? 1 : 0);
			break;
		}
	}

}
