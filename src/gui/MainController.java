package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import backend.ClibCall;
import backend.CodeTreeBuilder;
import backend.Intepretor;
import backend.Util;
import backend.Util.PathCom;
import backend.Util.Var;
import frontend.GrammarStateManager;
import frontend.LRStateTableParser;
import frontend.Lexer;
import frontend.ProductionManager;
import frontend.PropManager;
import frontend.Symbol;
import inference.Polytope;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lpsolve.LpSolveException;

public class MainController {
	private String code = "";
	PropManager pm = new PropManager();
	PathCom pathCom = new PathCom();
	HashSet<Symbol> live_pro = new HashSet<Symbol>();
	
	int iteration = 0;// 所有探寻路径数
	long timePath, timeVol;
	private Main main;

	public void setMain(Main main) {
		this.main = main;
	}
	
	@FXML
	private RadioMenuItem run;
	
	@FXML
	private RadioMenuItem debug;

	@FXML
	private TextArea textarea;

	@FXML
	private TextField property;

	@FXML
	private TextField depth;
	
	@FXML
	private TextField time;

//	@FXML
//	private Text t4;
//	
//	@FXML
//	private Text t5;

	@FXML
	private TextArea consoletext;
	
	@FXML
	public TextArea logtext;

	@FXML
	private void open() {

		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("txt files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show save file dialog
		File file = fileChooser.showOpenDialog(main.getPrimaryStage());
		try {
			if (file != null) {
				//int lineNum = 0;
				FileReader read = new FileReader(file);
				BufferedReader br = new BufferedReader(read);
				String temp;
				textarea.clear();
				while ((temp = br.readLine()) != null) {
					//lineNum++;
					textarea.appendText(temp+"\r\n");
					// Text text=new Text(lineNum+"");
					// gridLine.add(text, 1, lineNum);
				}
				br.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void export() {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("txt files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show save file dialog
		File file = fileChooser.showSaveDialog(main.getPrimaryStage());
		try {
			if (file != null) {
				FileWriter writer = new FileWriter(file);
				String str = textarea.getText();
				writer.write(str);
				writer.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void quit() {
		main.getPrimaryStage().close();
	}

	@FXML
	private boolean checkGrammar() {
		code = textarea.getText();
		if (code.trim().length() == 0) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("Validation code cannot be empty!");
			alert.setTitle("Error");
			alert.showAndWait();
			return false;
		}
		LRStateTableParser parser = new LRStateTableParser(new Lexer(), code);
		if (parser.parse()) {
			consoletext.setText("\nThe input can be accepted.");
			return true;
		}
		consoletext.setText("\nThe input is denied.");
		return false;
	}

	@FXML
	private void verification() {
		setDepth();
		String p = property.getText();
		if (p.trim().length() == 0) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setContentText("Please enter a property to be verified!");
			alert.setTitle("Warning");
			alert.showAndWait();
			return;
		}
		if (checkGrammar()) {
			if (!pm.propLex(live_pro, p)) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setContentText("Illegal property!");
				alert.setTitle("Error");
				alert.showAndWait();
				return;
			}
			consoletext.appendText("\n\n-----------Start Selecting Path-----------");
			selectPath(p);
			
			BigDecimal[] pathP = new BigDecimal[2];
			BigDecimal[] assertP = new BigDecimal[2];
			DecimalFormat df = new DecimalFormat("0.00");
			try {
				consoletext.appendText("\n\n-----------Start Calculating Path Coverage-----------");
				pathP = Util.readData(0);
				consoletext.appendText("\nPath Coverage: "+df.format(pathP[1].multiply(new BigDecimal("100")))+"%");
				consoletext.appendText("\n\n-----------Start Calculating Assertion Interval-----------");
				assertP = Util.readData(1);
				consoletext.appendText("\n"+p+": [" + df.format(assertP[1].multiply(new BigDecimal("100"))) + "%, " + df.format((assertP[0].subtract(pathP[1]).add(new BigDecimal("1"))).multiply(new BigDecimal("100"))) + "%] \n");
				//Util.appendContentToFile("\n"+p+": [" + df.format(assertP[1].multiply(new BigDecimal("100"))) + "%, " + df.format((assertP[0].subtract(pathP[1]).add(new BigDecimal("1"))).multiply(new BigDecimal("100"))) + "%] \n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			t4.setText("Time of Computing Bound:"+(System.currentTimeMillis() - timeVol)/1000.0+"s");
//			t5.setText("Finshed");
		}
	}
	
	@FXML
	private void termination() {
		CodeTreeBuilder treeBuilder = CodeTreeBuilder.getCodeTreeBuilder();
		Intepretor intepretor = Intepretor.getIntepretor();
		setTime();
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
					varsVal=Util.computeLP(count);
				} catch (LpSolveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				//终止时间
				Util.diffBound(varsVal);
				if(varsVal.length!=0) {
					consoletext.appendText("\nThis program terminates!");
					consoletext.appendText("\nThe upper bound of expected termination time: "+(varsVal[varsVal.length-1]-Util.k));
					double n=Util.comTerminalTime(Util.vars.size()+Util.r,50000,varsVal[varsVal.length-1]);
					DecimalFormat df = new DecimalFormat("#.00");  
					consoletext.appendText("\nN: "+df.format(n)+" (threshold does not exceed "+time.getText()+")");
				}else {	
					consoletext.appendText("\n\nUnable to determine whether the program can be terminated.");
					return;
				}
			}
		}
	}
	
	@FXML
	private void showTerminalEx(Event evt) {
		MenuItem m=(MenuItem) evt.getSource();
		File file=new File("./examples/termination/ex_"+m.getText()+".txt");
		property.clear();
		try {
			if (file != null) {
				FileReader read = new FileReader(file);
				BufferedReader br = new BufferedReader(read);
				String temp;
				textarea.clear();
				while ((temp = br.readLine()) != null) {
					textarea.appendText(temp+"\r\n");
				}
				br.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	private void showAssertEx(Event evt) {
		String p="";
		MenuItem m=(MenuItem) evt.getSource();
		File file=new File("./examples/assertions/ex_"+m.getText()+".txt");
		try {
			if (file != null) {
				FileReader read = new FileReader(file);
				BufferedReader br = new BufferedReader(read);
				String temp;
				textarea.clear();
				while ((temp = br.readLine()) != null) {
					textarea.appendText(temp+"\r\n");
				}
				br.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(m.getText()) {
			case "Carton": p="totalWeight>=5.5";break;
			case "Herman": p="count>=1";break;
			case "Framingham": p="pointsErr-points>=5";break;
			case "Sum-three": p="x>5&&y>0";break;
			case "Ckd-epi": p="f-f1>=0.1";break;
		}
		property.setText(p);
	}

	@FXML
	private void help() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText("你可以访问 https://github.com/xxxxxx 获取更多信息");
		alert.setTitle("Help");
		alert.showAndWait();
	}
	
	private void setLog() {
		if(debug.isSelected()) {
			
		}
	}

	private void selectPath(String prop_init) {
		consoletext.clear();
		iteration = 0;
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
		consoletext.appendText("\nTotal Execution Paths: "+iteration);
		consoletext.appendText("\nUnique Path: "+ Util.pathMap.size());
	}

	private void setDepth() {
		String d = depth.getText();
		// 判断字符串是否是数字
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		if (pattern.matcher(d).matches()) {
			int d1 = Integer.valueOf(d);
			if (d1 <= 0 || d1 >= 20) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setContentText("Better to enter between 1 and 20.");
				alert.setTitle("Warning");
				alert.showAndWait();
			}
			Util.depth = d1;
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("please input a number.");
			alert.setTitle("Error");
			alert.showAndWait();
			depth.setText("");
		}
	}
	
	private void setTime() {
		String t = time.getText();
		double t1 = Double.valueOf(t);
		Util.TerTime = t1;
	}

	@FXML
	private void clear() {
		consoletext.setText(null);
	}

	@FXML
	private void clearlog() {
		logtext.setText(null);
	}
}
