import edu.jhuapl.prophecy.EnergyCalculator;
import edu.jhuapl.prophecy.OspreyAStar;
import edu.jhuapl.prophecy.OspreyEnergyCalculator;
import edu.jhuapl.prophecy.SearchAlgorithm;

/**
 * Provides the same interface as MSAStar so that we can experiment behind the scenes
 *
 */
public class AStarWrapper {

	protected EnergyCalculator calculator = null;
	protected SearchAlgorithm search = null;
	
	public AStarWrapper(int treeLevels, int numRotForRes[], float arpMatrixRed[][], StericCheck stF) {
		calculator = new OspreyEnergyCalculator(treeLevels, numRotForRes, arpMatrixRed);
		search = new OspreyAStar(calculator, treeLevels, numRotForRes);
	}

	public int[] doAStar(boolean run1, int numMaxChanges, int nodesDefault[],
			boolean prunedNodes[], StrandRotamers strandRot[],
			String strandDefault[][], int numForRes[], int strandMut[][],
			boolean singleSeq, int mutRes2Strand[], int mutRes2MutIndex[])
			throws InterruptedException {
		
		return search.getConformation();
	}
}
