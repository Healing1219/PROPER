package backend;

import java.util.ArrayList;

import backend.Util.nestInfo;
import backend.Util.pathInfo;

public class TestExecutor extends BaseExecutor {

	@Override
	public Object Execute(ICodeNode root, ArrayList<Integer> path) {

		executeChildren(root, path);
		copyChild(root, root.getChildren().get(0));
		nestInfo n;
		if (root.getParent().getAttribute(ICodeKey.TokenType).equals(frontend.CTokenType.IF_STATEMENT)) {
			Util.loop++;
			Util.testNo++;
		}
		
		while(Util.nestCount.size()<root.getNo()) {
			n=new nestInfo();
			n.count.add(0, 0);
			Util.nestCount.add(n);
		}
		if (Util.exeValid) {
			if(Util.nestCount.size()>root.getNo()) {
				Util.nestCount.get(root.getNo()).count.add(0,0);
				Util.nestCount.get(root.getNo()).setFlag(true);
			}else {
				n=new nestInfo();
				n.count.add(0, 0);
				n.setFlag(true);
				Util.nestCount.add(root.getNo(), n);
			}
		}

		Util.testPath.add(root.getNo());
		
		if (Util.exeValid) {
			path.add(root.getNo());
			for(int i=0;i<Util.nestCount.size();i++) {
				if(Util.nestCount.get(i).isFlag()&&i!=root.getNo()) {
					Util.nestCount.get(i).addCount();
					//Util.nestCount.get(i).setCount(0,Util.nestCount.get(i).getCount()+1);
				}
			}
		}
		
		//因为while中的constrain与var不能覆盖
		if(Util.pathInfoMap.containsKey(root.getNo())) {
			Util.pathInfoMap.get(root.getNo()).getConstrainList().addAll(Util.constrainList);
			Util.pathInfoMap.get(root.getNo()).getVarList().addAll(Util.varList);
		}else {
			pathInfo p = new pathInfo();
			p.varList.addAll(Util.varList);
			p.constrainList.addAll(Util.constrainList);
			Util.Sym temp = new Util.Sym();
			Util.copySym(Util.lives, temp.live);
			Util.copySym(Util.wss, temp.ws);
			p.sym=temp;
			temp.isIf = true;
			Util.pathInfoMap.put(root.getNo(), p);
		}
		Util.varList.clear();
		Util.constrainList.clear();
		Util.lives.clear();
		Util.wss.clear();
		return root;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
		if(isReadVar) {
			executeChildren(root,isReadVar);
			if(Util.isWhile) {
				Util.loc++;
			}
		}else {
			Util.locType=1;//条件语句
			executeChildren(root,isReadVar);
			copyChild(root, root.getChildren().get(0));
		}
		return root;
	}

}
