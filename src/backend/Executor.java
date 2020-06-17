package backend;

import java.util.ArrayList;

public interface Executor {
    public Object Execute(ICodeNode root,ArrayList<Integer> path);
    
    public Object Execute(ICodeNode root,boolean isReadVar);
}
