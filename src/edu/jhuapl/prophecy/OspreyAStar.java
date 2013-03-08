package edu.jhuapl.prophecy;


public class OspreyAStar implements SearchAlgorithm {
	
	protected OspreyQueue queue = null;
	protected int numLevels;
	protected int numBranchesPerLevel[] = null;

	protected int conformation[] = null;
	
	protected EnergyCalculator calculator;
	
	public OspreyAStar(EnergyCalculator calculator, int numTreeLevels, int numBranchesPerLevel[]) {
		this.calculator = calculator;
		this.numLevels = numTreeLevels;
		
		this.numBranchesPerLevel = new int[numTreeLevels];
		for (int i = 0; i < numTreeLevels; i++) {
			this.numBranchesPerLevel[i] = numBranchesPerLevel[i];
		}

		conformation = new int[numTreeLevels];
	}

	@Override
	public int[] getConformation() {
		OspreyQueueNode bestNode = null;
		int curLevelNum = 0;

		if (queue == null) {
			initialize();
		}

		clearConformation(conformation);

		while (true) {
			bestNode = queue.curFront;
			if (bestNode == null) {
				// exhausted all possibilities or some error (why not return null?)
				return conformation;
			}
			queue.delete(bestNode);

			curLevelNum = bestNode.level;
			copyConformation(bestNode);
			
			// if we have a complete conformation, return
			if (curLevelNum == numLevels - 1) {
				return conformation;
			}

			curLevelNum++;
			for (int curNode = 0; curNode < numBranchesPerLevel[curLevelNum]; curNode++) {
				conformation[curLevelNum] = curNode;
				float score = calculator.getEnergy(conformation, curLevelNum);
				OspreyQueueNode node = new OspreyQueueNode(curNode, curLevelNum, conformation, score);
				queue.insert(node);
			}
		}
	}

	protected void initialize() {
		this.queue = new OspreyQueue();
		clearConformation(conformation);
		for (int curNode = 0; curNode < numBranchesPerLevel[0]; curNode++) {
			conformation[0] = curNode;

			float score = calculator.getEnergy(conformation, 0);

			OspreyQueueNode node = new OspreyQueueNode(curNode, 0, conformation, score);
			queue.insert(node);
		}
	}

	protected void clearConformation(int conformation[]) {
		// initialize for this run
		for (int i = 0; i < numLevels; i++) {
			conformation[i] = -1;
		}		
	}

	protected void copyConformation(OspreyQueueNode node) {
		for (int i = 0; i <= node.level; i++) {
			conformation[i] = node.confSoFar[i];
		}
	}

}
