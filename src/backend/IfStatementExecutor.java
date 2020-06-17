package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IfStatementExecutor extends BaseExecutor {

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {

		Integer val;
		int testNo;

		ICodeNode res = executeChild(root, 0, path);
		String s = "0";
		if (res.getAttribute(ICodeKey.VALUE) != null) {
			s = res.getAttribute(ICodeKey.VALUE).toString();
		}
		if (s == "true") {
			val = 1;
		} else if (s == "false") {
			val = 0;
		} else {
			val = Integer.parseInt(s);
		}

		if (val == 1) {
			Util.testState.add(Util.loop - 1, true);
		} else {
			Util.testState.add(Util.loop - 1, false);
		}
		copyChild(root, res);

		if (val != null && val != 0) {
			executeChild(root, 1, path);
			Util.pathInfoMap.get(root.getChildren().get(0).getNo()).sym.ws.addAll(Util.liveTemp.get(--Util.loop).live);// 最后一个test
			for (int i = 1; i < Util.testNo; i++) {
				testNo = Util.testPath.get(Util.testPath.size() - i - 1);// 从倒数第二个test序号开始取值
				Util.pathInfoMap.get(testNo).sym.ws.addAll(Util.liveTemp.get(Util.loop).live);
			}
			Util.liveTemp.remove(Util.loop);
			Util.testState.remove(Util.loop);
		} else {
			Util.exeValid = false;
			res = executeChild(root, 1, path);
			Util.pathInfoMap.get(root.getChildren().get(0).getNo()).sym.ws.addAll(Util.liveTemp.get(--Util.loop).live);
			for (int i = 1; i < Util.testNo; i++) {
				testNo = Util.testPath.get(Util.testPath.size() - i - 1);
				Util.pathInfoMap.get(testNo).sym.ws.addAll(Util.liveTemp.get(Util.loop).live);
			}
			Util.liveTemp.remove(Util.loop);
			Util.testState.remove(Util.loop);
		}
		// 结束一层if嵌套
		if (root.getParent().getAttribute(ICodeKey.TokenType).toString() != "IF_ELSE_STATEMENT") {
			Util.testNo = 0;
			Util.exeValid = true;
			if (Util.testState.contains(false)) {
				Util.exeValid = false;
			}
			// if中的语句数
			for (int i = Util.testPath.size() - 1; i >= 0 && Util.exeValid; i--) {
				if (Util.nestCount.size() > Util.testPath.get(i) && Util.nestCount.get(Util.testPath.get(i)).isFlag()) {
					Util.nestCount.get(Util.testPath.get(i)).setFlag(false);
					break;
				}
			}
		}
		return root;
	}

	@Override
	public Object Execute(ICodeNode root, boolean isReadVar) {
		ArrayList<Integer> arr;
		ArrayList<Double> arr1;
		ArrayList<ArrayList<Double>> arrs;
		if (isReadVar) {
			executeChild(root, 0, isReadVar);
			executeChild(root, 1, isReadVar);
		} else {
			Util.loop2++;
			if (Util.loop2 > Util.ifNo.size()) {
				arr = new ArrayList<Integer>();
				arr.add(Util.loc);
				Util.ifNo.add(arr);
			} else {
				Util.previous = Util.ifNo.get(Util.loop2 - 1).get(0)-1;
				Util.ifNo.get(Util.loop2 - 1).add(Util.loc);
			}
			ICodeNode res = executeChild(root, 0, isReadVar);
			copyChild(root, res);
			// 将前面的test反向加入
			for(int j=0;j < Util.ifNo.get(Util.loop2-1).size() - 1; j++) {
				int no=Util.ifNo.get(Util.loop2-1).get(j);
				//找到if的else路径
				if(j==Util.ifNo.get(Util.loop2-1).size() - 2) {
					arrs=new ArrayList<ArrayList<Double>>();		
					for(int i=0;i<Util.invariant.get(no).get(no+1).size();i++) {
						arr1 = new ArrayList<Double>();
						int n=1;
						if(i==0) n=-1;
						for(int k=0;k<Util.invariant.get(no).get(no+1).get(i).size();k++) {
							arr1.add(n*Util.invariant.get(no).get(no+1).get(i).get(k));
						}
						arrs.add(arr1);
						if(i==0)
							Util.comScope(no,Util.loc-1, arr1);
					}
					Util.invariant.get(no).put(Util.loc-1, arrs);
					Util.copyScope2(no, Util.loc-1);
				}
				
				arr1 = new ArrayList<Double>();
				for (int i = 0; i < Util.invariant.get(no).get(no+1).get(0).size(); i++) {
					arr1.add(-1.0 * Util.invariant.get(no).get(no+1).get(0).get(i));
				}
				Util.preInv.get(Util.loc - 1).add(arr1);
				Util.invariant.get(Util.loc - 1).get(Util.loc).add(arr1);
				Util.reScope(Util.loc - 1,Util.loc,arr1);
			}
			
			executeChild(root, 1, isReadVar);
			Util.loop2--;
			// 结束一层if嵌套
			int loc=Util.loc;
			if (root.getParent().getAttribute(ICodeKey.TokenType).toString() != "IF_ELSE_STATEMENT") {
				Util.isOut = true;
				// 将前面的test反向加入
				int no=Util.ifNo.get(Util.loop2).get(0);
				arrs=new ArrayList<ArrayList<Double>>();
				for(int i=0;i<Util.invariant.get(no).get(no+1).size();i++) {
					arr1 = new ArrayList<Double>();
					int n=1;
					if(i==0) n=-1;
					for(int k=0;k<Util.invariant.get(no).get(no+1).get(i).size();k++) {
						arr1.add(n*Util.invariant.get(no).get(no+1).get(i).get(k));
					}
					arrs.add(arr1);
					if(i==0)
						Util.comScope(no,Util.loc, arr1);
				}
				if(Util.lastLoc==Util.loc) {
					loc=1;
				}
				Util.invariant.get(no).put(loc, arrs);
				Util.copyScope2(no, loc);
			
				// 修改下标链接
				//Util.ifNo.get(Util.loop2).add(Util.loc);
				for (int i = 1; i < Util.ifNo.get(Util.loop2).size(); i++) {
					if(Util.lastLoc==Util.loc) {
						loc=1;
					}
					Util.invariant.get(Util.ifNo.get(Util.loop2).get(i) - 1).put(loc, Util.invariant
							.get(Util.ifNo.get(Util.loop2).get(i) - 1).get(Util.ifNo.get(Util.loop2).get(i)));
					Util.invariant.get(Util.ifNo.get(Util.loop2).get(i) - 1).remove(Util.ifNo.get(Util.loop2).get(i));
					Util.dpRSM.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.put(loc, Util.dpRSM
							.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.get(Util.ifNo.get(Util.loop2).get(i)));
					Util.dpRSM.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.remove(Util.ifNo.get(Util.loop2).get(i));
				}

				Util.previous = Util.ifNo.get(Util.loop2).get(0) - 1;
				Util.ifNo.remove(Util.loop2);
			}
		}
		return root;
	}

}
