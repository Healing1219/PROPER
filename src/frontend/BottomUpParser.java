package frontend;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mathworks.toolbox.javabuilder.MWException;

import backend.ClibCall;
import backend.CodeTreeBuilder;
import backend.Intepretor;
import backend.Util;
import backend.Util.PathCom;
import backend.Util.Sym;
import backend.Util.Var;
import backend.Util.invExp;
import backend.Util.nestInfo;
import backend.Util.varScope;
import lpsolve.LpSolveException;
import nonlinear.NlpSolver;

public class BottomUpParser {
	public static void main(String[] args) {

		ProductionManager productionManager = ProductionManager.getProductionManager();
		productionManager.initProductions();
		if (Util.debug) {
			productionManager.printAllProductions();
		}
		productionManager.runFirstSetAlgorithm();
		GrammarStateManager stateManager = GrammarStateManager.getGrammarManager();
		stateManager.buildTransitionStateMachine();
		CodeTreeBuilder treeBuilder = CodeTreeBuilder.getCodeTreeBuilder();
		Intepretor intepretor = Intepretor.getIntepretor();
		System.out.println("Input string for parsing:");
		LRStateTableParser parser = new LRStateTableParser(new Lexer());
		parser.parse();

		// 读取性质所需变量
/*		PropManager pm = new PropManager();
		HashSet<Symbol> live_pro = new HashSet<Symbol>();
//		String prop_init = "c<=8";
//		String prop_init = "count < 1";
//		String prop_init = "points >= 10";
//		String prop_init = "points - pointsErr >= 5";
		String prop_init = "x>0&&y>0&&z>0";
//		String prop_init = "pointsErr - points <= 5";
//		String prop_init = "f -f1 >= 0.1";
		pm.propLex(live_pro, prop_init);//检验性质是否合法

		int iteration = 0;// 所有探寻路径数
		
		for (int i = 0; i < 90;) {
			System.out.println("iteration:"+iteration);
			iteration++;
			ClibCall.count = 0;// 变量数重新赋值为0
			Util.wss = new HashSet<Symbol>();
			Util.lives = new HashSet<Symbol>();
			Util.varList = new ArrayList<Var>();
			Util.varMap = new HashMap<Symbol, ArrayList<Double>>(); // 记录变量的系数
			Util.constrainList = new ArrayList<ArrayList<Double>>();// 记录约束
			Util.pathInfoMap = new HashMap<Integer, Util.pathInfo>();// 记录每条路径的live集,var和constrain
			Util.liveTemp = new ArrayList<Sym>();
			Util.testPath = new ArrayList<Integer>();
			Util.testState = new ArrayList<Boolean>();
			Util.nestCount = new ArrayList<nestInfo>();
			ArrayList<Integer> path = new ArrayList<Integer>();// 记录路径
			ArrayList<Integer> path_slice = new ArrayList<Integer>();// 记录裁剪后的路径
			//ArrayList<HashSet<Symbol>> pathSym = new ArrayList<HashSet<Symbol>>();//按path顺序记录每条语句的live集
			
			long startTime = System.currentTimeMillis();
			if (intepretor != null) {
				intepretor.Execute(treeBuilder.getCodeTreeRoot(), path);
			}
			
			Util.lives.addAll(live_pro);//加入性质的live集
			Util.slicePath(path, path_slice);
			Util.timePath = System.currentTimeMillis() - startTime;
			Util.completedArr(Util.constrainList);
			
			//判断是否是唯一路径
			i=Util.uniquePath(i, path_slice, prop_init);
		}
		Util.nExec = iteration;

		// 推理上下界部分
		BigDecimal[] pathP = new BigDecimal[2];
		BigDecimal[] assertP = new BigDecimal[2];
		DecimalFormat df = new DecimalFormat("0.000000");
		try {
			pathP = Util.readData(0);
			assertP = Util.readData(1);
			Util.appendContentToFile("\n"+prop_init+": [" + df.format(assertP[1]) + "," + df.format(assertP[0].subtract(pathP[1]).add(new BigDecimal("1"))) + "] \n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// 判断程序是否终止
		if (intepretor != null) {
			intepretor.Execute(treeBuilder.getCodeTreeRoot(),true);
			Util.isWhile=false;
			Util.loc=1;
			Util.flag=0;
			intepretor.Execute(treeBuilder.getCodeTreeRoot(),false);
			int count=Util.expressInv();
			Util.comInequs(count);
			double[] varsVal = null;
			try {
				varsVal=Util.computeLP(count);
			} catch (LpSolveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//终止时间
			Util.diffBound(varsVal);
			if(varsVal.length!=0) {
				double time=Util.comTerminalTime(Util.vars.size()+Util.r,50000,varsVal[varsVal.length-1]);
				System.out.println("终止时间："+time);
			}	
		}
	}
}
