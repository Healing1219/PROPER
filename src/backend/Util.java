package backend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.lindo.Lingd13;

import java.util.Stack;

import backend.Util.R;
import backend.Util.invExp;
import backend.Util.varScope;
import frontend.PropManager;
import frontend.Symbol;
import inference.Box;
import inference.Ineq;
import inference.Polytope;
import inference.RVar;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import nonlinear.NlpSolver;

public class Util {

	// 配置
	public static int depth = 12;
	public static double EPS = 1e-05;
	public static int nTrials = 10000;
	public static int numBranches = 2;
	public static boolean useICP = true;
	public static boolean debug = false;
	public static boolean productDecompose = true;
	public static boolean redundancyEliminate = true;

	public static int nExec;// 总共执行数
	public static long timePath;// 记录寻找唯一路径的时间
	public static int flag = 0;// 判断用constrain还是constrain1
	public static int isAssign = 0;// 判断是赋值语句右边
	public static boolean exeValid = true;// 判断是否有效执行
	public static HashSet<Symbol> lives;
	public static HashSet<Symbol> wss;
	public static ArrayList<Var> varList;// 记录变量
	public static Map<Symbol, ArrayList<Double>> varMap;// 记录变量的向量
	public static ArrayList<Double> constrain = new ArrayList<Double>();// 约束
	public static ArrayList<Double> constrain1 = new ArrayList<Double>();// 约束test
	public static ArrayList<ArrayList<Double>> constrainList;// 记录约束
	public static ArrayList<ArrayList<Double>> constrainPropList;// 性质约束
	public static Map<Integer, pathInfo> pathInfoMap;// 记录每条路径的live集,var和constrain,从0开始
	public static int testNo = 0;// 记录if_else上面有多少个testPathNo需要添加ws集
	public static int loop = 0;// 记录目前的嵌套数
	public static ArrayList<Sym> liveTemp;// 记录if_else或while中的ws集
	public static ArrayList<Integer> testPath;// 记录约束路径序号
	public static ArrayList<Boolean> testState;// 记录约束路径状态
	public static ArrayList<nestInfo> nestCount;// 记录test嵌套中有几条语句，为找到执行if中语句前的live集,从1开始
	public static Map<ArrayList<Integer>, PathCom> pathMap = new HashMap<ArrayList<Integer>, PathCom>();// 不重复路径

	public static int e = 1;
	public static int k = -1 * e;
	public static int maxDegree = 2;// 目前只支持maxDegree=2;
	public static int maxMul = 2;
	public static int lastLoc;// 终止的位置
	// assiganment transitions 2
	// conditional transitions while/if 1
	// terminal location -1
	public static int locType = 0;// 未进入循环为0
	public static int loc = 0;
	public static int previous = 0;//
	public static boolean isOut;// 是否退出了一个if_else循环
	public static boolean isWhile = false;//
	public static int loop2 = 0;
	public static ArrayList<ArrayList<Integer>> ifNo = new ArrayList<ArrayList<Integer>>();
	public static Stack<Integer> whileNo = new Stack<Integer>();
	public static HashMap<String, Double> initV = new HashMap<String, Double>();// 初始值
	public static HashMap<String, R> vars = new HashMap<String, R>();// 变量
	public static ArrayList<Double> inv = new ArrayList<Double>();
	public static HashMap<Integer, R> maxInv = new HashMap<Integer, R>();
	public static HashMap<Integer, ArrayList<ArrayList<Double>>> preInv = new HashMap<Integer, ArrayList<ArrayList<Double>>>();
	public static HashMap<Integer, HashMap<Integer, ArrayList<ArrayList<Double>>>> invariant = new HashMap<Integer, HashMap<Integer, ArrayList<ArrayList<Double>>>>();
	public static HashMap<Integer, invExp> pre = new HashMap<Integer, invExp>();
	public static HashMap<Integer, HashMap<Integer, invExp>> g = new HashMap<Integer, HashMap<Integer, invExp>>();
	public static HashMap<Integer, HashMap<Integer, invExp>> invariantExp = new HashMap<Integer, HashMap<Integer, invExp>>();
	public static ArrayList<Inequ> inequs = new ArrayList<Util.Inequ>();
	public static int r = 0;
	public static double TerTime=0.01;
	public static HashMap<Integer, formWork> dpRSM = new HashMap<Integer, Util.formWork>();
	public static String basedir = System.getProperty("user.dir");;
	
	public static class nestInfo {
		boolean flag = false;
		ArrayList<Integer> count = new ArrayList<Integer>();

		public boolean isFlag() {
			return flag;
		}

		public void setFlag(boolean flag) {
			this.flag = flag;
		}

		public void addCount() {
			count.set(0, count.get(0) + 1);
		}

		public ArrayList<Integer> getCount() {
			return count;
		}
	}

	public static class pathInfo {
		ArrayList<Var> varList = new ArrayList<Util.Var>();
		ArrayList<ArrayList<Double>> constrainList = new ArrayList<ArrayList<Double>>();
		Sym sym = new Sym();

		public ArrayList<Var> getVarList() {
			return varList;
		}

		public ArrayList<ArrayList<Double>> getConstrainList() {
			return constrainList;
		}

		public Sym getSym() {
			return sym;
		}
	}

	public static class Sym {
		boolean isIf = false;
		HashSet<Symbol> live = new HashSet<Symbol>();
		HashSet<Symbol> ws = new HashSet<Symbol>();

		public void setIf(boolean isIf) {
			this.isIf = isIf;
		}

		public void setLive(HashSet<Symbol> live) {
			this.live = live;
		}

		public void setWs(HashSet<Symbol> ws) {
			this.ws = ws;
		}

		public boolean isIf() {
			return isIf;
		}

		public HashSet<Symbol> getLive() {
			return live;
		}

		public HashSet<Symbol> getWs() {
			return ws;
		}
	}

	public static class Var {
		String name, type;
		Object param1, param2;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getParam1() {
			return param1;
		}

		public void setParam1(Object param1) {
			this.param1 = param1;
		}

		public Object getParam2() {
			return param2;
		}

		public void setParam2(Object param2) {
			this.param2 = param2;
		}
	}

