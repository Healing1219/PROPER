package inference;

import java.util.HashMap;
import java.util.HashSet;

public class Union {

	public static class dset_t {
		int nVars;
		int[] parent;
		int[] rank;
	}

	public int getRank(dset_t d, int i) {
		return d.rank[i];
	}

	public int getParent(dset_t d, int i) {
		return d.parent[i];
	}

	public void setParent(dset_t d, int i, int j) {
		d.parent[i] = j;
	}

	public void setRank(dset_t d, int i, int j) {
		d.rank[i] = j;
	}

	public dset_t createDisjointSets(int numVars) {
		dset_t rSet = new dset_t();
		rSet.nVars = numVars;
		rSet.rank = new int[numVars];
		rSet.parent = new int[numVars];

		for (int i = 0; i < numVars; ++i) {
			rSet.rank[i] = 0;
			rSet.parent[i] = i;
		}
		return rSet;
	}

	public void link(dset_t d, int i, int j) {
		int rI = getRank(d, i);
		int rJ = getRank(d, j);
		if (rI > rJ) {
			// Link j to i
			setParent(d, j, i);
		} else {
			// Link i to j
			setParent(d, i, j);
			if (rI == rJ)
				setRank(d, j, rJ + 1);
		}
	}

	public int find(dset_t d, int i) {
		int pI = getParent(d, i);
		if (i == pI)
			return i;
		int qI = find(d, pI);
		setParent(d, i, qI);
		return qI;
	}

	public boolean doUnion(dset_t d, int i, int j) {
		int qI = find(d, i);
		int qJ = find(d, j);
		if (qI == qJ)
			return false;
		link(d, qI, qJ);
		return true;
	}

	public void collectClusters(dset_t d,HashMap<Integer,HashSet<Integer>> clusterMap) {
		for(int i=0;i<d.nVars;++i) {
			int qI=find(d,i);
			boolean flag=clusterMap.containsKey(qI);
			if(flag) {
				clusterMap.get(qI).add(i);
			}else {
				HashSet<Integer> sI=new HashSet<Integer>();
				sI.add(i);
				sI.add(qI);
				clusterMap.put(qI, sI);
			}
		}
	}
	
}
