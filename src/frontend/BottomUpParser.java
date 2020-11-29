package frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;

import backend.ClibCall;
import backend.CodeTreeBuilder;
import backend.Intepretor;
import backend.Util;
import backend.Util.Var;
import lpsolve.LpSolveException;

public class BottomUpParser {
	static PropManager pm = new PropManager();
	static HashSet<Symbol> live_pro = new HashSet<Symbol>();
	static int iteration = 0;// 所有探寻路径数
	
	static private String readFile(String strFile){
		String code = "";
		try {
			File file = new File(strFile);
			FileReader read = new FileReader(file);
			BufferedReader br = new BufferedReader(read);
			String temp;
			while ((temp = br.readLine()) != null) {
				code += temp+"\n";
			}
			br.close();
		}catch(FileNotFoundException e){
            System.err.println("The fileName or filePath is incorrect!");
        }catch(Exception e){
            e.printStackTrace();
        }
		return code;
    }
	
	static private boolean checkGrammar() {
		System.out.println("\nInput fileName or filePath(Relative path from project root directory):");
		Scanner s = new Scanner(System.in);
		String fileName = s.nextLine();
		String code = readFile(Util.basedir+"/"+fileName);
		System.out.println("\n"+code);
		LRStateTableParser parser = new LRStateTableParser(new Lexer(), code);
		System.out.println("\n-----------Start Checking Grammar-----------");
		return parser.parse();
	}
	
	static private void selectPath(String prop_init) {
		int iteration = 0;
		Util.pathMap = new HashMap<ArrayList<Integer>, Util.PathCom>();
		CodeTreeBuilder treeBuilder = CodeTreeBuilder.getCodeTreeBuilder();
		Intepretor intepretor = Intepretor.getIntepretor();
		//timePath = System.currentTimeMillis();
		for (int i = 0; i < 90;) {
			//System.out.println(iteration);
			iteration++;
			ClibCall.count = 0;// 变量数重新赋值为0
			Util.wss = new HashSet<Symbol>();
			Util.lives = new HashSet<Symbol>();
			Util.varList = new ArrayList<Var>();
			Util.varMap = new HashMap<Symbol, ArrayList<Double>>(); // 记录变量的系数
			Util.constrainList = new ArrayList<ArrayList<Double>>();// 记录约束
			Util.pathInfoMap = new HashMap<Integer, Util.pathInfo>();// 记录每条路径的live集,var和constrain
			Util.liveTemp = new ArrayList<Util.Sym>();
			Util.testPath = new ArrayList<Integer>();
			Util.testState = new ArrayList<Boolean>();
			Util.nestCount = new ArrayList<Util.nestInfo>();
			ArrayList<Integer> path = new ArrayList<Integer>();// 记录路径
			ArrayList<Integer> path_slice = new ArrayList<Integer>();// 记录裁剪后的路径
			//long startTime = System.currentTimeMillis();
			if (intepretor != null) {
				intepretor.Execute(treeBuilder.getCodeTreeRoot(), path);
			}
			Util.lives.addAll(live_pro);//加入性质的live集
			Util.slicePath(path, path_slice);
			//Util.timePath = System.currentTimeMillis() - startTime;
			Util.completedArr(Util.constrainList);
			
			//判断是否是唯一路径
			i=Util.uniquePath(i, path_slice, prop_init);
		}
		Util.nExec=iteration;
		System.out.println("\nTotal Execution Paths: "+iteration);
		System.out.println("\nUnique Path: "+ Util.pathMap.size());
	}
	
