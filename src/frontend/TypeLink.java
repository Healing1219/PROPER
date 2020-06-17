package frontend;

public class TypeLink {
	
    boolean  isDeclarator = true; //true 那么该object 的对象是declarator, false那么object指向的就是specifier
    
    Object   typeObject;
    
    private TypeLink  next = null;
    
    public TypeLink(boolean isDeclarator, Object typeObj) {
    	this.isDeclarator = isDeclarator;
    	this.typeObject = typeObj;
    }
    
    public Object getTypeObject() {
    	return typeObject;
    }
    
    public TypeLink toNext() {
    	return next;
    }
    
    public void setNextLink(TypeLink obj) {
    	this.next = obj;
    }
}