	public static class R {
		int id;
		int rId;
		boolean isR;
		double max;
		double min;
		double mean;
		double powMean;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
		}

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
		}

		public double getMean() {
			return mean;
		}

		public void setMean(double mean) {
			this.mean = mean;
		}

		public boolean isR() {
			return isR;
		}

		public void setR(boolean isR) {
			this.isR = isR;
		}

		public R(int id) {
			this.id = id;
		}

		public int getrId() {
			return rId;
		}

		public void setrId(int rId) {
			this.rId = rId;
		}

		public double getPowMean() {
			return powMean;
		}

		public void setPowMean(double powMean) {
			this.powMean = powMean;
		}

		public R(boolean isR, double min, double max, double mean, double powMean) {
			this.isR = isR;
			this.max = max;
			this.min = min;
			this.mean = mean;
			this.powMean = powMean;
		}
	}

	public static class invExp {
		int x, y;
		public double[][] arr;

		public invExp(int x, int y) {
			this.x = x;
			this.y = y;
			arr = new double[y][x];
		}
	}

	/*
	 * public static class varScope{ boolean isMax=false; boolean isMin=false;
	 * double max; double min; public boolean isMax() { return isMax; } public
	 * boolean isMin() { return isMin; } public double getMax() { return max; }
	 * public double getMin() { return min; } public varScope() {} public
	 * varScope(boolean isMax, boolean isMin, double max, double min) { super();
	 * this.isMax = isMax; this.isMin = isMin; this.max = max; this.min = min; } }
	 */

	public static class ifInfo {
		int id = 0;
		ArrayList<Integer> ifno = new ArrayList<Integer>();
		ArrayList<varScope> vars = new ArrayList<Util.varScope>();
	}

	public static class varScope {
		int id = 0;
		boolean isScattered = false;
		double max = Double.POSITIVE_INFINITY;
		double min = Double.NEGATIVE_INFINITY;
		String constraint = "";
		// ArrayList<Double> scatters=new ArrayList<Double>();
	}

	public static class formWork {
		String fun = "";
		public HashMap<Integer, String> varSub = new HashMap<Integer, String>();
		HashMap<Integer, ArrayList<varScope>> vars = new HashMap<Integer, ArrayList<varScope>>();
		// HashMap<Integer,ArrayList<String>> constraint=new HashMap<Integer,
		// ArrayList<String>>();
	}

	public static class formWork_ass {
		String fun = "";
		public HashMap<Integer, String> varSub = new HashMap<Integer, String>();
		HashMap<Integer, HashMap<Integer, ArrayList<varScope>>> vars = new HashMap<Integer, HashMap<Integer, ArrayList<varScope>>>();
		HashMap<Integer, String> constraint = new HashMap<Integer, String>();
	}

	public static class Inequ {
		int n;
		double[] coeffs;
		double c = 0;

		public Inequ(int n) {
			this.n = n;
			this.coeffs = new double[n];
		}

		public double getC() {
			return c;
		}

		public void setC(double c) {
			this.c = c;
		}

		public double[] getCoeffs() {
			return coeffs;
		}
	}

	public static void copySym(HashSet<Symbol> live, HashSet<Symbol> s) {
		Iterator<Symbol> iterator = live.iterator();
		while (iterator.hasNext()) {
			s.add(iterator.next());
		}
	}

	public static void ItpathInfoMap(int j) {
		Iterator<ArrayList<Double>> itC = pathInfoMap.get(j).getConstrainList().iterator();
		while (itC.hasNext()) {
			ArrayList<Double> c = itC.next();
			if (!constrainList.contains(c)) {
				constrainList.add(c);
			}
		}

		Iterator<Var> itV = Util.pathInfoMap.get(j).getVarList().iterator();
		while (itV.hasNext()) {
			Var var = itV.next();
			if (!varList.contains(var)) {
				varList.add(var);
			}
		}
	}

	public static class PathCom {
		ArrayList<Var> varList;
		ArrayList<ArrayList<Double>> constrainList;
		ArrayList<ArrayList<Double>> constrainProList;

		public ArrayList<Var> getVarList() {
			return varList;
		}

		public void setVarList(ArrayList<Var> varList) {
			this.varList = varList;
		}

		public ArrayList<ArrayList<Double>> getConstrainList() {
			return constrainList;
		}

		public void setConstrainList(ArrayList<ArrayList<Double>> constrainList) {
			this.constrainList = constrainList;
		}

		public ArrayList<ArrayList<Double>> getConstrainProList() {
			return constrainProList;
		}

		public void setConstrainProList(ArrayList<ArrayList<Double>> constrainProList) {
			this.constrainProList = constrainProList;
		}

	}

	// 设置常数C
	public static ArrayList<Double> setConstant(double d) {
		ArrayList<Double> arr = new ArrayList<Double>();

		if (flag == 0) {
			arr = constrain;
		} else if (flag == 1) {
			arr = constrain1;
		}
		if (arr.size() > 0) {
			arr.set(0, arr.get(0) + d);
		} else {
			arr.add(d);
		}
		return arr;
	}

	// 设置系数
	public static ArrayList<Double> setCoeff(int index, double d) {
		ArrayList<Double> arr = new ArrayList<Double>();

		if (flag == 0) {
			arr = constrain;
		} else if (flag == 1) {
			arr = constrain1;
		}
		while (arr.size() <= index) {
			arr.add(0.0);
		}
		arr.set(index, arr.get(index) + d);
		return arr;
	}

	// 补全
	public static void completedArr(ArrayList<ArrayList<Double>> constrainList) {
		ArrayList<Integer> indexVar = new ArrayList<Integer>();
		for (int i = 0; i < varList.size(); i++) {
			int index = Integer.parseInt(varList.get(i).getName().split("r_")[1]);
			indexVar.add(index);
		}
		Collections.sort(indexVar);
		Collections.reverse(indexVar);// 从大到小排序

		// 调整变量顺序
		Var temp = new Var();
		for (int i = 0; i < varList.size() - 1; i++) {
			for (int j = 0; j < varList.size() - 1 - i; j++) {
				int index1 = Integer.parseInt(varList.get(j).getName().split("r_")[1]);
				int index2 = Integer.parseInt(varList.get(j + 1).getName().split("r_")[1]);
				if (index1 > index2) {
					temp = varList.get(j);
					varList.set(j, varList.get(j + 1));
					varList.set(j + 1, temp);
				}
			}
		}

		for (int i = 0; i < constrainList.size(); i++) {
			// 结尾
			if (constrainList.get(i).size() - 1 > indexVar.get(0)) {
				for (int j = constrainList.get(i).size() - 1; j > indexVar.get(0); j--) {
					constrainList.get(i).remove(j);
				}
			}
			// 中间
			for (int j = 1; j < indexVar.size(); j++) {
				for (int k = indexVar.get(j - 1) - 1; k > indexVar.get(j); k--) {
					if (constrainList.get(i).size() - 1 >= k) {
						constrainList.get(i).remove(k);
					}
				}
			}
			// 开头
			for (int j = indexVar.get(indexVar.size() - 1) - 1; j > 0; j--) {
				if (constrainList.get(i).size() - 1 >= j) {
					constrainList.get(i).remove(j);
				}
			}
			// 补充0
			while (constrainList.get(i).size() <= varList.size()) {
				constrainList.get(i).add(0.0);
			}

		}
	}

	// 按path倒序遍历,找到path_slice
	public static void slicePath(ArrayList<Integer> path, ArrayList<Integer> path_slice) {
		ArrayList<HashSet<Symbol>> pathSym = new ArrayList<HashSet<Symbol>>();// 按path顺序记录每条语句的live集
		// 初始化pathSym
		for (int j = 0; j <= path.size(); j++) {
			pathSym.add(new HashSet<Symbol>());
		}
		Util.copySym(Util.lives, pathSym.get(0));

		// 按path倒序遍历,找到path_slice
		for (int j = path.size() - 1; j >= 0; j--) {
			Sym tmp = new Sym();
			tmp.setIf(pathInfoMap.get(path.get(j)).getSym().isIf());
			copySym(pathInfoMap.get(path.get(j)).getSym().getLive(), tmp.getLive());
			copySym(pathInfoMap.get(path.get(j)).getSym().getWs(), tmp.getWs());

			if (tmp.isIf()) {
				if (nestCount.size() > path.get(j)) {
					tmp.getWs()
							.retainAll(pathSym.get(path.size() - 1 - j - nestCount.get(path.get(j)).getCount().get(0)));
					nestCount.get(path.get(j)).getCount().remove(0);
				} else {
					tmp.getWs().retainAll(lives);
				}
				if (!tmp.getWs().isEmpty()) {
					path_slice.add(path.get(j));
					lives.addAll(tmp.getLive());
					ItpathInfoMap(path.get(j));
				}
				copySym(lives, pathSym.get(path.size() - j));
			} else {
				if (lives.removeAll(tmp.getLive())) {
					path_slice.add(path.get(j));
					lives.addAll(tmp.getWs());
					ItpathInfoMap(path.get(j));
				}
				copySym(lives, pathSym.get(path.size() - j));
			}
		}
		Collections.reverse(path_slice);
	}

	// 判断是否是唯一路径
	public static int uniquePath(int i, ArrayList<Integer> path_slice, String prop_init) {
		if (Util.pathMap.containsKey(path_slice)) {
			i++;
		} else {
			PathCom pathCom = new PathCom();
			pathCom.setVarList(Util.varList);
			pathCom.setConstrainList(Util.constrainList);
			PropManager pm = new PropManager();
			pm.propCons(prop_init);// 读取性质约束
			for (int j = 0; j < Util.constrainList.size(); ++j) {
				Util.constrainPropList.add(j, Util.constrainList.get(j));
			}
			pathCom.setConstrainProList(Util.constrainPropList);

			Util.pathMap.put(path_slice, pathCom);
			i = 0;
			Util.constrain = new ArrayList<Double>();
		}
		return i;
	}

	// 必须先读取变量再读取约束
	public static ArrayList<RVar> rVar;

	public static BigDecimal[] readData(int flag) throws IOException {
		Polytope p = new Polytope();
		FileWriter fileWriter = null;
		ArrayList<Var> varList = new ArrayList<Var>();
		ArrayList<ArrayList<Double>> constrainList = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> constrainProList = new ArrayList<ArrayList<Double>>();

		int i = 0;
		BigDecimal[] volComp = new BigDecimal[2];
		BigDecimal[] estP = new BigDecimal[1];
		estP[0] = new BigDecimal("0.0");// estimate
		volComp[0] = new BigDecimal("0.0");// UB
		volComp[1] = new BigDecimal("0.0");// LB
		//appendContentToFile("\n depth \t nVar \t timePath       nExec \t nUnique \t timeVol \t LB   \t      UB \n");

		for (Map.Entry<ArrayList<Integer>, PathCom> entry : pathMap.entrySet()) {
			i++;
			try {
				//fileWriter = new FileWriter("D:/Data/" + i + ".txt");// 创建文本文件
				varList = entry.getValue().getVarList();
				//appendContentToFile("  " + depth + "\t  " + varList.size() + "\t      " + timePath + "\t         "
				//		+ nExec + "  \t      " + pathMap.size() + "\t");
				rVar = new ArrayList<RVar>();
				readVariable(varList, fileWriter);
				// Collections.reverse(varList);
				Box bx = new Box(rVar);
				p.setPoly(bx);
				if (flag == 0) {
					constrainList = entry.getValue().getConstrainList();
					readConstraint(p, constrainList, fileWriter);
				} else {
					constrainProList = entry.getValue().getConstrainProList();
					readConstraint(p, constrainProList, fileWriter);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (null != fileWriter) {
					fileWriter.flush();
					fileWriter.close();
				}
			}
			boundComp(flag,p, volComp,estP);
			//readConstraint(p, constrainProList);
			//boundComp(p,volComp,1);
		}
		DecimalFormat df = new DecimalFormat("0.000000");
		if (flag == 0) {
			//appendContentToFile("\n Path Probability: [" + df.format(volComp[1]) + "," + df.format(volComp[0]) + "] \n");
		}else {
			//appendContentToFile("\n Estimate Probability: " + df.format(estP[0])+" \n");
		}
		return volComp;
	}

	// 读取变量
	public static void readVariable(ArrayList<Var> varList, FileWriter fileWriter) throws IOException {
		RVar r = new RVar();
		int vindex = 0;
		for (int i = 0; i < varList.size(); i++) {
			switch (varList.get(i).type) {
			case "U":
				rVar.add(r.uniformVariable(vindex++, varList.get(i).name, (double) varList.get(i).param1,
						(double) varList.get(i).param2));
				//fileWriter.write("U: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2
				//		+ "\r\n");
				break;
			case "I":
				rVar.add(r.uniformIntVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
//				fileWriter.write("I: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2
//						+ "\r\n");
				break;
			case "G":
				rVar.add(r.gaussianVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
				break;
			case "B":
				rVar.add(r.BinomialVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
				break;
			case "E":
				rVar.add(r.ExponentialVariable(vindex++, varList.get(i).name,
						Double.valueOf(varList.get(i).param1 + "")));
				break;
			case "Ga":
				rVar.add(r.GammaVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
				break;
			case "Be":
				rVar.add(r.BetaVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
				break;
			case "La":
				rVar.add(r.LaplaceVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + ""),
						Double.valueOf(varList.get(i).param2 + "")));
				break;
			case "Ge":
				rVar.add(
						r.GeometricVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + "")));
				break;
			case "P":
				rVar.add(r.PoissonVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + "")));
				break;
			case "T":
				rVar.add(r.TVariable(vindex++, varList.get(i).name, Double.valueOf(varList.get(i).param1 + "")));
				break;
			default:
				assert (false);
			}
		}
	}

	public static String readVariable(ArrayList<Var> varList, String s) {
		for (int i = 0; i < varList.size(); i++) {
			switch (varList.get(i).type) {
			case "U":
				s += "U: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "I":
				s += "I: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "G":
				s += "G: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "B":
				s += "B: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "E":
				s += "E: " + varList.get(i).name + " " + varList.get(i).param1 + "\r\n";
				break;
			case "Ga":
				s += "Ga: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "Be":
				s += "Be: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "Ge":
				s += "Ge: " + varList.get(i).name + " " + varList.get(i).param1 + " " + varList.get(i).param2 + "\r\n";
				break;
			case "La":
				s += "La: " + varList.get(i).name + " " + varList.get(i).param1 + "\r\n";
				break;
			case "P":
				s += "P: " + varList.get(i).name + " " + varList.get(i).param1 + "\r\n";
				break;
			case "T":
				s += "T: " + varList.get(i).name + " " + varList.get(i).param1 + "\r\n";
				break;
			default:
				assert (false);
			}
		}
		return s;
	}

	// 读取约束
	public static void readConstraint(Polytope p, ArrayList<ArrayList<Double>> constrainList, FileWriter fileWriter)
			throws IOException {
		for (int j = 0; j < constrainList.size(); j++) {
			// assert (rVar.size() == constrainList.get(j).size() - 1);
			Ineq e = new Ineq(rVar.size());
			e.c = constrainList.get(j).get(0);
//			fileWriter.write("C: " + e.c + " ");
			for (int i = 1; i < constrainList.get(j).size(); i++) {
				e.coeffs[i - 1] = constrainList.get(j).get(i);
//				fileWriter.write(e.coeffs[i - 1] + " ");
			}
			//fileWriter.write("\r\n");
			p.addInequality(e);
		}
	}
	
	public static String readConstraint(ArrayList<ArrayList<Double>> constrainList, String s) {
		for (int j = 0; j < constrainList.size(); j++) {
			s = s + "C: ";
			for (int i = 1; i < constrainList.get(j).size(); i++) {
				s = s + constrainList.get(j).get(i) + " ";
			}
			s = s + constrainList.get(j).get(0) + " " + "\r\n";
		}
		return s;
	}

	public static void boundComp(int flag,Polytope p, BigDecimal[] volComp,BigDecimal[] estP) {
		if (!p.isEmpty) {
			// System.out.println(" --- without bounding box reduction --- ");
			// p.mcEstimateProbability(false);
			// System.out.println(" --- with bounding box reduction -- ");
//			if(flag==1) {
//				p.mcEstimateProbability(true,estP);
//			}
			// System.out.println(" -- Bounding Box upper bound computation -- ");

			try {
				p.mcBoundProbability(Util.depth, Util.numBranches, volComp);
			} catch (LpSolveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String ArrayToString(double coeffs[]) {
		String str = "";
		for (double s : coeffs) {
			str += s + " ";
		}
		return str;
	}

	/*public static void appendContentToFile(String info) {
		File file = new File("D:/Data/result.txt");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(info);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public static void addInv(int n, double d) {
		for (int i = inv.size(); i <= n; i++) {
			inv.add(0.0);
		}
		inv.set(n, inv.get(n) + d);
	}

	public static void putMaxInv(int n, Symbol sym, double b, int sign) {
		Util.R r = null;
		if (sym != null && sym.isR()) {
			if (sign == 1) {
				r = new R(true, sym.r.min, sym.r.max, sym.r.mean, sym.r.powMean);
			} else if (sign == -1) {
				r = new R(true, -1 * sym.r.max, -1 * sym.r.min, sym.r.mean, sym.r.powMean);
			}
			r.rId = sym.getrId();
		} else if (sym != null && Util.vars.get(sym.getName()).isR()) {
			if (sign == 1) {
				r = new R(true, Util.vars.get(sym.getName()).getMin(), Util.vars.get(sym.getName()).getMax(),
						Util.vars.get(sym.getName()).getMean(), Util.vars.get(sym.getName()).getPowMean());
			} else if (sign == -1) {
				r = new R(true, -1 * Util.vars.get(sym.getName()).getMax(), -1 * Util.vars.get(sym.getName()).getMin(),
						Util.vars.get(sym.getName()).getMean(), Util.vars.get(sym.getName()).getPowMean());
			}
			r.rId = sym.getrId();
		} else {
			r = new R(false, sign * b, sign * b, sign * b, Math.pow(b, 2));
		}
		r.id = n;
		Util.maxInv.put(Util.loc, r);
	}

	public static ArrayList<ArrayList<Double>> handleOut(int previous) {
		int n;
		Util.R r;
		ArrayList<Double> arr;
		HashMap<Integer, R> map = new HashMap<Integer, R>();
		ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();

		for (int i = 0; i < Util.preInv.get(previous).size(); i++) {
			arr = new ArrayList<Double>();
			for (int j = 0; j < Util.preInv.get(previous).get(i).size(); j++) {
				arr.add(Util.preInv.get(previous).get(i).get(j));
			}
			tmp.add(arr);
		}

		if (!Util.isWhile && Util.isOut && Util.previous + 1 < Util.loc) {
			Iterator<Entry<Integer, R>> it = Util.maxInv.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, R> entry = it.next();
				if (Util.previous < entry.getKey()) {
					n = entry.getValue().getId();
					if (map.containsKey(n)) {
						if (entry.getValue().getMax() > map.get(n).getMax()) {
							map.get(n).setMax(entry.getValue().getMax());
						}
						if (entry.getValue().getMin() < map.get(n).getMin()) {
							map.get(n).setMin(entry.getValue().getMin());
						}
					} else {
						r = new R(true, entry.getValue().getMin(), entry.getValue().getMax(),
								entry.getValue().getMean(), entry.getValue().getPowMean());
						map.put(n, r);
					}
				}
			}

			Iterator<Map.Entry<Integer, R>> it1 = map.entrySet().iterator();
			while (it1.hasNext()) {
				Map.Entry<Integer, R> entry1 = it1.next();
				for (int i = 0; i < tmp.size(); i++) {
					if (entry1.getKey() >= tmp.get(i).size())
						continue;
					if (tmp.get(i).get(entry1.getKey()) > 0) {
						// if(entry1.getValue().getMin()<0)
						tmp.get(i).set(0, tmp.get(i).get(0) - entry1.getValue().getMin());
					} else if (tmp.get(i).get((int) entry1.getKey()) < 0) {
						// if(entry1.getValue().getMax()>0)
						tmp.get(i).set(0, tmp.get(i).get(0) + entry1.getValue().getMax());
					}
				}
			}
		}
		return tmp;
	}

	public static void putInv(int n, Symbol sym, double b, int sign) {// sign: + 1,- -1
		double m;
		ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();

		tmp = Util.handleOut(Util.previous);

		if (Util.isWhile) {
			for (int i = 0; i < tmp.get(0).size(); i++) {
				tmp.get(0).set(i, tmp.get(0).get(i) * -1);
			}
		}

		for (int i = 0; i < tmp.size(); i++) {
			if (tmp.get(i).size() <= n) {
				m = 0;
			} else {
				m = tmp.get(i).get(n);
			}
			if (sym != null && sym.isR()) {
				if (m > 0) {
					if (sign == 1)
						tmp.get(i).set(0, tmp.get(i).get(0) - sym.r.getMin());
					if (sign == -1)
						tmp.get(i).set(0, tmp.get(i).get(0) + sym.r.getMax());
				} else if (m < 0) {
					if (sign == 1)
						tmp.get(i).set(0, tmp.get(i).get(0) + sym.r.getMax());
					if (sign == -1)
						tmp.get(i).set(0, tmp.get(i).get(0) - sym.r.getMin());
				}
			} else if (sym != null && Util.vars.get(sym.getName()).isR()) {
				if (m > 0) {
					if (sign == 1)
						tmp.get(i).set(0, tmp.get(i).get(0) - Util.vars.get(sym.getName()).getMin());
					if (sign == -1)
						tmp.get(i).set(0, tmp.get(i).get(0) + Util.vars.get(sym.getName()).getMax());
				} else if (m < 0) {
					if (sign == 1)
						tmp.get(i).set(0, tmp.get(i).get(0) + Util.vars.get(sym.getName()).getMax());
					if (sign == -1)
						tmp.get(i).set(0, tmp.get(i).get(0) - Util.vars.get(sym.getName()).getMin());
				}
			} else {
				if (m != 0) {
					if (sign == 1)
						tmp.get(i).set(0, tmp.get(i).get(0) - b);
					if (sign == -1)
						tmp.get(i).set(0, tmp.get(i).get(0) + b);
				}
			}
		}
		Util.preInv.put(Util.loc, tmp);
	}

	public static void putInvR(Symbol setter, Symbol sym) {
		ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();

		tmp = Util.handleOut(Util.previous);

		if (Util.isWhile) {
			for (int i = 0; i < tmp.get(0).size(); i++) {
				tmp.get(0).set(i, tmp.get(0).get(i) * -1);
			}
		}

		if (sym != null) {
			if (sym.isR()) {
				Util.vars.get(setter.getName()).setR(true);
				Util.vars.get(setter.getName()).setMax(sym.r.max);
				Util.vars.get(setter.getName()).setMin(sym.r.min);
				Util.vars.get(setter.getName()).setMean(sym.r.mean);
				int n = Util.vars.get(setter.getName()).getId();
				Util.addInv(0, sym.r.max);
				Util.addInv(n, -1);
				tmp.add(Util.inv);
				Util.inv = new ArrayList<Double>();
				Util.addInv(0, -1 * sym.r.min);
				Util.addInv(n, 1);
				tmp.add(Util.inv);
				// 修改dpRSM中vars的id
				for (int i = 0; i < Util.dpRSM.get(Util.loc).vars.get(Util.loc + 1).size(); i++) {
					Util.dpRSM.get(Util.loc).vars.get(Util.loc + 1).get(i).id = n;
				}
				Util.r--;
				Util.inv = new ArrayList<Double>();
			} else {
				int n = Util.vars.get(setter.getName()).getId();
				int m = Util.vars.get(sym.getName()).getId();
				Util.addInv(m, 1);
				Util.addInv(n, -1);
				tmp.add(Util.inv);
				Util.inv = new ArrayList<Double>();
				Util.addInv(m, -1);
				Util.addInv(n, 1);
				tmp.add(Util.inv);
				Util.inv = new ArrayList<Double>();
			}
		} else {
			int n = Util.vars.get(setter.getName()).getId();
//			Util.addInv(0, -1 * Double.parseDouble(setter.getValue() + ""));
//			Util.addInv(n, 1);
//			tmp.add(Util.inv);
//			Util.inv = new ArrayList<Double>();
//			Util.addInv(0, Double.parseDouble(setter.getValue() + ""));
//			Util.addInv(n, -1);
//			tmp.add(Util.inv);
			Util.inv = new ArrayList<Double>();
//			求[a,b]
			varScope v = new varScope();
			ArrayList<varScope> var = new ArrayList<Util.varScope>();
			formWork f = new formWork();
			v.id = n;
			v.max = Double.parseDouble(setter.getValue() + "");
			v.min = Double.parseDouble(setter.getValue() + "");
			var.add(v);
			f.vars.put(Util.loc + 1, var);
			Util.dpRSM.put(Util.loc, f);
		}
		Util.preInv.put(Util.loc, tmp);
	}

	public static void putOutWhile(int n, int loc1, int loc2) {
		ArrayList<Double> arr = new ArrayList<Double>();
		ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> tmp1 = new ArrayList<ArrayList<Double>>();

		tmp = Util.handleOut(n);

		// 将修改好加入previous+1
		for (int i = 0; i < tmp.size(); i++) {
			if (!Util.invariant.get(previous).get(previous + 1).contains(tmp.get(i))) {
				Util.invariant.get(previous).get(previous + 1).add(tmp.get(i));
			}
		}
		// 将不符合if/while中的test加入loc
		for (int i = 0; i < Util.invariant.get(previous).get(previous + 1).size(); i++) {
			int m = 1;
			if (i == 0)
				m = -1;
			arr = new ArrayList<Double>();
			for (int j = 0; j < Util.invariant.get(previous).get(previous + 1).get(i).size(); j++) {
				arr.add(Util.invariant.get(previous).get(previous + 1).get(i).get(j) * m);
			}
			Util.comScope(previous, Util.loc, arr);
			tmp1.add(arr);
		}
		Util.invariant.get(previous).put(Util.loc, tmp1);
	}

	public static void putInvariant(int n, int loc1, int loc2) {
		ArrayList<Double> arr = new ArrayList<Double>();
		ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();
		HashMap<Integer, ArrayList<ArrayList<Double>>> haa = new HashMap<Integer, ArrayList<ArrayList<Double>>>();

		tmp = Util.handleOut(n);

		if (Util.isWhile) {
			Util.isWhile = false;
			for (int i = 0; i < tmp.get(0).size(); i++) {
				tmp.get(0).set(i, tmp.get(0).get(i) * -1);
			}
		}

		// if(Util.isOut) {
		Util.isOut = false;
		// }

		if (Util.invariant.containsKey(loc1)) {
			if (Util.invariant.get(loc1).containsKey(loc2)) {
				for (int i = 0; i < tmp.size(); i++) {
					Util.invariant.get(loc1).get(loc2).add(tmp.get(i));
				}
			} else {
				Util.invariant.get(loc1).put(loc2, tmp);
			}
		} else {
			haa.put(loc2, tmp);
			Util.invariant.put(loc1, haa);
		}
	}

	public static void putPre(int n, Symbol sym, double b, int sign) {
		int i, j, x;
		double mean, powMean;
		x = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		Util.invExp invExp = new Util.invExp(x, x);
		// invExp.arr[0][0]=1.0;
		for (i = 0; i < invExp.x; i++) {
			invExp.arr[i][i] = 1.0;
		}

		if (sym != null && sym.isR()) {
			mean = sym.r.getMean();
			powMean = sym.r.getPowMean();
		} else if (sym != null && Util.vars.get(sym.getName()).isR()) {
			mean = Util.vars.get(sym.getName()).getMean();
			powMean = Util.vars.get(sym.getName()).getPowMean();
		} else {
			mean = b;
			powMean = Math.pow(b, 2);
		}

		invExp.arr[0][n] += mean * sign;
		x = Util.vars.size() + 1;
		for (i = 1; i < n; i++) {
			x += Util.vars.size() - i + 1;
		}
		invExp.arr[0][x] += powMean;
		invExp.arr[n][x] += 2 * mean * sign;

		for (i = 1; i < n; i++) {
			int y = Util.vars.size();
			for (j = 1; j < i; j++)
				y += Util.vars.size() - j + 1;
			y += n - j + 1;
			invExp.arr[i][y] += mean * sign;
		}
		for (i = 1; i <= Util.vars.size() - n; i++) {
			invExp.arr[n + i][x + i] += mean * sign;
		}
		Util.pre.put(Util.loc, invExp);
	}

	public static void putPre1(int n, Symbol sym, double val) {
		int i, j, x, y;
		double mean, powMean;
		x = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		Util.invExp invExp = new Util.invExp(x, x);
		for (i = 0; i < invExp.x; i++) {
			if (i != n)
				invExp.arr[i][i] = 1.0;
		}
		if(sym!=null&&!sym.isR()) {
			int m = Util.vars.get(sym.getName()).getId();
			for (i = 1; i < n; i++) {
				y = Util.vars.size();
				x = Util.vars.size();
				for (j = 1; j < i; j++)
					y += Util.vars.size() - j + 1;
				y += n - j + 1;
				for (j = 1; j < m; j++)
					x += Util.vars.size() - j + 1;
				x+=i;
				invExp.arr[x][y] += 1;
				invExp.arr[y][y] = 0;
			}
			invExp.arr[m][n] += 1;
			x = Util.vars.size() + 1;
			y = Util.vars.size() + 1;
			for (i = 1; i < n; i++) {
				x += Util.vars.size() - i + 1;
			}
			for (i = 1; i < m; i++) {
				y += Util.vars.size() - i + 1;
			}
			invExp.arr[x][x] = 0;
			invExp.arr[y][x] += 1;
			for (i = 1; i <= Util.vars.size() - n; i++) {
				invExp.arr[x + i][x + i] = 0;
				if(n>m) {
					invExp.arr[y+n-1+ i][x + i] +=1;
				}else {
					if(n+i<m) {
						int y1 = Util.vars.size();
						for (j = 1; j < n+i; j++)
							y1 += Util.vars.size() - j + 1;
						invExp.arr[y1+i+1][x + i] +=1;
					}else {
						invExp.arr[y+i-(m-n)][x + i] +=1;
					}	
				}
			}
		}else {
			if(sym==null) {
				mean = val;
				powMean = val * val;
			}else {
				mean = sym.r.getMean();
				powMean = sym.r.getPowMean();
			}
			for (i = 1; i < n; i++) {
				y = Util.vars.size();
				for (j = 1; j < i; j++)
					y += Util.vars.size() - j + 1;
				y += n - j + 1;
				invExp.arr[i][y] += mean;
				invExp.arr[y][y] = 0;
			}

			invExp.arr[0][n] = mean;
			x = Util.vars.size() + 1;
			for (i = 1; i < n; i++) {
				x += Util.vars.size() - i + 1;
			}
			invExp.arr[x][x] = 0;
			invExp.arr[0][x] = powMean;
			for (i = 1; i <= Util.vars.size() - n; i++) {
				invExp.arr[x + i][x + i] = 0;
				invExp.arr[n + i][x + i] += mean;
			}
		}
		Util.pre.put(Util.loc, invExp);
	}

	public static int expressInv() {
		int x, y, count = 0;
		invExp invExp;
		HashMap<Integer, invExp> haa;
		// invExpand invE;
		Iterator<Map.Entry<Integer, HashMap<Integer, ArrayList<ArrayList<Double>>>>> it = Util.invariant.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<Integer, HashMap<Integer, ArrayList<ArrayList<Double>>>> entry = it.next();
			Iterator<Map.Entry<Integer, ArrayList<ArrayList<Double>>>> it1 = entry.getValue().entrySet().iterator();
			haa = new HashMap<Integer, invExp>();
			// invE=new invExpand();
			// HashMap<Integer,HashMap<Integer,varScope>> vars=new HashMap<Integer,
			// HashMap<Integer,varScope>>();
			while (it1.hasNext()) {
				Entry<Integer, ArrayList<ArrayList<Double>>> entry1 = it1.next();
				x = entry1.getValue().size() + ((entry1.getValue().size() + 1) * entry1.getValue().size()) / 2 + 1;
				y = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
				count += x;
				// HashMap<Integer,varScope> var=new HashMap<Integer, Util.varScope>();
				invExp = new invExp(x, y);
				invExp.arr[0][0] = 1;
				for (int i = 0; i < entry1.getValue().size(); i++) {
					// comScope(entry1.getKey(),entry1.getValue().get(i),vars);
					for (int ai = 0; ai < entry1.getValue().get(i).size(); ai++) {
						invExp.arr[ai][i + 1] += entry1.getValue().get(i).get(ai);

						for (int j = i; j < entry1.getValue().size(); j++) {
							for (int bi = 0; bi < entry1.getValue().get(j).size(); bi++) {
								y = entry1.getValue().size() + j - i + 1;
								for (int i2 = 0; i2 < i; i2++) {
									y += entry1.getValue().size() - i2;
								}
								if (ai == 0) {
									if (bi == 0) {
										invExp.arr[0][y] += entry1.getValue().get(i).get(ai)
												* entry1.getValue().get(j).get(bi);
									} else {
										invExp.arr[bi][y] += entry1.getValue().get(i).get(ai)
												* entry1.getValue().get(j).get(bi);
									}
								} else if (bi == 0) {
									invExp.arr[ai][y] += entry1.getValue().get(i).get(ai)
											* entry1.getValue().get(j).get(bi);
								} else {
									x = Util.vars.size() + Math.max(ai, bi) - Math.min(ai, bi) + 1;
									for (int i1 = 1; i1 < Math.min(ai, bi); i1++) {
										x += Util.vars.size() - i1 + 1;
									}
									invExp.arr[x][y] += entry1.getValue().get(i).get(ai)
											* entry1.getValue().get(j).get(bi);
								}
							}
						}
					}
				}
				haa.put(entry1.getKey(), invExp);
				// invE.vars=vars;
			}
			Util.invariantExp.put(entry.getKey(), haa);
			// Util.dpRSM.put(entry.getKey(), invE);
		}
		return count;
	}

	public static void comInequs(int count) {
		int x = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		int num = 0;

		for (int i = 1; i < Util.loc; i++) {
			HashMap<Integer, invExp> map = Util.invariantExp.get(i);
			Iterator<Map.Entry<Integer, Util.invExp>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, Util.invExp> entry = it.next();
				int n = entry.getKey();
				for (int j = 0; j < x; j++) {
					Util.Inequ inequ = new Util.Inequ((Util.loc - 1) * x + count);
					if (j == 0)
						inequ.c += 1;
					if (n == Util.loc && j == 0)
						inequ.c -= 1;
					inequ.getCoeffs()[(i - 1) * x + j] += 1;
					if (Util.pre.containsKey(i)) {
						for (int k = 0; k < Util.pre.get(i).arr[j].length; k++) {
							inequ.getCoeffs()[(n - 1) * x + k] -= Util.pre.get(i).arr[j][k];
						}
					} else {
						if (entry.getKey() != Util.loc)
							inequ.getCoeffs()[(entry.getKey() - 1) * x + j] += -1;
					}
					for (int k = 0; k < entry.getValue().arr[j].length; k++) {
						inequ.getCoeffs()[(Util.loc - 1) * x + num] -= entry.getValue().arr[j][k];
						num++;
					}
					if (j != x - 1) {
						num -= entry.getValue().arr[j].length;
					}
					Util.inequs.add(inequ);
				}
			}
		}
	}

	public static double[] comObject(int count) {
		int a = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		double[] goal = new double[(Util.loc - 1) * a + count];
		Iterator<Map.Entry<String, Double>> it = Util.initV.entrySet().iterator();
		int n, x;
		goal[0] = 1;
		while (it.hasNext()) {
			Entry<String, Double> entry = it.next();
			n = Util.vars.get(entry.getKey()).getId();
			goal[n] = entry.getValue();

			x = Util.vars.size() + 1;
			for (int i = 1; i < n; i++) {
				x += Util.vars.size() - i + 1;
			}
			Iterator<Map.Entry<String, Double>> it1 = Util.initV.entrySet().iterator();
			while (it1.hasNext()) {
				Entry<String, Double> entry1 = it1.next();
				int n1 = Util.vars.get(entry1.getKey()).getId();
				if (n > n1)
					continue;
				else
					goal[x + n1 - n] = entry.getValue() * entry1.getValue();
			}
		}
		return goal;
	}

	public static double[] computeLP(int count) throws LpSolveException {
		double[] varsVal = new double[0];
		int a = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		if(Util.inequs.size() == 0) {
			return varsVal;
		}
		int nVars = Util.inequs.get(0).getCoeffs().length;
		LpSolve solver = LpSolve.makeLp(0, nVars);// rows,columns
		solver.strSetObjFn(ArrayToString(comObject(count)));
		solver.setMinim();
		for (int i = 1; i <= nVars - count; i++) {
			solver.setLowbo(i, -1.0 / 0);
		}
		for (int i = 0; i < Util.inequs.size(); i++) {
			solver.strAddConstraint(ArrayToString(Util.inequs.get(i).coeffs), solver.EQ, Util.inequs.get(i).c);
		}

		if (Util.debug) {
			solver.printLp();
			System.out.println(solver.getObjective());
		}

		if (solver.solve() == solver.OPTIMAL) {
			varsVal = solver.getPtrVariables();
			for (int i = 0; i < varsVal.length; i++) {
				BigDecimal b = new BigDecimal(varsVal[i]);
				varsVal[i] = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
			}
		}

		if (varsVal.length == 0) {
			System.out.println("Sorry,temporarily unable to judge the termination of the probability program.");
			return varsVal;
		} else {
			if (Util.debug) {
				System.out.println("T");
				for (int i = 0; i < varsVal.length; i++) {
					System.out.println(varsVal[i]);
				}
			}
		}
		double[] varsVal1 = java.util.Arrays.copyOf(varsVal, varsVal.length + 1);
		BigDecimal b = new BigDecimal(solver.getObjective());
		varsVal1[varsVal.length] = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		solver.deleteLp();
		return varsVal1;
	}

	public static varScope copyVarScope(varScope v1) {
		varScope v = new varScope();
		v.id = v1.id;
		v.isScattered = v1.isScattered;
		v.max = v1.max;
		v.min = v1.min;
		v.constraint = v1.constraint;
		/*
		 * for(int j=0;j<v1.scatters.size();j++) { v.scatters.add(v1.scatters.get(j)); }
		 */
		return v;
	}

	public static void copyScope(int eq) {// eq:1表示>,-1表示<,0其它
		ArrayList<varScope> var1;
		if (Util.dpRSM.containsKey(Util.previous)) {
			ArrayList<varScope> var = new ArrayList<varScope>();
			HashMap<Integer, ArrayList<varScope>> vars = new HashMap<Integer, ArrayList<varScope>>();
			formWork form = new formWork();
			var1 = dpRSM.get(Util.previous).vars.get(Util.previous + 1);//
			if (var1 != null) {
				for (int i = 0; i < var1.size(); i++) {
					// 求并集
					if (Util.dpRSM.containsKey(Util.loc)) {
						boolean flag = false;
						ArrayList<varScope> arr = Util.dpRSM.get(Util.loc).vars.get(Util.loc + 1);
						for (int j = 0; j < arr.size(); j++) {
							if (arr.get(j).id == var1.get(i).id && var1.get(i).id != 0) {
								flag = true;
								if (var1.get(i).isScattered && eq != 0) {
									if (!(arr.get(j).min > var1.get(i).max) && !(arr.get(j).max < var1.get(i).min)) {
										if (eq > 0) {
											arr.get(j).max = Math.min(arr.get(j).max, var1.get(i).max);
											arr.get(j).min = Math.max(arr.get(j).min + 1, var1.get(i).min);
										} else {
											arr.get(j).max = Math.min(arr.get(j).max - 1, var1.get(i).max);
											arr.get(j).min = Math.max(arr.get(j).min, var1.get(i).min);
										}
									}
									/*
									 * ArrayList<Double> scatter=new ArrayList<Double>(); for(int
									 * k=0;k<var1.get(i).scatters.size();k++) { if(eq) {
									 * if(var1.get(i).scatters.get(k)<=arr.get(j).max&&var1.get(i).scatters.get(k)>=
									 * arr.get(j).min) { scatter.add(var1.get(i).scatters.get(k)); } }else {
									 * if(var1.get(i).scatters.get(k)<arr.get(j).max&&var1.get(i).scatters.get(k)>
									 * arr.get(j).min) { scatter.add(var1.get(i).scatters.get(k)); } } }
									 */
									arr.get(j).isScattered = true;
									// arr.get(j).scatters=scatter;
								} else {
									double max = Math.min(arr.get(j).max, var1.get(i).max);
									double min = Math.max(arr.get(j).min, var1.get(i).min);
									if (max >= min) {
										arr.get(j).max = max;
										arr.get(j).min = min;
									}
								}
							}
						}
						if (!flag) {
							arr.add(Util.copyVarScope(var1.get(i)));
						}
					} else {
						var.add(Util.copyVarScope(var1.get(i)));
						vars.put(Util.loc + 1, var);
						form.vars = vars;
						Util.dpRSM.put(Util.loc, form);
					}
				}
			}
		}
	}

	public static void copyScope2(int loc, int nextLoc) {
		if (nextLoc == Util.lastLoc && loc != 1)
			nextLoc = 1;
		ArrayList<varScope> vars = Util.dpRSM.get(loc).vars.get(loc + 1);
		for (int i = 1; i < vars.size(); i++) {
			Util.dpRSM.get(loc).vars.get(nextLoc).add(vars.get(i));
		}
	}

	public static void rScope(int r, double min, double max, boolean isScatter) {
		varScope v = new varScope();
		ArrayList<varScope> var = new ArrayList<varScope>();
		HashMap<Integer, ArrayList<varScope>> vars = new HashMap<Integer, ArrayList<varScope>>();
		formWork form = new formWork();
		v.isScattered = isScatter;
		v.id = Util.vars.size() + r;
		/*
		 * if(isScatter&&scatters!=null) { v.scatters=scatters; }else {
		 */
		v.max = max;
		v.min = min;
		// }
		if (Util.dpRSM.containsKey(Util.loc) && Util.dpRSM.get(Util.loc).vars.containsKey(Util.loc + 1)) {
			Util.dpRSM.get(Util.loc).vars.get(Util.loc + 1).add(v);
		} else {
			var.add(v);
			vars.put(Util.loc + 1, var);
			form.vars = vars;
			Util.dpRSM.put(Util.loc, form);
		}
	}

	public static void comScope(int loc, int nextLoc, ArrayList<Double> inv) {
		if (nextLoc == Util.lastLoc && loc != 1)
			nextLoc = 1;
		int count = 0, id = 0;
		varScope v = new varScope();
		v.isScattered = false;
		ArrayList<varScope> var = new ArrayList<varScope>();
		HashMap<Integer, ArrayList<varScope>> vars = new HashMap<Integer, ArrayList<varScope>>();
		formWork form = new formWork();
		String s1 = "";
		for (int i = 1; i < inv.size(); i++) {
			if (inv.get(i) != 0) {
				count++;
				id = i;
				if (s1 != "") {
					s1 += "+" + inv.get(i) + "*x" + i;
				} else {
					s1 += inv.get(i) + "*x" + i;
				}
			}
			if (count > 1) {
				id = 0;//判断是否只有一个变量
				break;
			}
		}
		if (id == 0) {
			v.constraint = s1 + ">=" + -1*inv.get(0);
		}
		if (id > 0) {
			if (inv.get(id) < 0) {
				double max = inv.get(0) * -1.0 / inv.get(id);
				v.id = id;
				v.max = max;
			} else {
				double min = inv.get(0) * -1 / inv.get(id);
				v.id = id;
				v.min = min;
			}
		}
		if (Util.dpRSM.containsKey(loc) && Util.dpRSM.get(loc).vars.containsKey(nextLoc)) {
			if (id == 0) {
				Util.dpRSM.get(loc).vars.get(nextLoc).add(v);
			} else if (!Util.unionSet(id, loc, nextLoc, inv)) {
				Util.dpRSM.get(loc).vars.get(nextLoc).add(v);
			}
		} else if (Util.dpRSM.containsKey(loc)) {
			if (!Util.dpRSM.containsKey(loc - 1)) {
				var.add(v);
				vars.put(nextLoc, var);
				Util.dpRSM.get(loc).vars.put(nextLoc, var);
			} else {
				if (Util.dpRSM.get(loc - 1).vars.containsKey(loc)) {
					for (int i = 0; i < Util.dpRSM.get(loc - 1).vars.get(loc).size(); i++) {
						if (Util.dpRSM.get(loc - 1).vars.get(loc).get(i).id == v.id) {
							if (Util.dpRSM.get(loc - 1).vars.get(loc).get(i).isScattered) {
								v.isScattered = true;
							}
							if (!(Util.dpRSM.get(loc - 1).vars.get(loc).get(i).min > v.max)
									&& !(Util.dpRSM.get(loc - 1).vars.get(loc).get(i).max < v.min)) {
								v.max = Math.min(Util.dpRSM.get(loc - 1).vars.get(loc).get(i).max, v.max);
								v.min = Math.max(Util.dpRSM.get(loc - 1).vars.get(loc).get(i).min, v.min);
							}
						}
					}
				}
				var.add(v);
				Util.dpRSM.get(loc).vars.put(nextLoc, var);
			}
		} else {
			var.add(v);
			vars.put(nextLoc, var);
			form.vars = vars;
			Util.dpRSM.put(loc, form);
		}
	}

	public static boolean unionSet(int id, int loc, int nextLoc, ArrayList<Double> inv) {
		boolean flag = false;
		for (int i = 0; id > 0 && i < Util.dpRSM.get(loc).vars.get(nextLoc).size(); i++) {
			if (id == Util.dpRSM.get(loc).vars.get(nextLoc).get(i).id) {
				flag = true;
				varScope v = Util.dpRSM.get(loc).vars.get(nextLoc).get(i);
				if (inv.get(id) < 0) {
					double max = inv.get(0) * -1.0 / inv.get(id);
					/*
					 * if(v.isScattered) { for(int j=0;j<v.scatters.size();j++) {
					 * if(v.scatters.get(j)>max) { v.scatters.remove(j); } } }else {
					 */
					if (max < v.min) {// 不相交
						/*
						 * varScope v1=new varScope(); v1.id=id; v1.isScattered=false; v1.max=max;
						 * Util.dpRSM.get(loc).vars.get(nextLoc).add(v1);
						 */
					} else {// 相交
						v.max = Math.min(max, v.max);
					}
					// }
				} else {
					double min = inv.get(0) * -1 / inv.get(id);
					/*
					 * if(v.isScattered) { for(int j=0;j<v.scatters.size();j++) {
					 * if(v.scatters.get(j)<min) { v.scatters.remove(j); } } }else {
					 */
					if (min > v.max) {// 不相交
						/*
						 * varScope v1=new varScope(); v1.id=id; v1.isScattered=false; v1.min=min;
						 * Util.dpRSM.get(loc).vars.get(nextLoc).add(v1);
						 */
					} else {// 相交
						v.min = Math.max(min, v.min);
					}
					// }
				}
			}
		}
		return flag;
	}

	public static void reScope(int loc, int nextLoc, ArrayList<Double> inv) {
		int count = 0, id = 0;
		// boolean flag=true;
		for (int i = 1; i < inv.size(); i++) {
			if (inv.get(i) != 0) {
				count++;
				id = i;
			}
			if (count > 1) {
				id = 0;
				break;
			}
		}

		if (!Util.unionSet(id, loc, nextLoc, inv)) {
			varScope v1 = new varScope();
			v1.id = id;
			if (inv.get(id) < 0) {
				double max = inv.get(0) * -1.0 / inv.get(id);
				v1.max = max;
			} else {
				double min = inv.get(0) * -1 / inv.get(id);
				v1.min = min;
			}
			Util.dpRSM.get(loc).vars.get(nextLoc).add(0, v1);
		}
	}

	public static void diffBound(double[] varsVal) {
		int a = Util.vars.size() + ((Util.vars.size() + 1) * Util.vars.size()) / 2 + 1;
		int r = Util.vars.size();
		for (int i = 1; i < Util.loc; i++) {
			String s = "", s1;
			int m = 1, n = 1;

			if (Util.maxInv.containsKey(i)) {
				int j = Util.maxInv.get(i).getId();
				if (Util.maxInv.get(i).isR()) {
					s1 = "(x" + j + "+x" + (r + Util.maxInv.get(i).getrId()) + ")";
				} else {
					s1 = "(x" + j + "+" + Util.maxInv.get(i).getMean() + ")";
				}
				Util.dpRSM.get(i).varSub.put(j, s1);
			}

			for (int j = 0; j < a; j++) {
				if (j == 0) {
					if (varsVal[a * (i - 1) + j] != 0)
						s += varsVal[a * (i - 1) + j] + "";
				} else if (j > 0 && j <= Util.vars.size()) {
					if (varsVal[a * (i - 1) + j] != 0)
						s += "+" + varsVal[a * (i - 1) + j] + "*x" + j;
				} else {
					if (varsVal[a * (i - 1) + j] != 0)
						s += "+" + varsVal[a * (i - 1) + j] + "*x" + m + "*x" + n;
					if (n == Util.vars.size()) {
						m++;
						n = m;
					} else {
						n++;
					}
				}
			}
			Util.dpRSM.get(i).fun = s;
		}
	}

	static {
		System.loadLibrary ( "lingojni64" );//for linux
		//System.loadLibrary ( "lingj64_18" );//for windows
	}

	public static double callLingo(String fileName) {
		int nErr = 0;
		Lingd13 lng = new Lingd13();
		// create the Lingo environment
		Object pnLngEnv = lng.LScreateEnvLng();
		if (pnLngEnv == null) {
			if (Util.debug)
				System.out.println("***Unable to create Lingo environment***");
			return -1;
		}

		// open a log file
		nErr = lng.LSopenLogFileLng(pnLngEnv, "lingo.log");
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LSopenLogFileLng() error***: " + nErr);
			return -1;
		}

		// pass pointers to output areas
		int[] nPointersNow = new int[1];
		double obj[] = new double[1];
		nErr = lng.LSsetPointerLng(pnLngEnv, obj, nPointersNow);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LSsetPointerLng()2 error***: " + nErr);
			return -1;
		}
		double dStatus[] = new double[1];
		nErr = lng.LSsetPointerLng(pnLngEnv, dStatus, nPointersNow);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LSsetPointerLng()3 error***: " + nErr);
			return -1;
		}

		dStatus[0] = -1;
		String sScript = "SET ECHOIN 1 \n TAKE " + Util.basedir + "/" + fileName + "\n";
		sScript = sScript + "GO" + "\n";
		sScript = sScript + "QUIT" + "\n";
		nErr = lng.LSexecuteScriptLng(pnLngEnv, sScript);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LSexecuteScriptLng() error***: " + nErr);
			return -1;
		}
		nErr = lng.LSclearPointersLng(pnLngEnv);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LSclearPointersLng() error***: " + nErr);
			return -1;
		}

		// check the solution status
		if (dStatus[0] != lng.LS_STATUS_GLOBAL_LNG)
			if (Util.debug)
				System.out.println("***Unable to Solve*** dStatus:" + dStatus[0]);

		// close Lingo's log file
		nErr = lng.LScloseLogFileLng(pnLngEnv);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			if (Util.debug)
				System.out.println("***LScloseLogFileLng() error***: " + nErr);
			return -1;
		}

		// delete the Lingo environment
		lng.LSdeleteEnvLng(pnLngEnv);
		if (nErr != lng.LSERR_NO_ERROR_LNG) {
			System.out.println("***LSdeleteEnvLng() error***: " + nErr);
			return -1;
		}
		return obj[0];
	}

	public static void writeFile(String data, String fileName) {
		File file = new File(Util.basedir + "/" + fileName);
		try {
			file.createNewFile();
			Writer out = new FileWriter(file);
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class order {
		int id;
		int n;// 取第n个
		int m;// 一共又m个

		public order(int id, int n, int m) {
			this.id = id;
			this.n = n;
			this.m = m;
		}
	}

	public static void assistance(HashMap<Integer, formWork_ass> dpRSM) {
		formWork_ass f;
		HashMap<Integer, ArrayList<varScope>> ha;
		Iterator<Entry<Integer, formWork>> it = Util.dpRSM.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, formWork> entry = it.next();
			f = new formWork_ass();
			f.fun = entry.getValue().fun;
			f.varSub = entry.getValue().varSub;
			Iterator<Entry<Integer, ArrayList<varScope>>> it1 = entry.getValue().vars.entrySet().iterator();
			while (it1.hasNext()) {
				Entry<Integer, ArrayList<varScope>> entry1 = it1.next();
				ha = new HashMap<Integer, ArrayList<varScope>>();
				String s = "";
				for (int i = 0; i < entry1.getValue().size(); i++) {
					if (entry1.getValue().get(i).id > 0) {
						if (ha.containsKey(entry1.getValue().get(i).id)) {
							ha.get(entry1.getValue().get(i).id).add(entry1.getValue().get(i));
						} else {
							ArrayList<varScope> arr = new ArrayList<Util.varScope>();
							arr.add(entry1.getValue().get(i));
							ha.put(entry1.getValue().get(i).id, arr);
						}
					} else {
						s += entry1.getValue().get(i).constraint + ";\n";
					}
				}
				f.vars.put(entry1.getKey(), ha);
				f.constraint.put(entry1.getKey(), s);
			}
			dpRSM.put(entry.getKey(), f);
		}
	}

	public static void putOrder(ArrayList<order> order, HashMap<Integer, ArrayList<varScope>> map) {
		Iterator<Entry<Integer, ArrayList<varScope>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ArrayList<varScope>> entry = it.next();
			if (entry.getValue().size() > 1) {
				order.add(new order(entry.getKey(), 0, entry.getValue().size()));
			}
		}
	}

	public static String[] boundScript(String sScriptData, int j, int n, HashMap<Integer, ArrayList<varScope>> invE) {
		String[] sScript = new String[2];
		sScript[0] = "";
		if (invE.get(j).get(n).max == invE.get(j).get(n).min) {
			sScriptData += "x" + invE.get(j).get(n).id + "=" + invE.get(j).get(n).max + "; \n";
		} else {
			if (invE.get(j).get(n).max < Double.POSITIVE_INFINITY) {
				sScript[0] += "x" + invE.get(j).get(n).id + "<=" + invE.get(j).get(n).max + "; \n";
			}
			if (invE.get(j).get(n).min > Double.NEGATIVE_INFINITY) {
				sScript[0] += "x" + invE.get(j).get(n).id + ">=" + invE.get(j).get(n).min + "; \n";
			}
			if (invE.get(j).get(n).isScattered) {
				sScript[0] += "@gin(x" + (invE.get(j).get(n).id) + "); \n";
			}
		}
		sScript[1] = sScriptData;
		return sScript;
	}

	public static double comTerminalTime(int r, int n, double result) {
		double max = 0, min = 0, tmpMax, tmpMin;
		boolean isMax = false, isMin = false;
		HashMap<Integer, formWork_ass> dpRSM = new HashMap<Integer, Util.formWork_ass>();
		assistance(dpRSM);

		for (int i = 1; i < Util.loc; i++) {
			HashMap<Integer, HashMap<Integer, ArrayList<varScope>>> map = dpRSM.get(i).vars;
			Iterator<Entry<Integer, HashMap<Integer, ArrayList<varScope>>>> it = map.entrySet().iterator();

			while (it.hasNext()) {
				ArrayList<order> order = new ArrayList<order>();
				Entry<Integer, HashMap<Integer, ArrayList<varScope>>> entry = it.next();
				HashMap<Integer, ArrayList<varScope>> invE = entry.getValue();
				Iterator<Entry<Integer, String>> it2 = dpRSM.get(i).varSub.entrySet().iterator();
				Util.putOrder(order, entry.getValue());

				String fun;
				if (entry.getKey() == Util.loc) {
					fun = "-1";
				} else {
					fun = dpRSM.get(entry.getKey()).fun;
				}
				while (it2.hasNext() && entry.getKey() != Util.loc) {
					Entry<Integer, String> entry2 = it2.next();
					String s1 = "x" + entry2.getKey() + "";
					String s2 = entry2.getValue();
					fun = fun.replace(s1, s2);
				}
				fun += "-(" + dpRSM.get(i).fun + ")";
				String sScript = "MODEL: \nSETS: \nVAR/1.." + r + "/:x; \nENDSETS \n";
				sScript += "[OBJECTIVE] MIN = " + fun + ";\n";
				sScript += dpRSM.get(i).constraint.get(entry.getKey());
				String sScriptData = "DATA:\n";
				String[] sScriptTemp = new String[2];

				if (order.size() == 0) {
					for (int j = 1; j <= r; j++) {
						if (invE.containsKey(j)) {
							// 直接取第一个
							sScriptTemp = Util.boundScript(sScriptData, j, 0, invE);
							sScript += sScriptTemp[0];
							sScriptData = sScriptTemp[1];
						}
					}
					// 计算最大最小值
					sScript += sScriptData + "@POINTER(1)=OBJECTIVE;\n" + "@POINTER(2)=@STATUS();\n" + "ENDDATA\n"
							+ "END";

					Util.writeFile(sScript, "lingo_min.lg4");
					tmpMin = Util.callLingo("lingo_min.lg4");
					Util.writeFile(sScript.replace("MIN", "MAX"), "lingo_max.lg4");
					tmpMax = Util.callLingo("lingo_max.lg4");
					if (!isMax || max < tmpMax) {
						max = tmpMax;
						isMax = true;
					}
					if (!isMin || min > tmpMin) {
						min = tmpMin;
						isMin = true;
					}
				} else {
					boolean flag = true;
					int t = order.size() - 1;
					while (t >= 0) {
						String sScript1 = sScript;
						String sScriptData1 = sScriptData;
						for (int j = 1; j < order.get(t).id; j++) {
							if (invE.containsKey(j)) {
								// 直接取第一个
								sScriptTemp = Util.boundScript(sScriptData1, j, 0, invE);
								sScript1 += sScriptTemp[0];
								sScriptData1 = sScriptTemp[1];
							}
						}
						for (int j = r; j >= order.get(t).id; j--) {
							if (invE.containsKey(j)) {
								if (j == order.get(t).id && order.get(t).n == order.get(t).m) {
									if (t >= 1) {
										t--;
										for (int k = t + 1; k < order.size(); k++) {
											order.get(k).n = 0;
										}
									}
								}
								int k;
								for (k = order.size() - 1; k >= t; k--) {
									if (order.get(k).id == j && order.get(k).n < order.get(k).m) {
										order.get(k).n++;
										flag = false;
										break;
									}
								}
								if (flag) {
									// 取第一个
									sScriptTemp = Util.boundScript(sScriptData1, j, 0, invE);
									sScript1 += sScriptTemp[0];
									sScriptData1 = sScriptTemp[1];
								} else {
									// 取第der.get(j).n个
									sScriptTemp = Util.boundScript(sScriptData1, j, order.get(k).n - 1, invE);
									sScript1 += sScriptTemp[0];
									sScriptData1 = sScriptTemp[1];
								}

							}
						}

						flag = true;

						// 计算最大最小值
						sScript1 += sScriptData1 + "@POINTER(1)=OBJECTIVE;\n" + "@POINTER(2)=@STATUS();\n" + "ENDDATA\n"
								+ "END";

						Util.writeFile(sScript1, "lingo_min.lg4");
						tmpMin = Util.callLingo("lingo_min.lg4");
						Util.writeFile(sScript1.replace("MIN", "MAX"), "lingo_max.lg4");
						tmpMax = Util.callLingo("lingo_max.lg4");
						if (!isMax || max < tmpMax) {
							max = tmpMax;
							isMax = true;
						}
						if (!isMin || min > tmpMin) {
							min = tmpMin;
							isMin = true;
						}
						if (t == 0 && order.get(t).n == order.get(t).m) {
							break;
						}
					}
				}
			}
		}

		// 计算
		String sScript = "MODEL: \n"
		+"[OBJECTIVE]MIN=N;\n"
		+"@exp((-2*((N-1)-"+result+")^2)/((N-1)*"+(max - min)+"^2))<"+TerTime+";\n"
		+"(N-1)>"+result+";\n" 
		+"DATA: \n"
		+ "@POINTER(1)=OBJECTIVE;\n" + "@POINTER(2)=@STATUS();\n" + "ENDDATA\n"
		+ "END";
		Util.writeFile(sScript, "lingo_result.lg4");
		tmpMin = Util.callLingo("lingo_result.lg4");
		return tmpMin;
	}
}