	public static void main(String[] args) {
		ProductionManager productionManager = ProductionManager.getProductionManager();
		productionManager.initProductions();
		productionManager.runFirstSetAlgorithm();
		GrammarStateManager stateManager = GrammarStateManager.getGrammarManager();
		stateManager.buildTransitionStateMachine();
		System.out.println("Which function do you want to choose?");
		System.out.println("1. Termination analysis");
		System.out.println("2. Assertions analysis");
		System.out.print("(enter 1 or 2):");
		Scanner s = new Scanner(System.in);
		int function = s.nextInt();
		//int function = 2;
		
		if(function == 1) {
			CodeTreeBuilder treeBuilder = CodeTreeBuilder.getCodeTreeBuilder();
			Intepretor intepretor = Intepretor.getIntepretor();
			// 判断程序是否终止
			if (checkGrammar()) {
				if (intepretor != null) {
					Util.r=0;
					Util.loc=0;
					Util.previous=0;
					Util.locType=0;
					Util.loop2=0;
					Util.isWhile=false;
					Util.ifNo=new ArrayList<ArrayList<Integer>>();
					Util.whileNo=new Stack<Integer>();
					Util.initV=new HashMap<String, Double>();
					Util.vars=new HashMap<String, Util.R>();
					Util.maxInv=new HashMap<Integer, Util.R>();
					Util.preInv=new HashMap<Integer, ArrayList<ArrayList<Double>>>();
					Util.invariant=new HashMap<Integer, HashMap<Integer,ArrayList<ArrayList<Double>>>>();
					Util.pre=new HashMap<Integer, Util.invExp>();
					Util.g=new HashMap<Integer, HashMap<Integer,Util.invExp>>();
					Util.invariantExp=new HashMap<Integer, HashMap<Integer,Util.invExp>>();
					Util.inequs=new ArrayList<Util.Inequ>();
					Util.dpRSM=new HashMap<Integer, Util.formWork>();
					intepretor.Execute(treeBuilder.getCodeTreeRoot(),true);
					Util.isWhile=false;
					Util.loc=1;
					Util.flag=0;
					intepretor.Execute(treeBuilder.getCodeTreeRoot(),false);
					int count=Util.expressInv();
					Util.comInequs(count);
					double[] varsVal = null;
					try {
						System.out.println("\n\n-----------Whether the Program is to be Terminated-----------");
						varsVal=Util.computeLP(count);
					} catch (LpSolveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
					//终止时间
					if(varsVal.length!=0) {
						System.out.println("\nThis program terminates!");
						Util.diffBound(varsVal);
						System.out.println("\n\n-----------Expected termination time-----------");
						System.out.println("\nThe upper bound of expected termination time: "+(varsVal[varsVal.length-1]-Util.k));
						System.out.println("\n\n-------------Concentration problem-------------");
						double n=Util.comTerminalTime(Util.vars.size()+Util.r,50000,varsVal[varsVal.length-1]);
						DecimalFormat df = new DecimalFormat("#.00");  
						System.out.println("\nN: "+df.format(n)+" (threshold does not exceed "+Util.TerTime+")\n");
					}else {	
						if(Util.inequs.size() == 0) {
							System.out.println("\nThis program terminates!");
							System.out.println("\n\n-----------Expected termination time-----------");
							System.out.println("\nIt does not enter the while-loop and expected termination time: 0");
						}else {
							System.out.println("\nUnable to determine whether the program can be terminated.\n");
						}
						return;
					}
				}
			}
		} else if(function == 2) {
			System.out.println("\nEnter the assertion to be verified:");
			Scanner s1 = new Scanner(System.in);
			String p = s1.nextLine();
			//String p="points >= 10";
			//PropManager pm = new PropManager();
			//HashSet<Symbol> live_pro = new HashSet<Symbol>();
			if (checkGrammar()) {
				if (!pm.propLex(live_pro, p)) {
					System.out.println("Illegal property!");
					return;
				}
				System.out.println("\n\n-----------Start Selecting Path-----------");
				selectPath(p);
				
				BigDecimal[] pathP = new BigDecimal[2];
				BigDecimal[] assertP = new BigDecimal[2];
				DecimalFormat df = new DecimalFormat("0.00");
				try {
					System.out.println("\n\n-----------Start Calculating Path Coverage-----------");
					pathP = Util.readData(0);
					System.out.println("\nPath Coverage: "+df.format(pathP[1].multiply(new BigDecimal("100")))+"%");
					System.out.println("\n\n-----------Start Calculating Assertion Interval-----------");
					assertP = Util.readData(1);
					System.out.println("\n"+p+": [" + df.format(assertP[1].multiply(new BigDecimal("100"))) + "%, " + df.format((assertP[0].subtract(pathP[1]).add(new BigDecimal("1"))).multiply(new BigDecimal("100"))) + "%] \n");
					//Util.appendContentToFile("\n"+p+": [" + df.format(assertP[1].multiply(new BigDecimal("100"))) + "%, " + df.format((assertP[0].subtract(pathP[1]).add(new BigDecimal("1"))).multiply(new BigDecimal("100"))) + "%] \n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
