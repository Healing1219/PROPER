package backend;

import java.util.ArrayList;

public interface ICodeNode {
	
	public boolean hasNextBrother();
   
    public ICodeNode  getParent();
    
    public ICodeNode addChild(ICodeNode node);
    
    public ArrayList<ICodeNode> getChildren();
    
    public void setAttribute(ICodeKey key, Object value);
    
    public Object  getAttribute(ICodeKey key);
    
    public ICodeNode copy();
    
    public boolean isChildrenReverse();
    
    public void reverseChildren();

	public void setNo(int i);
	
	public int getNo();
}

