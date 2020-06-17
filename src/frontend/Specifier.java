package frontend;

public class Specifier {
	//type
	public static int  NONE = -1;
    public static int  INT = 0;
    public static int  REAL = 1;
    public static int  VOID = 2;
    public static int  BOOL = 3;

    //storage
    public static int  FIXED = 0;
    public static int  AUTO = 1;
    public static int  CONSTANT = 2;
    
    public static int  NO_OCLASS = 0;  //如果内存类型是auto, 那么存储类型就是NO_OCLASS
    public static int  PUBLIC = 1;
    public static int  PRIVATE = 2;
    public static int  COMMON = 3;
    
    private  int  basicType;
    public   void setType(int type) {
    	basicType = type;
    }
    public int getType() {
    	return basicType;
    }
    
    private int storageClass;
    public  void setStorageClass(int s) {
    	storageClass = s;
    }
    public int getStorageClass() {
    	return storageClass;
    }
    
    private int outputClass= NO_OCLASS;
    public void setOutputClass(int c) {
    	outputClass = c;
    }
    public int getOutputClass() {
    	return outputClass;
    }
    
    private int  constantValue = 0;
    public void setConstantVal(int v) {
    	constantValue = v;
    }
    public int getConstantVal() {
    	return constantValue;
    }

}
