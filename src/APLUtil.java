import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

public class APLUtil {

    /**
     * Get the sum of rotamers left
     * 
     * @param numRotForRes
     * @param prunedRotAtRes
     * @return
     */
    public long getNumberOfRotamersLeft(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {

	int[] numNotPrunedForRes = getRotamersLeftArray(numRotForRes, prunedRotAtRes);
	
        long total = 0;
	for (int numLeft: numNotPrunedForRes) {
	    total += numLeft;
	}
	
	return total;
    }

    public long getNumberRotamersAvail(int numRotForRes[]) {
	long total = 0;
        for (int i = 0; i < numRotForRes.length; i++) {
            total += numRotForRes[i];
        }
	return total;
    }

    /**
     * Does not work yet
     * Get size of conformation space (product of rotamers)
     * 
     * @param numRotForRes
     * @param prunedRotAtRes
     * @return
     */
    public double getNumberOfConformationsLeft(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
	int[] numNotPrunedForRes = getRotamersLeftArray(numRotForRes, prunedRotAtRes);
	
        double total = 1;
	for (int numLeft: numNotPrunedForRes) {
	    total *= numLeft;
	}
	
	return total;
    }

    /**
     * Does not work yet
     * @param numRotForRes
     * @return
     */
    public double getNumberConformationsAvail(int numRotForRes[]) {
	double total = 1;
        for (int i = 0; i < numRotForRes.length; i++) {
            total *= numRotForRes[i];
        }
	return total;
    }

    protected int[] getRotamersLeftArray(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
        int numNotPrunedForRes[] = new int[numRotForRes.length];
        for (int i = 0; i < numNotPrunedForRes.length; i++) {
            numNotPrunedForRes[i] = numRotForRes[i];
        }

        Iterator<RotInfo<Boolean>> i = prunedRotAtRes.iterator();
        while (i.hasNext()) {
            RotInfo<Boolean> ri = i.next();
            if (ri.state) {
        	numNotPrunedForRes[ri.curPos]--;
            }
        }
        return numNotPrunedForRes;
    }

    /**
     * This does not work yet
     * 
     * @param numRotForRes
     * @param prunedRotAtRes
     * @return
     */
    public double getNumberOfMutantsLeft(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
	
	TreeMap<Integer, HashSet<Integer>> aminoAcidsLeft = new TreeMap<Integer, HashSet<Integer>>();
        for (int i = 0; i < numRotForRes.length; i++) {
            aminoAcidsLeft.put(i, new HashSet<Integer>());
        }

        // get a map of pos -> set of amino acids available
        Iterator<RotInfo<Boolean>> i = prunedRotAtRes.iterator();
        while (i.hasNext()) {
            RotInfo<Boolean> ri = i.next();
            if (ri.state == false) {
        	aminoAcidsLeft.get(ri.curPos).add(ri.curAA);
            }
        }
        
        // check for zero amino acids left at a position
        for (Integer pos : aminoAcidsLeft.keySet()) {
            if (aminoAcidsLeft.get(pos).size() == 0) {
        	return 0.0;
            }
        }
        
        // find all the positions that have more than 1 amino acid
        ArrayList<Integer> mutablePositions = new ArrayList<Integer>();
        for (Integer pos : aminoAcidsLeft.keySet()) {
            if (aminoAcidsLeft.get(pos).size() > 1) {
        	mutablePositions.add(pos);
            }
        }
        
        // wild type
        long total = 1;
        
        // one point mutation
        for (Integer pos : mutablePositions) {
            total += aminoAcidsLeft.get(pos).size() - 1;
        }
        
        // two point mutation
        int numMutablePositions = mutablePositions.size();
        for (int m=0; m<numMutablePositions; m++) {
    	    int numAtPos1 = aminoAcidsLeft.get(mutablePositions.get(m)).size();
            for (int n=(m+1); n<numMutablePositions; n++) {
        	int numAtPos2 = aminoAcidsLeft.get(mutablePositions.get(n)).size();
        	total += (numAtPos1 - 1) * (numAtPos2 - 1);
            }
        }
        return total;
    }
    
    /**
     * Get the number of mutants that we started with
     */
    public long getNumberMutantsAvail() {
	// wild type
	long total = 1;
	
	// single point mutations
	total += 19 * 16;
	
	// double point mutations
	long sixteen_choose_two = 120;
	total += sixteen_choose_two * 19 * 19;
	
	return total;
    }
    
    protected int[] getAminoAcidsLeftArray(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
        int numAminoAcidsForRes[] = new int[numRotForRes.length];
        HashMap<Integer, HashSet<Integer>> aminoAcidsLeft = new HashMap<Integer, HashSet<Integer>>();
        for (int i = 0; i < numRotForRes.length; i++) {
            aminoAcidsLeft.put(i, new HashSet<Integer>());
        }
        
        Iterator<RotInfo<Boolean>> i = prunedRotAtRes.iterator();
        while (i.hasNext()) {
            RotInfo<Boolean> ri = i.next();
            if (ri.state == false) {
        	aminoAcidsLeft.get(ri.curPos).add(ri.curAA);
            }
        }

        for (Integer key : aminoAcidsLeft.keySet()) {
            numAminoAcidsForRes[key] = aminoAcidsLeft.get(key).size();
        }
        return numAminoAcidsForRes;
    }

    /**
     * This needs work
     */
    protected double getConformationsLeft2(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes, RotamerLibrary lib) {
	TreeMap<Integer, HashSet<Integer>> aminoAcidsLeft = new TreeMap<Integer, HashSet<Integer>>();
        for (int i = 0; i < numRotForRes.length; i++) {
            aminoAcidsLeft.put(i, new HashSet<Integer>());
        }

        // get a map of pos -> set of amino acids available
        Iterator<RotInfo<Boolean>> i = prunedRotAtRes.iterator();
        while (i.hasNext()) {
            RotInfo<Boolean> ri = i.next();
            if (ri.state == false) {
        	aminoAcidsLeft.get(ri.curPos).add(ri.curAA);
            }
        }
        
        // check for zero amino acids left at a position
        for (Integer pos : aminoAcidsLeft.keySet()) {
            if (aminoAcidsLeft.get(pos).size() == 0) {
        	return 0.0;
            }
        }
        
        // find all the positions that have more than 1 amino acid
        ArrayList<Integer> mutablePositions = new ArrayList<Integer>();
        ArrayList<Integer> nonMutablePositions = new ArrayList<Integer>();
        for (Integer pos : aminoAcidsLeft.keySet()) {
            if (aminoAcidsLeft.get(pos).size() > 1) {
        	mutablePositions.add(pos);
            } else {
        	nonMutablePositions.add(pos);
            }
        }
        
        // non mutable conformations
        double nonMutableConformations = 1;
        for (Integer pos: nonMutablePositions) {
            int aa = aminoAcidsLeft.get(pos).iterator().next();
            int numConformations = lib.getNumRotForAAtype(aa);
            if (numConformations == 0) {
        	numConformations = 1;
            }
            nonMutableConformations *= numConformations;
        }
        
        double total = 0;
        
        // wild type
        double wildTypeConformations = nonMutableConformations;
        for (Integer pos: mutablePositions) {
            // first one is treated as our wild type
            int aa = aminoAcidsLeft.get(pos).iterator().next();
            int numConformations = lib.getNumRotForAAtype(aa);
            if (numConformations == 0) {
        	numConformations = 1;
            }
            wildTypeConformations *= numConformations;
        }
        total += wildTypeConformations;
        
        // one point mutation
        for (Integer pos : mutablePositions) {
            HashSet<Integer> aaSet = aminoAcidsLeft.get(pos);
            Iterator<Integer> iterator = aaSet.iterator();
            int aa = iterator.next();
            int numConformations = lib.getNumRotForAAtype(aa);
            if (numConformations == 0) {
        	numConformations = 1;
            }
            double conformationsMinusMutation = wildTypeConformations / numConformations;
            while (iterator.hasNext()) {
        	aa = iterator.next();
                numConformations = lib.getNumRotForAAtype(aa);
                if (numConformations == 0) {
            	    numConformations = 1;
                }
                total += numConformations * conformationsMinusMutation;
            }
        }
        
        // two point mutation
        int numMutablePositions = mutablePositions.size();
        for (int m=0; m<numMutablePositions; m++) {
            HashSet<Integer> aaSet1 = aminoAcidsLeft.get(mutablePositions.get(m));
            Iterator<Integer> iterator = aaSet1.iterator();
            int aa = iterator.next();
            int numConformations = lib.getNumRotForAAtype(aa);
            if (numConformations == 0) {
        	numConformations = 1;
            }

            double pos1Conformations = 7;
    	    int numAtPos1 = aminoAcidsLeft.get(mutablePositions.get(m)).size();
            for (int n=(m+1); n<numMutablePositions; n++) {
                HashSet<Integer> aaSet2 = aminoAcidsLeft.get(mutablePositions.get(n);
        	int numAtPos2 = aminoAcidsLeft.get(mutablePositions.get(n)).size();
        	total += (numAtPos1 - 1) * (numAtPos2 - 1);
            }
        }
        return total;
    }
    
    /**
     * Reset the pruned array so that nothing is pruned
     */
    public void resetPrunedArray(PrunedRotamers<Boolean> prunedRotAtRes) {
	Iterator<RotInfo<Boolean>> i = prunedRotAtRes.iterator();
        while (i.hasNext()) {
            RotInfo<Boolean> ri = i.next();
            prunedRotAtRes.set(ri.curPos, ri.curAA, ri.curRot, false);
        }
    }

    /**
     * Displays amino acid index, name, and number of rotamers
     * @param lib
     */
    public void displayRotamerLibrary(RotamerLibrary lib) {
	for (int i=0; i<21; i++) {
	    int numRotamers = lib.getNumRotForAAtype(i);
	    String name = lib.getAAName(i);
	    System.out.println("" + i + ":\t" + name + "\t " + numRotamers);
	}
    }
}
