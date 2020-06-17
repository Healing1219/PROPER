package backend;

import java.util.ArrayList;

public abstract class BaseExecutor implements Executor {
	private static boolean continueExecute = true;
	private static Object returnObj = null;

	protected void setReturnObj(Object obj) {
		this.returnObj = obj;
	}

	protected Object getReturnObj() {
		return returnObj;
	}

	protected void clearReturnObj() {
		this.returnObj = null;
	}

	protected void isContinueExecution(boolean execute) {
		this.continueExecute = execute;
	}

	protected void executeChildren(ICodeNode root,ArrayList<Integer> path) {
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		root.reverseChildren();

		int i = 0;
		while (i < root.getChildren().size()) {

			if (continueExecute != true) {
				break;
			}

			ICodeNode child = root.getChildren().get(i);
			child.setAttribute(ICodeKey.VALUE, null);
			Executor executor = factory.getExecutor(child);
			if (executor != null) {
				executor.Execute(child,path);
			} else {
				//System.err.println("Not suitable Executor found, node is: " + child.toString());
			}

			i++;
		}
	}
	
	protected ICodeNode executeChild(ICodeNode root, int childIdx,ArrayList<Integer> path) {
		// 把孩子链表的倒转放入到节点本身，减少逻辑耦合性
		root.reverseChildren();
		ICodeNode child;
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		child = (ICodeNode) root.getChildren().get(childIdx);
		Executor executor = factory.getExecutor(child);
		ICodeNode res = (ICodeNode) executor.Execute(child,path);

		return res;
	}

	protected void copyChild(ICodeNode root, ICodeNode child) {
		root.setAttribute(ICodeKey.SYMBOL, child.getAttribute(ICodeKey.SYMBOL));
		root.setAttribute(ICodeKey.VALUE, child.getAttribute(ICodeKey.VALUE));
		root.setAttribute(ICodeKey.TEXT, child.getAttribute(ICodeKey.TEXT));
		//root.setAttribute(ICodeKey.PROB, child.getAttribute(ICodeKey.PROB));
	}

	protected void executeChildren(ICodeNode root,boolean isReadVar) {
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		root.reverseChildren();

		int i = 0;
		while (i < root.getChildren().size()) {

			if (continueExecute != true) {
				break;
			}

			ICodeNode child = root.getChildren().get(i);
			child.setAttribute(ICodeKey.VALUE, null);
			Executor executor = factory.getExecutor(child);
			if (executor != null) {
				executor.Execute(child,isReadVar);
			} else {
				//System.err.println("Not suitable Executor found, node is: " + child.toString());
			}
			i++;
		}
	}
	
	protected ICodeNode executeChild(ICodeNode root, int childIdx,boolean isReadVar) {
		// 把孩子链表的倒转放入到节点本身，减少逻辑耦合性
		root.reverseChildren();
		ICodeNode child;
		ExecutorFactory factory = ExecutorFactory.getExecutorFactory();
		child = (ICodeNode) root.getChildren().get(childIdx);
		Executor executor = factory.getExecutor(child);
		ICodeNode res = (ICodeNode) executor.Execute(child, isReadVar);

		return res;
	}
}
