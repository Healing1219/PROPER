package backend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import frontend.CGrammarInitializer;
import frontend.Symbol;

public class BinaryExecutor extends BaseExecutor {
	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		executeChildren(root, path);
		ICodeNode child;
		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		Symbol sym1, sym2;
		String s1, s2;
		int val1, val2;
		// float val = 0, val3, val4;// val表示系数
		BigDecimal b1, b2;
		ArrayList<Double> arr = new ArrayList<Double>();
		ArrayList<Double> arr1 = new ArrayList<Double>();

		if (Util.exeValid) {
			switch (production) {
			case CGrammarInitializer.Unary_TO_Binary:
				child = root.getChildren().get(0);
				copyChild(root, child);
				break;

			case CGrammarInitializer.Binary_AND_Binary_TO_Binary:
			case CGrammarInitializer.Binary_OR_Binary_TO_Binary:
				Collections.reverse(root.getChildren());
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				if (s1 == "true") {
					val1 = 1;
				} else if (s1 == "false") {
					val1 = 0;
				} else {
					val1 = Integer.parseInt(s1);
				}
				if (s2 == "true") {
					val2 = 1;
				} else if (s2 == "false") {
					val2 = 0;
				} else {
					val2 = Integer.parseInt(s2);
				}
				if (production == CGrammarInitializer.Binary_OR_Binary_TO_Binary) {
					if (val1 == 0 && val2 == 0) {
						root.setAttribute(ICodeKey.VALUE, false);
					} else {
						root.setAttribute(ICodeKey.VALUE, true);
					}
				} else {
					if (val1 == 1 && val2 == 1) {
						root.setAttribute(ICodeKey.VALUE, true);
					} else {
						root.setAttribute(ICodeKey.VALUE, false);
					}
				}
				break;

			case CGrammarInitializer.Binary_Star_Unary_TO_Binary:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();

				b1 = new BigDecimal(s1);
				b2 = new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b1.multiply(b2).doubleValue());
				if (sym1 == null && sym2 == null) {// sym1与sym2均为实数
					/*
					 * boolean isFloat = (s1.indexOf('.') != -1 || s2.indexOf('.') != -1); if
					 * (isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2);
					 * root.setAttribute(ICodeKey.VALUE, val4 * val3); sym.setValue(val4 * val3);
					 * Util.setConstant((Float) root.getAttribute(ICodeKey.VALUE)); } else { val1 =
					 * Integer.parseInt(s1); val2 = Integer.parseInt(s2);
					 * root.setAttribute(ICodeKey.VALUE, val2 * val1); sym.setValue(val2 * val1);
					 * Util.setConstant((Integer) root.getAttribute(ICodeKey.VALUE)); } //
					 * 常量加上s1*s2的值并减去s1与s2的值
					 */
					Util.setConstant(-1 * Double.parseDouble(s1));
					Util.setConstant(-1 * Double.parseDouble(s2));
					Util.setConstant(b1.multiply(b2).doubleValue());
				} else if (sym1 == null && sym2 != null) {// sym1是实数，sym2是变量
					/*
					 * boolean isFloat = s1.indexOf('.') != -1; if (sym2.getSpecifier() == 1 ||
					 * isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2);
					 * root.setAttribute(ICodeKey.VALUE, val4 * val3); sym.setValue(val4 * val3);
					 * val = val3; } else { val1 = Integer.parseInt(s1); val2 =
					 * Integer.parseInt(s2); root.setAttribute(ICodeKey.VALUE, val2 * val1);
					 * sym.setValue(val2 * val1); val = (float) (val1 * 1.0); }
					 */
					arr = Util.varMap.get(sym2);
					if (arr != null) {
						for (int i = 0; i < arr.size(); i++) {
							Util.setCoeff(i, (b1.doubleValue() - 1) * arr.get(i));
						}
					} // 常量减去s1,并加上(系数-1)的变量sym2
					Util.setConstant(-1 * Double.parseDouble(s1));
				} else if (sym2 == null && sym1 != null) {// sym2是实数，sym1是变量
					/*
					 * boolean isFloat = s2.indexOf('.') != -1; if (sym1.getSpecifier() == 1 ||
					 * isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2);
					 * root.setAttribute(ICodeKey.VALUE, val4 * val3); sym.setValue(val4 * val3);
					 * val = val3; } else { val1 = Integer.parseInt(s1); val2 =
					 * Integer.parseInt(s2); root.setAttribute(ICodeKey.VALUE, val2 * val1);
					 * sym.setValue(val2 * val1); val = (float) (val1 * 1.0); }
					 */
					arr = Util.varMap.get(sym1);
					if (arr != null) {
						for (int i = 0; i < arr.size(); i++) {
							Util.setCoeff(i, (b1.doubleValue() - 1) * arr.get(i));
						}
					} // 常量减去s2,并加上(系数-1)的变量sym1
					Util.setConstant(-1 * Double.parseDouble(s2));
				} else {// 两个都是变量
					/*
					 * if (sym1.getSpecifier() == 0 && sym2.getSpecifier() == 0) { val1 =
					 * Integer.parseInt(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).
					 * toString()); val2 =
					 * Integer.parseInt(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).
					 * toString()); root.setAttribute(ICodeKey.VALUE, val2 * val1);
					 * sym.setValue(val2 * val1); } else if (sym1.getSpecifier() == 1 &&
					 * sym2.getSpecifier() == 1) { val3 =
					 * Float.valueOf(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString
					 * ()); val4 =
					 * Float.valueOf(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString
					 * ()); root.setAttribute(ICodeKey.VALUE, val4 * val3); sym.setValue(val4 *
					 * val3); } else { System.err.println("ERROR:不同类型无法相乘！"); }
					 */
					arr = Util.varMap.get(sym1);
					arr1 = Util.varMap.get(sym2);
					if (arr.size() > 1 && arr1.size() > 1) {
						System.err.println("暂不支持概率变量与概率变量相乘");
					} else if (arr.size() == 1 && arr1.size() > 1) {
						if (arr1 != null) {
							for (int i = 0; i < arr1.size(); i++) {
								Util.setCoeff(i, (arr.get(0) - 1) * arr1.get(i));
							}
						}
						Util.setConstant(-1 * arr.get(0));
					} else if (arr.size() > 1 && arr1.size() == 1) {
						if (arr != null) {
							for (int i = 0; i < arr.size(); i++) {
								Util.setCoeff(i, (arr1.get(0) - 1) * arr.get(i));
							}
						}
						Util.setConstant(-1 * arr1.get(0));
					} else if (arr.size() == 1 && arr1.size() == 1) {
						Util.setConstant(-1 * Double.parseDouble(s1));
						Util.setConstant(-1 * Double.parseDouble(s2));
						Util.setConstant((Double) root.getAttribute(ICodeKey.VALUE));
					}
				}
				// root.setAttribute(ICodeKey.SYMBOL, sym);

				if (Util.debug) {
					System.out.println("Assign product of " + root.getChildren().get(0).getAttribute(ICodeKey.TEXT)
							+ " and " + root.getChildren().get(1).getAttribute(ICodeKey.TEXT) + " to variable "
							+ root.getAttribute(ICodeKey.VALUE));
				}
				break;

			case CGrammarInitializer.Binary_DivOP_Unary_TO_Binary:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				b1 = new BigDecimal(s1);
				b2 = new BigDecimal(s2);
				if (b1.equals(new BigDecimal("0"))) {
					System.err.println("ERROR:除数不能为0！");
					assert (false);
				}
				root.setAttribute(ICodeKey.VALUE, b2.divide(b1, 10, BigDecimal.ROUND_HALF_UP).doubleValue());
				// sym = new Symbol();
				if (sym1 == null && sym2 == null) {
					/*
					 * boolean isFloat = (s1.indexOf('.') != -1 || s2.indexOf('.') != -1); if
					 * (isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2); if (val3 ==
					 * 0.0) { System.err.println("ERROR:除数不能为0！"); break;// assert(false) }
					 * root.setAttribute(ICodeKey.VALUE, val4 * 1.0 / val3); sym.setValue(val4 * 1.0
					 * / val3); } else { val1 = Integer.parseInt(s1); val2 = Integer.parseInt(s2);
					 * if (val1 == 0) { System.err.println("ERROR:除数不能为0！"); break;// assert(false)
					 * } if ((val2 * 1.0 / val1) - (val2 / val1) == 0) {
					 * root.setAttribute(ICodeKey.VALUE, val2 / val1); } else {
					 * root.setAttribute(ICodeKey.VALUE, val2 * 1.0 / val1); } }
					 */
					Util.setConstant(-1 * Double.parseDouble(s1));
					Util.setConstant(-1 * Double.parseDouble(s2));
					Util.setConstant((Double) root.getAttribute(ICodeKey.VALUE));
				} else if (sym1 == null && sym2 != null) {
					/*
					 * boolean isFloat = s1.indexOf('.') != -1; if (sym2.getSpecifier() == 1 ||
					 * isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2); if (val3 ==
					 * 0.0) { System.err.println("ERROR:除数不能为0！"); break; }
					 * root.setAttribute(ICodeKey.VALUE, val4 * 1.0 / val3); sym.setValue(val4 * 1.0
					 * / val3); val = (float) (1.0 / val3); } else { val1 = Integer.parseInt(s1);
					 * val2 = Integer.parseInt(s2); if (val1 == 0) {
					 * System.err.println("ERROR:除数不能为0！"); break; } if ((val2 * 1.0 / val1) - (val2
					 * / val1) == 0) { root.setAttribute(ICodeKey.VALUE, val2 / val1); } else {
					 * root.setAttribute(ICodeKey.VALUE, val2 * 1.0 / val1); } val = (float) (1.0 /
					 * val1); }
					 */
					arr = Util.varMap.get(sym2);
					if (arr != null) {
						for (int i = 0; i < arr.size(); i++) {
							Util.setCoeff(i, (1.0 / b1.doubleValue() - 1) * arr.get(i));
						}
					}
					Util.setConstant(-1 * Double.parseDouble(s1));
					Util.setConstant(-1 * Double.parseDouble(s2));
				} else if (sym2 == null && sym1 != null) {
					/*
					 * boolean isFloat = s2.indexOf('.') != -1; if (sym1.getSpecifier() == 1 ||
					 * isFloat) { val3 = Float.valueOf(s1); val4 = Float.valueOf(s2); if (val3 ==
					 * 0.0) { System.err.println("ERROR:除数不能为0！"); break; }
					 * root.setAttribute(ICodeKey.VALUE, val4 * 1.0 / val3); } else { val1 =
					 * Integer.parseInt(s1); val2 = Integer.parseInt(s2); if (val1 == 0) {
					 * System.err.println("ERROR:除数不能为0！"); break; } if ((val2 * 1.0 / val1) - (val2
					 * / val1) == 0) { root.setAttribute(ICodeKey.VALUE, val2 / val1); } else {
					 * root.setAttribute(ICodeKey.VALUE, val2 * 1.0 / val1); } }
					 */
					if (Util.varMap.get(sym1).size() > 1) {
						System.err.println("暂不支持概率变量为除数");
					} else {
						Util.setConstant(-1 * Double.parseDouble(s1));
						Util.setConstant(-1 * Double.parseDouble(s2));
						Util.setConstant((double) root.getAttribute(ICodeKey.VALUE));
					}
				} else {
					/*
					 * if (sym1.getSpecifier() == 0 && sym2.getSpecifier() == 0) { val1 =
					 * Integer.parseInt(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).
					 * toString()); val2 =
					 * Integer.parseInt(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).
					 * toString()); if (val1 == 0) { System.err.println("ERROR:除数不能为0！"); break; }
					 * if ((val2 * 1.0 / val1) - (val2 / val1) == 0) {
					 * root.setAttribute(ICodeKey.VALUE, val2 / val1); } else {
					 * root.setAttribute(ICodeKey.VALUE, val2 * 1.0 / val1); } } else if
					 * (sym1.getSpecifier() == 1 && sym2.getSpecifier() == 1) { val3 =
					 * Float.valueOf(root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString
					 * ()); val4 =
					 * Float.valueOf(root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString
					 * ()); if (val3 == 0.0) { System.err.println("ERROR:除数不能为0！"); break; }
					 * root.setAttribute(ICodeKey.VALUE, val4 * 1.0 / val3); } else {
					 * System.err.println("ERROR:不同类型无法相除！"); }
					 */
					arr = Util.varMap.get(sym1);
					arr1 = Util.varMap.get(sym2);
					if (arr.size() > 1 && arr1.size() > 1) {
						System.err.println("暂不支持概率变量与概率变量相除");
					} else if (arr.size() > 1) {
						System.err.println("暂不支持概率变量为除数");
					} else if (arr1.size() > 1) {
						Util.setConstant(-1 * Double.parseDouble(s1));
						Util.setConstant(-1 * Double.parseDouble(s2));
						for (int i = 0; i < arr1.size(); i++) {
							Util.setCoeff(i, (arr.get(0) - 1) * arr1.get(i));
						}
					} else {
						Util.setConstant(-1 * Double.parseDouble(s1));
						Util.setConstant(-1 * Double.parseDouble(s2));
						Util.setConstant((Double) root.getAttribute(ICodeKey.VALUE));
					}
				}

				// root.setAttribute(ICodeKey.SYMBOL, sym);
				if (Util.debug) {
					System.out.println("Assign divide of " + root.getChildren().get(0).getAttribute(ICodeKey.TEXT)
							+ " and " + root.getChildren().get(1).getAttribute(ICodeKey.TEXT) + " to variable "
							+ root.getAttribute(ICodeKey.VALUE));
				}
				break;
			}
		}
		return root;
	}

	@Override
	public Object Execute(ICodeNode root, boolean isReadVar) {
		executeChildren(root, isReadVar);
		ICodeNode child;
		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		Symbol sym1, sym2;
		String s1, s2;
		int val1, val2;
		BigDecimal b1, b2;
		
		if (!isReadVar) {
			switch (production) {
			case CGrammarInitializer.Unary_TO_Binary:
				child = root.getChildren().get(0);
				copyChild(root, child);
				break;

			case CGrammarInitializer.Binary_AND_Binary_TO_Binary:
			case CGrammarInitializer.Binary_OR_Binary_TO_Binary:
				Collections.reverse(root.getChildren());
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				if (s1 == "true") {
					val1 = 1;
				} else if (s1 == "false") {
					val1 = 0;
				} else {
					val1 = Integer.parseInt(s1);
				}
				if (s2 == "true") {
					val2 = 1;
				} else if (s2 == "false") {
					val2 = 0;
				} else {
					val2 = Integer.parseInt(s2);
				}
				if (production == CGrammarInitializer.Binary_OR_Binary_TO_Binary) {
					if (val1 == 0 && val2 == 0) {
						root.setAttribute(ICodeKey.VALUE, false);
					} else {
						root.setAttribute(ICodeKey.VALUE, true);
					}
				} else {
					if (val1 == 1 && val2 == 1) {
						root.setAttribute(ICodeKey.VALUE, true);
					} else {
						root.setAttribute(ICodeKey.VALUE, false);
					}
				}
				break;

			case CGrammarInitializer.Binary_Star_Unary_TO_Binary:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();

				b1 = new BigDecimal(s1);
				b2 = new BigDecimal(s2);
				root.setAttribute(ICodeKey.VALUE, b1.multiply(b2).doubleValue());
				if (sym1 == null && sym2 == null) {// sym1与sym2均为实数
					if (Util.locType == 1) {
						Util.addInv(0,
								-Double.parseDouble(s1) - Double.parseDouble(s2) + b1.multiply(b2).doubleValue());
					}
				} else if (sym1 == null && sym2 != null) {// sym1是实数，sym2是变量
					// 常量减去s1,并加上(系数-1)的变量sym2
					if (Util.locType == 1) {
						Util.addInv(0, -Double.parseDouble(s1));
						Util.addInv(Util.vars.get(sym2.getName()).getId(), b1.doubleValue() - 1);
					}
				} else if (sym2 == null && sym1 != null) {// sym2是实数，sym1是变量
					// 常量减去s2,并加上(系数-1)的变量sym1
					if (Util.locType == 1) {
						Util.addInv(0, -Double.parseDouble(s2));
						Util.addInv(Util.vars.get(sym1.getName()).getId(), b2.doubleValue() - 1);
					}
				} else {// 两个都是变量
					int n1 = Util.vars.get(sym1.getName()).getId();
					int n2 = Util.vars.get(sym2.getName()).getId();
					int n;
					if (n1 <= n2) {

					} else {

					}
				}
				break;

			case CGrammarInitializer.Binary_DivOP_Unary_TO_Binary:
				Collections.reverse(root.getChildren());
				sym1 = (Symbol) root.getChildren().get(0).getAttribute(ICodeKey.SYMBOL);
				sym2 = (Symbol) root.getChildren().get(1).getAttribute(ICodeKey.SYMBOL);
				s1 = root.getChildren().get(0).getAttribute(ICodeKey.VALUE).toString();
				s2 = root.getChildren().get(1).getAttribute(ICodeKey.VALUE).toString();
				b1 = new BigDecimal(s1);
				b2 = new BigDecimal(s2);
				if (b1.equals(new BigDecimal("0"))) {
					System.err.println("ERROR:除数不能为0！");
					assert (false);
				}
				root.setAttribute(ICodeKey.VALUE, b2.divide(b1, 10, BigDecimal.ROUND_HALF_UP).doubleValue());
				if (sym1 == null && sym2 == null) {
					if (Util.locType == 1) {
						Util.addInv(0, -Double.parseDouble(s1) - Double.parseDouble(s2)
								+ (Double) root.getAttribute(ICodeKey.VALUE));
					}
				} else if (sym1 == null && sym2 != null) {
					if (Util.locType == 1) {
						Util.addInv(0, -Double.parseDouble(s1));
						Util.addInv(Util.vars.get(sym2.getName()).getId(), 1 / b1.doubleValue() - 1);
					}
				} else if (sym2 == null && sym1 != null) {
					System.err.println("暂不支持变量为除数");
				} else {
					System.err.println("暂不支持变量为除数");
				}
			}
		}
		return root;
	}
}
