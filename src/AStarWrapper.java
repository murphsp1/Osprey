import edu.jhuapl.prophecy.EnergyCalculator;
import edu.jhuapl.prophecy.OspreyEnergyCalculator;


public class AStarWrapper {
	protected EnergyCalculator calculator = null;
	
	protected int numTreeLevels;
	protected int numNodesForLevel[] = null;

	protected int curConf[] = null;

	protected ExpansionQueue curExpansion;
	
	public AStarWrapper(int treeLevels, int numRotForRes[], float arpMatrixRed[][], StericCheck stF) {
		calculator = new OspreyEnergyCalculator(treeLevels, numRotForRes, arpMatrixRed);

		numTreeLevels = treeLevels;
		numNodesForLevel = new int[numTreeLevels];

		for (int i = 0; i < numTreeLevels; i++) {
			numNodesForLevel[i] = numRotForRes[i];
		}

		curExpansion = new ExpansionQueue();
		curConf = new int[numTreeLevels];
	}

	public int[] doAStar(boolean run1, int numMaxChanges, int nodesDefault[],
			boolean prunedNodes[], StrandRotamers strandRot[],
			String strandDefault[][], int numForRes[], int strandMut[][],
			boolean singleSeq, int mutRes2Strand[], int mutRes2MutIndex[])
			throws InterruptedException {
		
		int curLevelNum = 0;
		QueueNode expNode = null;

		if (run1) {
			initializeQueue();
		}

		// initialize for this run
		for (int i = 0; i < numTreeLevels; i++) {
			curConf[i] = -1;
		}

		while (true) {
			expNode = curExpansion.curFront;
			if (expNode == null) {
				// exhausted all possibilities or some error
				return curConf;
			}
			curExpansion.delete(expNode);

			for (int i = 0; i <= expNode.level; i++) {
				curConf[i] = expNode.confSoFar[i];
			}
			curLevelNum = expNode.level;

			// if we have a complete conformation, return
			if (curLevelNum == numTreeLevels - 1) {
				return curConf;
			}

			curLevelNum++;
			for (int curNode = 0; curNode < numNodesForLevel[curLevelNum]; curNode++) {
				curConf[curLevelNum] = curNode;
				float score = calculator.getEnergy(curConf, curLevelNum);
				QueueNode node = new QueueNode(curNode, curLevelNum, curConf, score);
				curExpansion.insert(node);
			}
		}
	}


	protected void initializeQueue() {

		// initialize for this run
		for (int i = 0; i < numTreeLevels; i++) {
			curConf[i] = -1;
		}

		// initially, we are at level zero; all nodes at that level are
		// visible;
		// compute their f(n) score and add to the expansion queue
		for (int curNode = 0; curNode < numNodesForLevel[0]; curNode++) {

			// this is the only node in the current conformation
			curConf[0] = curNode;

			// compute f for the current node
			float score = calculator.getEnergy(curConf, 0);

			QueueNode node = new QueueNode(curNode, 0, curConf, score);
			curExpansion.insert(node);
		}
	}
}
