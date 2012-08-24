import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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

    public double getNumberOfMutantsLeft(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
	int[] aminoAcidsLeft = getAminoAcidsLeftArray(numRotForRes, prunedRotAtRes);
	
        double total = 1;
	for (int numLeft: aminoAcidsLeft) {
	    total *= numLeft;
	}
	
	return total;
    }
    
    public int[] getAminoAcidsLeftArray(int numRotForRes[], PrunedRotamers<Boolean> prunedRotAtRes) {
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

}
