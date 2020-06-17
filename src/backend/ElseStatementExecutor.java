package backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ElseStatementExecutor extends BaseExecutor {

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {

		int testNo;
		Integer val;
		// 先执行if 部分
		ICodeNode res = executeChild(root, 0, path);
		String s = "1";
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

		if (val == 0) {
			Util.exeValid = true;
			// 最后一个没有if的else,嵌套数loop+1
			if (root.getChildren().get(1).getAttribute(ICodeKey.TokenType).toString() != "IF_STATEMENT") {
				Util.loop++;
				Util.testState.add(Util.loop - 1, true);
			}
			// if 部分没有执行，所以执行else部分
			res = executeChild(root, 1, path);

			// 执行到最后一个没有if的else
			if (root.getChildren().get(1).getAttribute(ICodeKey.TokenType).toString() != "IF_STATEMENT") {
				Util.loop--;
				Util.testState.remove(Util.loop);
				for (int i = 1; i <= Util.testNo; i++) {
					testNo = Util.testPath.get(Util.testPath.size() - i);
					Util.pathInfoMap.get(testNo).sym.ws.addAll(Util.liveTemp.get(Util.loop).live);
				}
				Util.liveTemp.remove(Util.loop);
			}
		} else {
			Util.exeValid = false;
			// if 部分没有执行，所以执行else部分
			if (root.getChildren().get(1).getAttribute(ICodeKey.TokenType).toString() != "IF_STATEMENT") {
				Util.loop++;
				Util.testState.add(Util.loop - 1, false);
			}
			res = executeChild(root, 1, path);

			// 执行到最后一个没有if的else
			if (root.getChildren().get(1).getAttribute(ICodeKey.TokenType).toString() != "IF_STATEMENT") {
				Util.loop--;
				Util.testState.remove(Util.loop);
				for (int i = 1; i <= Util.testNo; i++) {
					testNo = Util.testPath.get(Util.testPath.size() - i);
					Util.pathInfoMap.get(testNo).sym.ws.addAll(Util.liveTemp.get(Util.loop).live);
				}
				Util.liveTemp.remove(Util.loop);
			}
		}

		// 结束一层if_else嵌套
		if (root.getParent().getAttribute(ICodeKey.TokenType).toString() == "STMT_LIST") {
			Util.testNo = 0;
			Util.exeValid = true;
			if (Util.testState.contains(false)) {
				Util.exeValid = false;
			}
			// if_else中的语句数
			int count = 0;
			for (int i = 0; i < Util.testPath.size(); i++) {
				if (Util.nestCount.size() > Util.testPath.get(i) && Util.nestCount.get(Util.testPath.get(i)).isFlag()) {
					count++;
					if (count > Util.loop) {
						Util.nestCount.get(Util.testPath.get(i)).setFlag(false);
					}
				}
			}
		}
		copyChild(root, res);

		return root;
	}

	@Override
	public Object Execute(ICodeNode root, boolean isReadVar) {
		if (isReadVar) {
			executeChild(root, 0, isReadVar);
			executeChild(root, 1, isReadVar);
		} else {
			ArrayList<Double> arr1;
			ArrayList<ArrayList<Double>> arrs;
			// 先执行if 部分
			ICodeNode res = executeChild(root, 0, isReadVar);
			// 最后一个没有if的else
			if (root.getChildren().get(1).getAttribute(ICodeKey.TokenType).toString() != "IF_STATEMENT") {
				Util.previous = Util.ifNo.get(Util.loop2).get(0)-1;
				Util.ifNo.get(Util.loop2).add(Util.loc);// else
				Util.loop2++;
			}
			// else
			res = executeChild(root, 1, isReadVar);
			copyChild(root, res);
			// 结束一层if_else嵌套
			if (root.getParent().getAttribute(ICodeKey.TokenType).toString() == "STMT_LIST") {
				Util.isOut = true;
				Util.loop2--;
				// 将前面的test反向加入
				for(int j=0;j < Util.ifNo.get(Util.loop2).size() - 1; j++) {
					int no=Util.ifNo.get(Util.loop2).get(j);
					if(j==Util.ifNo.get(Util.loop2).size() - 2) {
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
				int loc=Util.loc;
				//Util.ifNo.get(Util.loop2).add(Util.loc);
				// 修改下标链接
				for (int i = 1; i <Util.ifNo.get(Util.loop2).size(); i++) {
					if(Util.lastLoc==Util.loc)
						loc=1;
					Util.invariant.get(Util.ifNo.get(Util.loop2).get(i) - 1).put(loc, Util.invariant
							.get(Util.ifNo.get(Util.loop2).get(i) - 1).get(Util.ifNo.get(Util.loop2).get(i)));
					Util.invariant.get(Util.ifNo.get(Util.loop2).get(i) - 1).remove(Util.ifNo.get(Util.loop2).get(i));
					Util.dpRSM.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.put(loc, Util.dpRSM
							.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.get(Util.ifNo.get(Util.loop2).get(i)));
					Util.dpRSM.get(Util.ifNo.get(Util.loop2).get(i) - 1).vars.remove(Util.ifNo.get(Util.loop2).get(i));
				}

				Util.previous = Util.ifNo.get(Util.loop2).get(0) - 1;

//			for(int i=0;i<Util.ifTest.size();i++) {
//				if(Util.ifTest.get(i).get(0)-Util.previous-1==0.0) {
//					Util.ifTest.remove(i);
//				}
//			}

				Util.ifNo.remove(Util.loop2);

			}
		}
		return root;
	}

}
