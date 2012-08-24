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
	int[] aminoAcidsLeft = getAminoAcidsLeftArray(numRotForRes, prunedRotAtRes);
	
        double total = 1;
	for (int numLeft: aminoAcidsLeft) {
	    total *= numLeft;
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
