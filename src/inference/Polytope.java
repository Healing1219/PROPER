package inference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import backend.Util;
import gui.MainController;
import inference.Union.dset_t;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Polytope implements Serializable {
	public int nVars;
	public Box bx;
	public ArrayList<Ineq> ineqs;
	public boolean isEmpty;

//	public class Fraction {
//		public int nVar;
//		public long time;
//		public BigDecimal[] bd;
//	}
//
//	public static ArrayList<Fraction> frac = new ArrayList<Fraction>();

	public Polytope() {

	}

	public Polytope clone() {
		Polytope pCopy = null;
		try {
			// 写入字节流
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			pCopy = (Polytope) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return pCopy;
	}

	public void setPoly(Box bx) {
		ineqs = new ArrayList<Ineq>();
		isEmpty = false;
		nVars = Util.rVar.size();
		this.bx = bx;
	}

	public void setUpperBound(int dim, double u) {
		bx.setUpperBound(dim, u);
	}

	public void setLowerBound(int dim, double l) {
		bx.setLowerBound(dim, l);
	}

	public void unsetUpperBound(int dim) {
		bx.unsetUpperBound(dim);
	}

	public void unsetLowerBound(int dim) {
		bx.unsetLowerBound(dim);
	}

	public boolean checkBounds(int dim) {
		return bx.checkBounds(dim);
	}

	public boolean isABox() {
		for (int i = 0; i < ineqs.size(); i++) {
			if (!ineqs.get(i).boxSatisfies(bx))
				return false;
		}
		return true;
	}

	public void addInequality(Ineq e) {
		double[] dim = new double[3];// dim l u
		boolean[] bound = new boolean[2];// isLB isUB
		if (e.nVars() <= 0) {
			if (e.consCoeff() < 0.0) {
				if(Util.debug) {
					System.err.println("Encountered 0 < 1 constraint!!");
				}
				isEmpty = true;
			}
			return;
		}

		if (e.isBoxConstraint(dim, bound)) {
			if (bound[0]) {
				if(Util.debug) {
					System.out.println("constraint converted to lower bound " + dim[1]);
				}
				if (bx.hasUpperBound((int) dim[0])) {
					double u1 = bx.upperBound((int) dim[0]);
					if (u1 < dim[1]) {
						System.out.println("Conflict detected.");
						isEmpty = true;
						return;
					}
				}
				bx.setLowerBound((int) dim[0], dim[1]);

			} else {
				assert (bound[1]);
				System.out.println("constraint converted to upper bound " + dim[2]);
				if (bx.hasLowerBound((int) dim[0])) {
					double l1 = bx.lowerBound((int) dim[0]);
					if (l1 > dim[2]) {
						System.out.println("Conflict detected.");
						isEmpty = true;
						return;
					}
				}
				bx.setUpperBound((int) dim[0], dim[2]);
			}
			return;
		}

		if (Util.redundancyEliminate) {
			for (int i = 0; i < ineqs.size(); i++) {
				double[] scale = new double[1];
				int flag = ineqs.get(i).subsumesOrIsSubsumed(e, scale);
				if (flag != 0) {
					System.out.println("Detected redundancy in input.");
					if (flag == 1)
						return;
					if (flag == 2) {
						ineqs.get(i).c = scale[0] * e.consCoeff();
						return;
					}
				}
			}
		}
		ineqs.add(e);
	}

	public int pruneRedundantConstraints() {
		int count = 0;
		for (int i = 0; i < ineqs.size(); i++) {
			if (!ineqs.get(i).checkRedundant(bx)) {
				count++;
			}
		}
		if (Util.debug) {
			System.out.println("# Irredundant: " + count);
		}
		return count;
	}

	public void boxCheckEmpty() {
		for (int i = 0; i < nVars; i++) {
			if (bx.hasBothBound(i)) {
				if (bx.lowerBound(i) > bx.upperBound(i)) {
					isEmpty = true;
				}
			}
		}
	}

	public double solveSimplexAndGetResult(LpSolve solver, boolean[] bb) throws LpSolveException {
		int stat = 8;
		double optVal = -1234.56;
		try {
			stat = solver.solve();
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (stat == solver.OPTIMAL) {
			bb[0] = true;// feas
			bb[1] = true;// bnded
			optVal = solver.getObjective();
		} else if (stat == solver.UNBOUNDED) {
			bb[0] = true;// feas
			bb[1] = false;// bnded
		} else {
			bb[0] = false;// feas
		}
		return optVal;
	}

	public void computeBoundingBoxUsingLP() throws LpSolveException {
		assert (!isEmpty);
		int nRows = pruneRedundantConstraints(); // 减少多余约束
		if (nRows == 0)
			return;

		// Create a problem with constraints and variables
		LpSolve solver = LpSolve.makeLp(0, nVars);// rows,columns
		solver.setMaxim();
		// solver.printLp();
		for (int i = 0; i < nVars; i++) {
			if (bx.hasBothBound(i)) {
//				double l=bx.lowerBound(i);
//				double u=bx.upperBound(i);
//				if(l+Util.EPS<u-Util.EPS) {
				solver.setBounds(i + 1, bx.lowerBound(i), bx.upperBound(i));
//				}else {
//					//solver.setBoundsTighter(true);
//				}
			} else if (bx.hasLowerBound(i) && !bx.hasUpperBound(i)) {
				solver.setLowbo(i + 1, bx.lowerBound(i));
			} else if (!bx.hasLowerBound(i) && bx.hasUpperBound(i)) {
				solver.setUpbo(i + 1, bx.upperBound(i));
			} else {

			}
			// Clear all objective coefficients.
			solver.setObj(i + 1, 0.0);
		}

		// solver.printLp();
		int count = 0;
		for (int i = 0; i < ineqs.size(); i++) {
			if (ineqs.get(i).isRedundant)
				continue;
			// solver.addConstraint(ineqs.get(i).coeffs, solver.LE, ineqs.get(i).c);
			solver.strAddConstraint(Util.ArrayToString(ineqs.get(i).coeffs), solver.LE, ineqs.get(i).c);
			count++;
		}
		if (Util.debug) {
			solver.printLp();
		}

		assert (count == nRows);
		// Now we can compute upper and lower bounds for the variables one by one.
		for (int i = 0; i < nVars; i++) {
			// maximize
			solver.setObj(i + 1, 1.0);
			boolean[] result = new boolean[2];
			result[0] = result[1] = false;
			double optVal = solveSimplexAndGetResult(solver, result);
			if (!result[0]) {
				isEmpty = true;
				return;
			}
			if (!result[1]) {
				assert (!bx.hasUpperBound(i));
			} else {
				assert (result[0] && result[1]);
				bx.setUpperBound(i, optVal);
			}

			// minimize
			solver.setObj(i + 1, -1.0);
			result[0] = result[1] = false;
			optVal = solveSimplexAndGetResult(solver, result);
			assert (result[0]);
			if (!result[1]) {
				assert (!bx.hasLowerBound(i));
			} else {
				assert (result[0] && result[1]);
				bx.setLowerBound(i, -1.0 * optVal);
			}
			// cleanup
			solver.setObj(i + 1, 0.0);
		}
		solver.deleteLp();
		boxCheckEmpty();
	}

	public void computeBoundingBoxUsingICP() {
		if (isEmpty)
			return;
		pruneRedundantConstraints();
		int count = 0;
		boolean change = true;
		while (change && count < 10) {
			change = false;
			count++;
			for (int j = 0; j < nVars; ++j) {
				boolean[] hasBound = new boolean[2];
				hasBound[1] = bx.hasLowerBound(j);
				hasBound[0] = bx.hasUpperBound(j);
				double[] bound = new double[2];
				bound[1] = -100000;
				bound[0] = 100000;
				if (hasBound[1])
					bound[1] = bx.lowerBound(j);
				if (hasBound[0])
					bound[0] = bx.upperBound(j);
				if (hasBound[1] && hasBound[0])
					assert (bound[1] <= bound[0]);

				for (int i = 0; i < ineqs.size(); ++i) {
					boolean rVal = ineqs.get(i).compressBox(bx, j, hasBound, bound);
					if (rVal)
						change = true;
					if (hasBound[1] && hasBound[0] && bound[1] > bound[0]) {
						isEmpty = true;
						return;
					}
				}

				/*
				 * if(hasBound[1] && hasBound[0] && bound[1]>bound[0]) { isEmpty=true; return; }
				 */
				if (!bx.varData.get(j).isIntegerVariable() && hasBound[1] && hasBound[0] && bound[0] == bound[1]) {
					isEmpty = true;
					return;
				}

				if (hasBound[1]) {
					bx.setLowerBound(j, bound[1]);
				}
				if (hasBound[0]) {
					bx.setUpperBound(j, bound[0]);
				}
			}
		}
		boxCheckEmpty();
	}

	public double computeBoundingBoxProbability() {
		double p = 1;
		if (isEmpty)
			return 0.0;
		for (int i = 0; i < nVars; i++) {
			p *= Util.rVar.get(i).computeIntervalProbability(bx.hasLowerBound(i), bx.lowerBound(i), bx.hasUpperBound(i),
					bx.upperBound(i));
		}
		return p;
	}

	public BigDecimal computeBoundingBoxProbability1(BigDecimal p) {
		double r = 1;
		if (isEmpty) {
			return new BigDecimal("0.0");
		}
		p = new BigDecimal("1");
		for (int i = 0; i < nVars; i++) {
			r = Util.rVar.get(i).computeIntervalProbability(bx.hasLowerBound(i), bx.lowerBound(i), bx.hasUpperBound(i),
					bx.upperBound(i));
			BigDecimal q = new BigDecimal(r + "");
			p = p.multiply(q);
		}
		return p;
	}

	public boolean isSatisfied(double[] pt) {
		if (isEmpty)
			return false;
		for (int i = 0; i < ineqs.size(); i++) {
			if (!ineqs.get(i).isSatisfied(pt))
				return false;
		}
//		if (Util.debug)
//			System.out.println("SAT");
		return true;
	}

	public boolean generateAndCheckSample() {
		if (isEmpty)
			return false;
		double[] pt = new double[nVars];
//		if (Util.debug) {
//			System.out.print("S: ");
//		}
		for (int i = 0; i < nVars; i++) {
			pt[i] = Util.rVar.get(i).generateSample();
//			if (Util.debug) {
//				System.out.print(", " + pt[i]);
//			}
		}
//		if (Util.debug)
//			System.out.println();
		return isSatisfied(pt);
	}

	public boolean generateAndCheckSampleBB() {
		if (isEmpty)
			return false;
		double[] pt = new double[nVars];
//		if (Util.debug) {
//			System.out.print("S: ");
//		}
		for (int i = 0; i < nVars; i++) {
			pt[i] = Util.rVar.get(i).generateSampleInRange(bx.hasLowerBound(i), bx.lowerBound(i), bx.hasUpperBound(i),
					bx.upperBound(i));
//			if (Util.debug) {
//				System.out.print(", " + pt[i]);
//			}
		}
//		if (Util.debug)
//			System.out.println();
		return isSatisfied(pt);
	}

	public double computeProbability(int nTrials, boolean flag) {
		int count = 0;
		if (isEmpty)
			return 0.0;

		for (int i = 0; i < nTrials; ++i) {
			if (flag) {
				if (generateAndCheckSampleBB())
					count++;
			} else {
				if (generateAndCheckSample())
					count++;
			}
		}
		System.out.println(" # trials: " + nTrials + " and success : " + count);
		return count * 1.0 / nTrials;
	}

	public void mcEstimateProbability(boolean flag,BigDecimal[] estP) {
		if (flag) {
			try {
				computeBoundingBoxUsingLP();
			} catch (LpSolveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (isEmpty) {
				System.out.println("Warning: input polyhedron is empty!");
			}
			double pd = computeBoundingBoxProbability();
			System.out.println("Upper bound estimate (1Box):" + pd);
			double pr = computeProbability(Util.nTrials, flag);
			System.out.println("pBoxEst = " + pr * pd);
			estP[0]=estP[0].add(new BigDecimal(pr * pd+""));
		} else {
			double pr = computeProbability(Util.nTrials, flag);
			System.out.println("pSimpleEst = " + pr);
		}
	}

	public void expandBoxAlongDimension(boolean[] fLB, boolean[] fUB, double[] lb, double[] ub, int dim) {
		boolean[] found = new boolean[2];// foundLB,foundUB
		double[] bound = new double[2];// lNew,uNew
		found[0] = bx.hasLowerBound(dim);
		found[1] = bx.hasUpperBound(dim);
		bound[0] = bx.lowerBound(dim);
		bound[1] = bx.upperBound(dim);

		for (int i = 0; i < ineqs.size(); i++) {
			ineqs.get(i).evaluateAgainstBox(fLB, fUB, lb, ub, dim, found, bound);
		}
		if (found[0]) {
			fLB[dim] = true;
			lb[dim] = bound[0];
		}

		if (found[1]) {
			fUB[dim] = true;
			ub[dim] = bound[1];
		}
	}

	public BigDecimal innerApproximationFromSamplePoint(double[] pt, BigDecimal p) {
		p = new BigDecimal("1.0");
		boolean[] fLB = new boolean[nVars];
		boolean[] fUB = new boolean[nVars];
		double[] lb = new double[pt.length];
		double[] ub = new double[pt.length];
		for (int i = 0; i < nVars; ++i) {
			fLB[i] = true;
			fUB[i] = true;
			lb[i] = pt[i];
			ub[i] = pt[i];
		}
		for (int i = 0; i < nVars; ++i) {
			expandBoxAlongDimension(fLB, fUB, lb, ub, i);
			double q = Util.rVar.get(i).computeIntervalProbability(fLB[i], lb[i], fUB[i], ub[i]);
			p = p.multiply(new BigDecimal(Double.toString(q)));
		}
		return p;
	}

	public BigDecimal computeInnerApproximation(BigDecimal p) {
		p = new BigDecimal("0.0");
		if (isEmpty)
			return p;
		int nTrials = 40;
		// int count = 0;
		double[] pt = new double[nVars];
		for (int j = 0; j < nTrials; ++j) {
			for (int i = 0; i < nVars; ++i) {
				pt[i] = Util.rVar.get(i).generateSampleInRange(bx.hasLowerBound(i), bx.lowerBound(i),
						bx.hasUpperBound(i), bx.upperBound(i));
			}
			if (isSatisfied(pt)) {
				BigDecimal pNew = new BigDecimal("0.0");
				pNew = innerApproximationFromSamplePoint(pt, pNew);

				if (pNew.compareTo(p) > -1) {
					p = pNew;
				}
//				count++;
			}
		}
		return p;
	}

	/*-- Cartesian Decompsition of polyhedra.  
	
	
	  1. Collect MSCCs of the constraint graph where xi --- xj 
	     iff xi and xj occur together in some constraint.
	  2. Check if it is worth decomposing by counting the number of inequalities
	     in each MSCC.
	  3. If at least two (or more) non trivial clusters remain, then perform the
	  decomposition.
	
	  ---*/

	public boolean cartesianDecompose(ArrayList<Polytope> retVec) {
		Union union = new Union();
		dset_t d = new dset_t();
		d = union.createDisjointSets(nVars);// initial
		boolean iterDone = false;

		while (!iterDone) {
			iterDone = true;
			// Go through every inequality and pairwise link the sets involved.
			for (int i = 0; i < ineqs.size(); ++i) {
				if (!ineqs.get(i).unionVars(d))
					iterDone = false;
			}
		}
		// Now collect the clusters
		HashMap<Integer, HashSet<Integer>> clusterMap = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, ArrayList<Ineq>> ineqClusters = new HashMap<Integer, ArrayList<Ineq>>();
		union.collectClusters(d, clusterMap);
		// Check if the decomposition is worth it
		int numNonZeroClusters = 0;
		HashSet<Integer> clI = new HashSet<Integer>();
		Iterator it = clusterMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			int j = (int) entry.getKey();
			clI = (HashSet<Integer>) entry.getValue();
			// Check if any inequality belongs to this cluster.
			int ineqCount = 0;
			for (int i = 0; i < ineqs.size(); ++i) {
				if (ineqs.get(i).hasVarInCommon(clI)) {
					ineqCount++;
					if (ineqClusters.containsKey(j)) {
						ineqClusters.get(j).add(ineqs.get(i));//
					} else {
						ArrayList<Ineq> iq = new ArrayList<Ineq>();
						iq.add(ineqs.get(i));
						ineqClusters.put(j, iq);
					}
				}
			}
			if (ineqCount > 0)
				numNonZeroClusters++;
		}

		if (numNonZeroClusters <= 1)
			return false;// It is not worth decomposing.

		// Otherwise, lets go for it.
		it = clusterMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			int j = (int) entry.getKey();
			clI = (HashSet<Integer>) entry.getValue();
			Polytope poly = new Polytope();
			Box bx1 = new Box(Util.rVar);
			poly.setPoly(bx1);

			// Prepare the bounds for the variables in the cluster.
			Iterator<Integer> iter = clI.iterator();
			while (iter.hasNext()) {
				int k = (int) iter.next();
				if (bx.hasUpperBound(k)) {
					poly.setUpperBound(k, bx.upperBound(k));
				}
				if (bx.hasLowerBound(k)) {
					poly.setLowerBound(k, bx.lowerBound(k));
				}
			}

			if (ineqClusters.containsKey(j)) {
				ArrayList<Ineq> vecIneq = new ArrayList<Ineq>();
				vecIneq = ineqClusters.get(j);
				for (int i = 0; i < vecIneq.size(); ++i) {
					poly.ineqs.add(vecIneq.get(i));
				}
			}
			retVec.add(poly);
		}
		return true;
	}

	public int selectBranchDimension() {
		// If a dimension is unbounded or half-bounded select that
		// or else select max-width dimension
		HashMap<Integer, Integer> oMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < ineqs.size(); ++i) {
			ineqs.get(i).collectVarOccurrenceCounts(oMap);
		}

		for (int i = nVars - 1; i >= 0; --i) {
			if (!oMap.containsKey(i))
				continue;
			if (!bx.hasLowerBound(i) && !bx.hasUpperBound(i))
				return i;
		}

		for (int i = 0; i < nVars; ++i) {
			if (!oMap.containsKey(i))
				continue;
			if (!bx.hasLowerBound(i) || !bx.hasUpperBound(i))
				return i;
		}
		double maxVal = bx.upperBound(0) - bx.lowerBound(0);
		int maxDim = -1;
		int maxOMap = -1;
		for (int i = 0; i < nVars; ++i) {
			if (!oMap.containsKey(i))
				continue;
			double delta = bx.upperBound(i) - bx.lowerBound(i);
			if (maxDim == -1 || delta > maxVal || (delta == maxVal && oMap.get(i) > maxOMap)) {
				maxDim = i;
				maxVal = delta;
				maxOMap = oMap.get(i);
			}
		}
		return maxDim;
	}

	public Polytope copyAndRestrict(Polytope p, int dim, boolean hasLB, double lb, boolean hasUB, double ub) {
		Polytope pNew = new Polytope();

		pNew = p.clone();

		if (hasLB && hasUB) {
			assert (lb <= ub);
		}
		if (hasLB) {
			pNew.setLowerBound(dim, lb);
		} else {
			pNew.unsetLowerBound(dim);
		}
		if (hasUB) {
			pNew.setUpperBound(dim, ub);
		} else {
			pNew.unsetUpperBound(dim);
		}
		pNew.boxCheckEmpty();
		return pNew;
	}

	public void branchAndBound(int dim, int nBranches, ArrayList<Polytope> res) {
		assert (dim >= 0);
		assert (dim < nVars);
		assert (nBranches > 1);
		assert (!isEmpty);
		double l, u;
		double delta;
		int i;
		boolean isInteger = Util.rVar.get(dim).isIntegerVariable();

		if (bx.hasBothBound(dim)) {
			l = bx.lowerBound(dim);
			u = bx.upperBound(dim);
			delta = (u - l) / nBranches;
			if (!isInteger) {
				for (i = 0; i < nBranches; ++i) {
					Polytope pNew = new Polytope();
					pNew = copyAndRestrict(this, dim, true, l + i * delta, true, l + (i + 1) * delta);
					res.add(pNew);
				}
				assert (l + (i + 1) * delta >= u);
			} else {
				if (delta <= 1.0) {
					for (i = (int) l; i <= u; ++i) {
						Polytope pNew = new Polytope();
						pNew = copyAndRestrict(this, dim, true, i, true, i);
						res.add(pNew);
					}
				} else {
					double x, y;
					for (i = 0; i < nBranches; ++i) {
						x = l + i * delta;
						y = x + delta;
						if (i > 0 && Math.ceil(x) == Math.floor(x)) {
							x = x + 0.5;
						}
						Polytope pNew = new Polytope();
						pNew = copyAndRestrict(this, dim, true, Math.ceil(x), true, Math.floor(y));
						res.add(pNew);
					}
				}
			}
		} else if (bx.hasLowerBound(dim)) {
			l = bx.lowerBound(dim);
			u = l + 10.0;
			delta = (u - l) / (nBranches - 1);
			for (i = 0; i < nBranches - 1; ++i) {
				Polytope pNew = new Polytope();
				pNew = copyAndRestrict(this, dim, true, l + i * delta, true, l + (i + 1) * delta);
				res.add(pNew);
			}
			assert (l + (i + 1) * delta >= u);
			Polytope pNew = new Polytope();
			pNew = copyAndRestrict(this, dim, true, u, false, 0.0);
			res.add(pNew);
		} else if (bx.hasUpperBound(dim)) {
			u = bx.upperBound(dim);
			l = u - 10.0;
			delta = (u - l) / (nBranches - 1);
			for (i = 0; i < nBranches - 1; ++i) {
				Polytope pNew = new Polytope();
				pNew = copyAndRestrict(this, dim, true, u - (i + 1) * delta, true, u - i * delta);
				res.add(pNew);
			}
			Polytope pNew = new Polytope();
			pNew = copyAndRestrict(this, dim, false, 0.0, true, l);
			res.add(pNew);
		} else {
			if (nBranches == 2) {
				Polytope pNew = new Polytope();
				pNew = copyAndRestrict(this, dim, false, 0.0, true, 0.0);
				res.add(pNew);
				pNew = copyAndRestrict(this, dim, true, 0.0, false, 0.0);
				res.add(pNew);
			} else {
				assert (nBranches > 2);
				Polytope pNew = new Polytope();
				pNew = copyAndRestrict(this, dim, false, 0.0, true, -10.0);
				res.add(pNew);
				pNew = copyAndRestrict(this, dim, true, 10.0, false, 0.0);
				res.add(pNew);
				l = -10.0;
				u = 10.0;
				nBranches = nBranches - 2;
				delta = (u - l) / nBranches;
				for (i = 0; i < nBranches; ++i) {
					pNew = copyAndRestrict(this, dim, true, l + i * delta, true, l + (i + 1) * delta);
					res.add(pNew);
				}
			}
		}
	}

	public void mcBoundProbabilityRec(Polytope p, int maxDepth, int nBranches, BigDecimal[] bd)
			throws LpSolveException {
		if (Util.useICP) {
			p.computeBoundingBoxUsingICP();
		} else {
			p.computeBoundingBoxUsingLP();
		}

		if (p.isABox()) {
			BigDecimal q = new BigDecimal("0.0");
			q = p.computeBoundingBoxProbability1(q);
			bd[0] = bd[0].add(q);// pVal
			bd[1] = bd[1].add(q);// pInner
			return;
		}

		if (isEmpty)
			return;

		if (maxDepth <= 0) {
			BigDecimal q1 = new BigDecimal("0.0");
			q1 = p.computeBoundingBoxProbability1(q1);
			bd[0] = bd[0].add(q1);
			q1 = new BigDecimal("0.0");
			q1 = p.computeInnerApproximation(q1);
			bd[1] = bd[1].add(q1);
			return;
		}

		ArrayList<Polytope> decomposedResult = new ArrayList<Polytope>();
		boolean rVal = false;
		if (Util.productDecompose) {
			rVal = p.cartesianDecompose(decomposedResult);
		}

		if (!rVal) {
			int dim = p.selectBranchDimension();
			ArrayList<Polytope> branches = new ArrayList<Polytope>();
			p.branchAndBound(dim, nBranches, branches);
			for (int i = 0; i < branches.size(); ++i) {
				branches.get(i).mcBoundProbabilityRec(branches.get(i), maxDepth - 1, nBranches, bd);
			}
		} else {
			BigDecimal x = new BigDecimal("1.0");
			BigDecimal y = new BigDecimal("1.0");
			BigDecimal[] ab = new BigDecimal[2];
			for (int i = 0; i < decomposedResult.size(); ++i) {
				ab[0] = new BigDecimal("0.0");
				ab[1] = new BigDecimal("0.0");
				Polytope pp = decomposedResult.get(i);
				if (pp.isABox()) {
					ab[0] = pp.computeBoundingBoxProbability1(ab[0]);
					ab[1] = ab[1].add(ab[0]);
				} else {
					int dim = pp.selectBranchDimension();
					assert (dim >= 0);
					ArrayList<Polytope> branches = new ArrayList<Polytope>();
					pp.branchAndBound(dim, nBranches, branches);
					for (int j = 0; j < branches.size(); ++j) {
						branches.get(j).mcBoundProbabilityRec(branches.get(j), maxDepth - 1, nBranches, ab);
					}
				}
				x = x.multiply(ab[0]);
				y = y.multiply(ab[1]);
			}
			bd[0] = bd[0].add(x);
			bd[1] = bd[1].add(y);
		}
		return;
	}

	public void mcBoundProbability(int depth, int numBranches, BigDecimal[] volComp) throws LpSolveException {
		BigDecimal[] bd = new BigDecimal[2];
		bd[0] = new BigDecimal("0.0");// pVal
		bd[1] = new BigDecimal("0.0");// pInner
		if (!isEmpty) {
//			Fraction f = new Fraction();
			long startTime = System.currentTimeMillis();

			mcBoundProbabilityRec(this, depth, numBranches, bd);

			DecimalFormat df = new DecimalFormat("0.000000");
			//Util.appendContentToFile("\t  "+(System.currentTimeMillis()-startTime)+"         "+df.format(bd[1])+"\t"+df.format(bd[0])+"\t\n");
//			System.out.println(" Probability Upper Bound : " + bd[0]);
//			System.out.println(" Probability Lower Bound : " + bd[1]);
//			f.time = System.currentTimeMillis() - startTime;
//			f.bd = new BigDecimal[2];
//			f.bd[0] = new BigDecimal("0.0");
//			f.bd[1] = new BigDecimal("0.0");
//			f.bd[0] = f.bd[0].add(bd[0]);
//			f.bd[1] = f.bd[1].add(bd[1]);
//			f.nVar = nVars;
//			frac.add(f);
			volComp[0] = volComp[0].add(bd[0]);
			volComp[1] = volComp[1].add(bd[1]);
			//System.out.println(volComp[0]+" "+volComp[1]);
//			MainController m=new MainController();
//			m.logtext.setText(nVars+"\t"+(System.currentTimeMillis()-startTime)+"\t"+df.format(bd[1])+"\t"+df.format(bd[0])+"\t\n");
		}
	}

}
