package inference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import backend.Util;
import inference.Union.dset_t;

public class Ineq implements Serializable{

	public double coeffs[];
	public double c;
	public boolean isRedundant;

	public Ineq(int n) {
		coeffs = new double[n];
		c = -1;
		isRedundant = false;
	}

	public double consCoeff() {
		return c;
	}

	public void setConstant(double x) {
		assert (!isRedundant);
		c = x;
	}

	public int nVars() {
		if (isRedundant)
			return 0;
		int count = 0;
		for (int i = 0; i < coeffs.length; i++) {
			if (coeffs[i] > Util.EPS || coeffs[i] < -1.0 * Util.EPS) {
				count++;
			}
		}
		return count;
	}

	public boolean isSatisfied(double[] p) {
		if (isRedundant)
			return true;
		assert (p.length == coeffs.length);
		double r = 0.0;
		for (int i = 0; i < p.length; ++i) {
			r += coeffs[i] * p[i];
		}
		return (r <= c);
	}

	public boolean boxSatisfies(Box bx) {
		if (isRedundant)
			return true;

		double b = 0.0;
		for (int i = 0; i < coeffs.length; i++) {
			if (coeffs[i] < 0.0) {
				if (!bx.hasLowerBound(i))
					return false;
				b = b + bx.lowerBound(i) * coeffs[i];
			}
			if (coeffs[i] > 0.0) {
				if (!bx.hasUpperBound(i))
					return false;
				b = b + bx.upperBound(i) * coeffs[i];
			}
		}
		return (b <= c);
	}

	public boolean checkRedundant(Box bx) {
		if(isRedundant)
			return true;
		if (boxSatisfies(bx)) {
			isRedundant = true;
		}
		return isRedundant;
	}

	public boolean isBoxConstraint(double[] dim, boolean[] bound) {
		if (isRedundant)
			return false;
		int count = 0;
		int i;
		bound[0] = bound[1] = false;// isLB = isUB = false;
		int nVars = coeffs.length;
		for (i = 0; i < nVars; ++i) {
			if (coeffs[i] > 0.0) {
				if (count == 0) {
					dim[0] = i;
					assert (!bound[0] && !bound[1]);
					bound[1] = true;
					dim[2] = c / coeffs[i];
					count = 1;
				} else {
					return false;
				}
				count++;
			} else if (coeffs[i] < 0.0) {
				if (count == 0) {
					dim[0] = i;
					assert (!bound[0] && !bound[1]);
					bound[0] = true;
					dim[1] = c / coeffs[i];
				} else {
					return false;
				}
				count++;
			}
		}
		return true;
	}

	// 无关0, subsumes 1,isSubsumed 2
	public int subsumesOrIsSubsumed(Ineq in2, double[] scale) {
		// Check if this subsumes in2 or vice-versa.
		scale[0] = 0.0;
		int i = 0;
		for (i = 0; i < coeffs.length && coeffs[i] == 0.0 && in2.coeffs[i] == 0.0; ++i) {
		}
		if (i < coeffs.length) {
			if (coeffs[i] == 0.0 || in2.coeffs[i] == 0.0)
				return 0;
			scale[0] = coeffs[i] / in2.coeffs[i];
			if (scale[0] < 0.0)
				return 0;
			i++;
			for (; i < coeffs.length; ++i) {
				if (coeffs[i] != scale[0] * in2.coeffs[i])
					return 0;
			}

			double c1 = this.c;
			double c2 = scale[0] * in2.c;
			if (c1 <= c2) {
				// subsumes = true;
				return 1;// subsumes
			}
			if (c2 <= c1) {
				// isSubsumed = true;
				return 2;// isSubsumed
			}
		}

		return 0;
	}

	public void evaluateAgainstBox(boolean[] fLB, boolean[] fUB, double[] lb, double[] ub, int dim, boolean[] found,
			double[] bound) {
		assert (dim >= 0);
		assert (dim < coeffs.length);

		if (isRedundant)
			return;

		if (coeffs[dim] == 0.0)
			return;
		double b = 0.0;
		for (int i = 0; i < coeffs.length; ++i) {
			if (i == dim || coeffs[i] == 0.0)
				continue;
			if (coeffs[i] < 0.0) {
				assert (fUB[i]);
				b = b + coeffs[i] * lb[i];
			} else {
				assert (fLB[i]);
				b = b + coeffs[i] * ub[i];
			}
		}

		b = (c - b) / coeffs[dim];
		if (coeffs[dim] > 0.0) {
			if (!found[1]) {// foundUB
				found[1] = true;
				bound[1] = b;// uNew
			} else {
				if (b <= bound[1])
					bound[1] = b;
			}
		} else {
			if (!found[0]) {
				found[0] = true;
				bound[0] = b;
			} else {
				if (b >= bound[0])
					bound[0] = b;
			}
		}
	}

	public boolean unionVars(dset_t cl) {
		boolean retVal = true;
		Union union = new Union();
		if (isRedundant)
			return retVal;
		int firstNonZero = -1;
		for (int i = 0; i < coeffs.length; ++i) {
			if (coeffs[i] > Util.EPS || coeffs[i] < -1 * Util.EPS) {
				if (firstNonZero < 0)
					firstNonZero = i;
				else {
					boolean ch = union.doUnion(cl, firstNonZero, i);
					if (ch)
						retVal = false;
				}
			}
		}
		return retVal;
	}

	public boolean compressBox(Box bx, int dim, boolean[] hasBound, double[] bound) {
		assert (dim >= 0);
		assert (dim < coeffs.length);
		if (isRedundant)
			return false;
		if (coeffs[dim] == 0.0)
			return false;

		double b = 0.0;
		for (int i = 0; i < coeffs.length; ++i) {
			if (i == dim)
				continue;
			if (coeffs[i] < 0.0) {
				if (bx.hasUpperBound(i)) {
					b = b + coeffs[i] * bx.upperBound(i);
				} else {
					return false;
				}
			} else if (coeffs[i] > 0.0) {
				if (bx.hasLowerBound(i)) {
					b = b + coeffs[i] * bx.lowerBound(i);
				} else {
					return false;
				}
			}
		}
		
		b=(c-b)/coeffs[dim];
		boolean change=false;
		if(coeffs[dim]>0.0) {
			if(!hasBound[0]) {//hasUB
				hasBound[0]=true;
				bound[0]=b;//ub
				change=true;
			}else {
				if(bound[0]>b) {
					bound[0]=b;
					change=true;
				}
			}
		}else {
			assert(coeffs[dim]<0.0);
			if(!hasBound[1]) {//haslb
				hasBound[1]=true;
				bound[1]=b;
				change=true;
			}else {
				if(bound[1]<b) {
					bound[1]=b;
					change=true;
				}
			}
		}
		return change;
	}

	public boolean hasVarInCommon(HashSet<Integer> s) {
		if (isRedundant)
			return false;
		Iterator<Integer> it = s.iterator();
		while (it.hasNext()) {
			int i = it.next();
			if (coeffs[i] > Util.EPS || coeffs[i] < -1 * Util.EPS) {
				return true;
			}
		}
		return false;
	}

	public void collectVarOccurrenceCounts(HashMap<Integer, Integer> oMap) {
		if (isRedundant)
			return;
		for (int i = 0; i < coeffs.length; ++i) {
			if (coeffs[i] > Util.EPS || coeffs[i] < -1 * Util.EPS)
				if (oMap.containsKey(i)) {
					oMap.put(i, oMap.get(i) + 1);
				} else {
					oMap.put(i, 1);
				}
		}
	}

}
