package inference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Box implements Serializable{

	private int nVars;
	private boolean hasLB[];
	private double lb[];
	private boolean hasUB[];
	private double ub[];
	List<RVar> varData=new ArrayList<RVar>();
	
	public Box(List<RVar> varData){
		this.varData=varData;
		nVars=varData.size();
		hasLB=new boolean[varData.size()];
		hasUB=new boolean[varData.size()];
		lb=new double[varData.size()];
		ub=new double[varData.size()];
		
		for(int i=0;i<nVars;i++) {
			if(varData.get(i).hasLowerBound()) {
				hasLB[i]=true;
				lb[i]=varData.get(i).getLowerBound();
			}else {
				hasLB[i]=false;
			}
			
			if(varData.get(i).hasUpperBound()) {
				hasUB[i]=true;
				ub[i]=varData.get(i).getUpperBound();
			}else {
				hasUB[i]=false;
			}
		}
	}
	
	void roundDimension(int dim) {
		if(hasLB[dim]){
			lb[dim]=Math.ceil(lb[dim]);
		}
		if(hasUB[dim]){
			ub[dim]=Math.floor(ub[dim]);
		}
	}
	
	boolean checkBounds(int dim) {
		if(hasLB[dim]&&hasUB[dim]) {
			//assert(lb[dim]<=ub[dim]);
			if(lb[dim]<=ub[dim]) {
				return false;
			}
		}
		return true;
	}
	
	boolean hasLowerBound(int dim) {
		assert(dim>=0);
		assert(dim<nVars);
		return hasLB[dim];
	}
	
	boolean hasUpperBound(int dim) {
		assert(dim>=0);
		assert(dim<nVars);
		return hasUB[dim];
	}
	
	boolean hasBothBound(int dim) {
		assert(dim>=0);
		assert(dim<nVars);
		return hasUB[dim]&&hasLB[dim];
	}
	
	 double lowerBound(int dim){
	    assert(dim >= 0);
	    assert(dim < nVars);
	    assert(hasLB[dim]);
	    return lb[dim];
	  }
	 
	 double upperBound(int dim){
	    assert(dim >= 0);
	    assert(dim < nVars);
	    assert(hasUB[dim]);
	    return ub[dim];
	  }
	 
	 void setUpperBound(int dim, double v)
	  {
	    assert(dim >= 0);
	    assert(dim < nVars);
	    if (varData.get(dim) .isIntegerVariable())
	      v = Math.floor(v);
	    if (!hasUB[dim] || ub[dim] >= v)
	    {
	      hasUB[dim] = true;
	      ub[dim] = v;
	    }
	    checkBounds(dim);
	  }

	  void setLowerBound(int dim,double v)
	  {
	    assert(dim >= 0);
	    assert(dim < nVars);
	    if (varData.get(dim).isIntegerVariable())
	      v = Math.ceil(v);
	    if (!hasLB[dim] || lb[dim] <= v)
	    {
	      hasLB[dim] = true;
	      lb[dim] = v;
	    }
	    checkBounds(dim);
	  }
	  
	  void unsetLowerBound(int dim)
	  {
	    assert(dim >= 0);
	    assert(dim < nVars);
	    hasLB[dim] = false;
	  }

	  void unsetUpperBound(int dim)
	  {
	    assert(dim >= 0);
	    assert(dim < nVars);
	    hasUB[dim] = false;
	  }
}
