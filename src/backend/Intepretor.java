package backend;

import java.util.ArrayList;

public class Intepretor implements Executor{

	private static Intepretor intepretor = null;
	public static Intepretor getIntepretor() {
		if (intepretor == null) {
			intepretor = new Intepretor();
		}
		
		return intepretor;
	}
	
	private Intepretor() {
		
	}
	
	@Override
	public Object Execute(ICodeNode root,ArrayList<Integer> path) {
		if (root == null) {
			return null;
		}
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		Executor executor = factory.getExecutor(root);
		executor.Execute(root,path);
		
		return root;
	}
	
	@Override
	public Object Execute(ICodeNode root,boolean isReadVar) {
		if (root == null) {
			return null;
		}
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		Executor executor = factory.getExecutor(root);
		executor.Execute(root,isReadVar);
		
		return root;
	}
}
