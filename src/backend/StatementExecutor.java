package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import backend.Util.R;
import backend.Util.Sym;
import backend.Util.pathInfo;
import frontend.CGrammarInitializer;

public class StatementExecutor extends BaseExecutor {
	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {
		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		ICodeNode node;
		Util.Sym temp = new Sym();
		pathInfo p = new pathInfo();

			switch (production) {
			case CGrammarInitializer.While_LP_Test_RP_TO_Statement:
				boolean flag = true;
				while (isLoopContinute(root, path)) {
					if (flag) {
						Util.loop++;
						flag = false;
					}
					Util.testState.add(Util.loop-1,true);
					executeChild(root, 1, path);
					Util.testState.remove(Util.loop-1);
					for(int i=Util.testPath.size()-1;i>=0;i--) {
						if(Util.nestCount.size()>Util.testPath.get(i)&&Util.nestCount.get(Util.testPath.get(i)).isFlag()) {
							Util.nestCount.get(Util.testPath.get(i)).setFlag(false);
							break;
						}
					}
				}
				if (flag) {
					Util.exeValid = false;
					Util.loop++;
					Util.testState.add(Util.loop-1,false);
					executeChild(root, 1, path);
					Util.exeValid = true;
				}
				Util.testNo=0;
				Util.pathInfoMap.get(root.getChildren().get(0).getNo()).sym.ws
						.addAll(Util.liveTemp.get(--Util.loop).live);
				Util.liveTemp.remove(Util.loop);
				break;

			case CGrammarInitializer.Return_Expr_Semi_TO_Statement:
				node = executeChild(root, 0, path);
				Object obj = node.getAttribute(ICodeKey.VALUE);
				setReturnObj(obj);
				isContinueExecution(false);
				path.add(root.getNo());
				p.varList = Util.varList;
				Util.copySym(Util.lives, temp.live);
				Util.copySym(Util.wss, temp.ws);
				Util.pathInfoMap.put(root.getNo(), p);

				for (int i = 0; i < Util.loop; i++) {
					if (Util.liveTemp.size() <= i) {
						Util.liveTemp.add(temp);
					} else {
						Util.liveTemp.get(i).live.addAll(temp.live);
					}
				}
				Util.lives.clear();
				Util.wss.clear();
				break;

			case CGrammarInitializer.Expr_Semi_TO_Statement:
				executeChildren(root, path);
				if (Util.exeValid) {
					path.add(root.getNo());
					for(int i=0;i<Util.nestCount.size();i++) {
						if(Util.nestCount.get(i).isFlag()) {
							Util.nestCount.get(i).addCount();
							//Util.nestCount.get(i).setCount(0,Util.nestCount.get(i).getCount().get(0)+1);
						}
					}
				}
				
				//因为while中的constrain与var不能覆盖
				if(Util.pathInfoMap.containsKey(root.getNo())) {
					Util.pathInfoMap.get(root.getNo()).getVarList().addAll(Util.varList);
				}else {
					p.varList.addAll(Util.varList);
					Util.copySym(Util.lives, temp.live);
					Util.copySym(Util.wss, temp.ws);
					Util.copySym(Util.lives, p.sym.live);
					Util.copySym(Util.wss, p.sym.ws);
					Util.pathInfoMap.put(root.getNo(), p);
				}

				for (int i = 0; i < Util.loop; i++) {
					if (Util.liveTemp.size() <= i) {
						Util.liveTemp.add(i, temp);
					} else {
						Util.liveTemp.get(i).live.addAll(temp.live);
					}
				}
				Util.varList.clear();
				Util.lives.clear();
				Util.wss.clear();
				break;

			default:
				executeChildren(root, path);
				break;
			}

		return root;
	}

	private boolean isLoopContinute(ICodeNode root, ArrayList<Integer> path) {
		ICodeNode res = null;
		res = executeChild(root, 0, path);
		int result =0;
		if (res.getAttribute(ICodeKey.VALUE) != null) {
			result = (Integer) res.getAttribute(ICodeKey.VALUE);
		}
		return res != null && result != 0;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
		int production = (int) root.getAttribute(ICodeKey.PRODUCTION);
		ArrayList<ArrayList<Double>> arr;
		HashMap<Integer,ArrayList<ArrayList<Double>>> haa=new HashMap<Integer, ArrayList<ArrayList<Double>>>();
			switch (production) {
			case CGrammarInitializer.While_LP_Test_RP_TO_Statement:
				if(isReadVar) {
					Util.isWhile=true;
					executeChild(root, 0,isReadVar);
					executeChild(root, 1,isReadVar);
					Util.lastLoc=Util.loc+1;
				}else {
					Util.whileNo.push(Util.loc);
					executeChild(root, 0,isReadVar);
					executeChild(root, 1,isReadVar);
					Util.previous=Util.whileNo.pop();
					arr=Util.invariant.get(Util.loc-1).get(Util.loc);
					haa.put(Util.previous, arr);
					Util.invariant.put(Util.loc-1, haa);
					Util.invariant.get(Util.loc-1).remove(Util.loc);
					Util.dpRSM.get(Util.loc-1).vars.put(Util.previous, Util.dpRSM.get(Util.loc-1).vars.get(Util.loc));
					Util.dpRSM.get(Util.loc-1).vars.remove(Util.loc);
					Util.isOut=true;
					//0.handleOut
					//1.while到while_out
					//2.while_out加入-test
					//3.if(isWhile)
					Util.putOutWhile(Util.previous,Util.previous,Util.loc);
					Util.isWhile=true;					
				}
				break;

			case CGrammarInitializer.Expr_Semi_TO_Statement:
				executeChildren(root,isReadVar);
				break;

			default:
				executeChildren(root,isReadVar);
				break;
			}

		return root;
	}
}
