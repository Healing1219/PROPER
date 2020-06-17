package backend;

import java.util.ArrayList;

public class StatementListExecutor extends BaseExecutor{
	 @Override 
	 public Object Execute(ICodeNode root,ArrayList<Integer> path) {
	    	executeChildren(root,path);
	    
	    	return root;
	    }
	 
	 @Override 
	 public Object Execute(ICodeNode root,boolean isReadVar) {
	    	executeChildren(root,isReadVar);    
	    	return root;
	    }
}
