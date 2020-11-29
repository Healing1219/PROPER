package frontend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import backend.Util;

public class PropManager {

	TypeSystem typeSystem = TypeSystem.getTypeSystem();
	ArrayList<Double> var = new ArrayList<Double>();
	ArrayList<Symbol> syml = new ArrayList<Symbol>();
	private Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

	public static int flag = 1;
	public static String operator = "+";
	// public static String prop_init = "c <= 8";

	public boolean checkStrIsNum(String str) {
		try {
			new BigDecimal(str).toString();
		} catch (Exception e) {
			return false;
		}

		Matcher isNum = NUMBER_PATTERN.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public boolean propLex(HashSet<Symbol> live_pro, String prop_init) {
		// Scanner s=new Scanner(System.in);
		// String prop_init=s.nextLine();
			String[] props = prop_init.split("\\(|\\)|&&");// 暂不支持或'||'
			// String[] props=prop_init.split("\\(|\\)|&&|\\|\\|");
			for (int i = 0; i < props.length; ++i) {
				if (props[i].isEmpty())
					continue;
				String[] prop = props[i].split(">=|<=|==|<|>");
				if (prop.length % 2 != 0) {
					return false;
				}
				for (int j = 0; j < prop.length; ++j) {
					String[] sym = prop[j].split("\\+|\\-|\\*|\\\\");
					for (int k = 0; k < sym.length; ++k) {
						sym[k] = sym[k].trim();
						// 判断是否是数字
						if (checkStrIsNum(sym[k]))
							continue;

						syml = typeSystem.getSymbol(sym[k]);
						if (syml != null) {
							live_pro.add(syml.get(0));
						} else {
							//System.err.println("输入性质不合法！");
							return false;
						} // if
					} // for
				} // for
			} // for
		return true;
	}

	public void calculate(String operator, String d1) {
		double d;
		d1 = d1.trim();
		if (Util.debug) {
			System.out.println(operator + " " + d1 + " " + flag);
		}
		if (checkStrIsNum(d1)) {
			if (flag == -1) {
				d = -1 * Double.parseDouble(d1);
			} else {
				d = Double.parseDouble(d1);
			}

			switch (operator) {
			case "+":
				Util.setConstant(d);
				break;
			case "-":
				Util.setConstant(-1 * d);
				break;
			case "*":
				for (int i = 0; i < var.size(); ++i) {
					Util.constrain.set(i, Util.constrain.get(i) * d);
				}
				break;
			case "\\":
				for (int i = 0; i < var.size(); ++i) {
					Util.constrain.set(i, Util.constrain.get(i) / d);
				}
				break;
			default:
				break;
			}
		} else {
			syml = typeSystem.getSymbol(d1);
			assert (syml != null);
			var = Util.varMap.get(syml.get(0));
			if (flag == -1) {
				for (int i = 0; i < var.size(); ++i) {
					var.set(i, var.get(i) * -1.0);
				}
			}

			switch (operator) {
			case "+":
				for (int i = 0; i < var.size(); ++i) {
					Util.setCoeff(i, var.get(i));
				}
				break;
			case "-":
				for (int i = 0; i < var.size(); ++i) {
					Util.setCoeff(i, -1.0 * var.get(i));
				}
				break;
			case "*":
			case "\\":
				System.out.println("暂不支持变量乘除！");
				break;
			default:
				break;
			}
		}
	}

	public boolean takeInEq(String s, String e) {
		if (s.contains(e)) {
			String[] tmp = s.split(e);
			assert (tmp.length <= 2);
			if (tmp.length == 1) {// 右边是负号
				calculate(operator, tmp[0]);
				operator = "-";
				flag = -1;
			} else {
				calculate(operator, tmp[0]);
				flag = -1;
				calculate(operator, tmp[1]);
			}
			return true;
		}
		return false;
	}

	public void propCons(String prop_init) {
		Util.constrainPropList = new ArrayList<ArrayList<Double>>();
		String[] props = prop_init.split("\\(|\\)|&&");
		for (int i = 0; i < props.length; ++i) {
			int count = 0;
			flag = 1;
			Util.constrain = new ArrayList<Double>();
			if (props[i].isEmpty())
				continue;
			if (props[i].contains("<=")) {
				String[] sym = props[i].split("\\+|\\-|\\*|\\\\");
				for (int j = 0; j < sym.length; ++j) {
					if (!takeInEq(sym[j], "<=")) {
						calculate(operator, sym[j]);
					}
					count += sym[j].length();
					if (j != sym.length - 1)
						operator = props[i].substring(count, ++count);
				}
				Util.setConstant(-2 * Util.constrain.get(0));
			} else if (props[i].contains(">=")) {
				String[] sym = props[i].split("\\+|\\-|\\*|\\\\");
				for (int j = 0; j < sym.length; ++j) {
					if (!takeInEq(sym[j], ">=")) {
						calculate(operator, sym[j]);
					}
					count += sym[j].length();
					if (j != sym.length - 1)
						operator = props[i].substring(count, ++count);
				}
				for (int k = 1; k < var.size(); ++k) {
					Util.constrain.set(k, Util.constrain.get(k) * -1);
				}
			} else if (props[i].contains("<")) {
				String[] sym = props[i].split("\\+|\\-|\\*|\\\\");
				for (int j = 0; j < sym.length; ++j) {
					if (!takeInEq(sym[j], "<")) {
						calculate(operator, sym[j]);
					}
					count += sym[j].length();
					if (j != sym.length - 1)
						operator = props[i].substring(count, ++count);
				}
				Util.setConstant(-2 * Util.constrain.get(0));
			} else if (props[i].contains(">")) {
				String[] sym = props[i].split("\\+|\\-|\\*|\\\\");
				for (int j = 0; j < sym.length; ++j) {
					if (!takeInEq(sym[j], ">")) {
						calculate(operator, sym[j]);
					}
					count += sym[j].length();
					if (j != sym.length - 1)
						operator = props[i].substring(count, ++count);
				}
				for (int k = 1; k < var.size(); ++k) {
					Util.constrain.set(k, Util.constrain.get(k) * -1);
				}
			} else {// boolean
				System.out.println("暂不支持Boolean型验证！");
			}
			Util.constrainPropList.add(Util.constrain);
		}
	}
}
