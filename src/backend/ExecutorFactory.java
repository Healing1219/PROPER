package backend;

import frontend.CTokenType;

public class ExecutorFactory {
    private static ExecutorFactory executorFactory = null;
    private ExecutorFactory() {
    	
    }
    
    public static ExecutorFactory getExecutorFactory() {
    	if (executorFactory == null) {
    		executorFactory = new ExecutorFactory();
    	}
    	
    	return executorFactory;
    }
    
    public Executor getExecutor(ICodeNode node) {
    	
    	if (node == null) {
    		return null;
    	}
    	
    	CTokenType type = (CTokenType)node.getAttribute(ICodeKey.TokenType);
    	//System.out.println(type);
    	switch (type) {
    	case UNARY:
    		return new UnaryNodeExecutor();
    	case BINARY:
    		return new BinaryExecutor();
    	case NO_COMMA_EXPR:
    		return new NoCommaExprExecutor();
    	case EXPR:
    		return new ExprExecutor();
    	case STATEMENT:
    		return new StatementExecutor();
    	case STMT_LIST:
    		return new StatementListExecutor();
    	case TEST:
    		return new TestExecutor();
    	case IF_STATEMENT:
    		return new IfStatementExecutor();
    	case IF_ELSE_STATEMENT:
    		return new ElseStatementExecutor();
    	case FUNCT_DECL:
    		return new FunctDeclExecutor();
    	case EXT_DEF:
    		return new ExtDefExecutor();
    	case ARGS:
    		return new ArgsExecutor();
    	case RELOP:
    		if(Util.exeValid) {
    			Util.flag=1;
    		}
    		return null;
    	case EQUAL:
    		Util.isAssign=1;
    		return null;
    	}
    	
    	return null;
    }
}
