package edu.jhuapl.prophecy;


public class OspreyEnergyCalculator implements EnergyCalculator {

	protected float pairwiseMinEnergyMatrix[][] = null;
	protected int nodeIndexOffset[] = null;
	protected int numTotalNodes;
	protected int numTreeLevels;
	private int numNodesForLevel[] = null;

	public OspreyEnergyCalculator(int numTreeLevels, int numRotForRes[], float arpMatrixRed[][]) {
		
		this.numTreeLevels = numTreeLevels;

		numTotalNodes = 0;
		nodeIndexOffset = new int[numTreeLevels];
		numNodesForLevel = new int[numTreeLevels];
		for (int i = 0; i < numTreeLevels; i++) {
			nodeIndexOffset[i] = numTotalNodes;
			numTotalNodes += numRotForRes[i];
			numNodesForLevel[i] = numRotForRes[i];
		}

		// the min energy matrix 
		// the last column contains the intra-energy for each rotamer
		// the last row contains the shell-residue energy for each rotamer
		pairwiseMinEnergyMatrix = new float[numTotalNodes + 1][numTotalNodes + 1];
		
		for (int i = 0; i < numTotalNodes + 1; i++) {
			for (int j = 0; j < numTotalNodes + 1; j++) {
				pairwiseMinEnergyMatrix[i][j] = arpMatrixRed[i][j];
			}
		}
	}

	@Override
	public float getEnergy(int[] conformation, int treeLevel) {
		return computeG(conformation, treeLevel) + computeH(conformation, treeLevel);
	}

	protected float computeG(int[] conformation, int treeLevel) {
		float gn = 0.0f;
		float minShellResE;
		float minIndVoxE;
		float sumMinPairE;
		for (int curLevel = 0; curLevel <= treeLevel; curLevel++) {

			int crazyIndex = nodeIndexOffset[curLevel] + conformation[curLevel];

			minShellResE = getReducedShellRotE(pairwiseMinEnergyMatrix, crazyIndex, numTotalNodes);
			minIndVoxE = pairwiseMinEnergyMatrix[crazyIndex][numTotalNodes];
			sumMinPairE = gSumMinPVE(treeLevel, curLevel + 1, crazyIndex, conformation);

			gn += (minShellResE + minIndVoxE + sumMinPairE);
		}

		return gn;
	}

	protected float computeH(int[] conformation, int treeLevel) {

		float hn = 0.0f;

		for (int curLevel = treeLevel + 1; curLevel < numTreeLevels; curLevel++) {
			hn += getEnergyAtLevel(treeLevel, curLevel, conformation);
		}

		return hn;
	}

	protected float gSumMinPVE(int topLevel, int startLevel, int index1, int conf[]) {

		int index2;
		float sum = 0.0f;

		for (int level = startLevel; level <= topLevel; level++) {
			index2 = nodeIndexOffset[level] + conf[level]; // s at j
			sum += pairwiseMinEnergyMatrix[index1][index2];
		}

		return sum;
	}

	protected float getReducedShellRotE(float RedEmat[][], int index, int numTotalNodes) {
		float shlRotE = 0.0f;
		// Skip the first energy which is the intra-rot energy still
		for (int i = numTotalNodes; i < RedEmat.length; i++) {
			shlRotE += RedEmat[i][index];
		}
		return shlRotE;
	}

	protected float getEnergyAtLevel(int topLevel, int curLevel, int conf[]) {

		float minE = (float) Math.pow(10, 30);
		float curE;
		int index1;

		float minShellResE;
		float minIndVoxE;
		float sumMinPairE;
		float sumMinMinPairE;

		for (int i1 = 0; i1 < numNodesForLevel[curLevel]; i1++) {

			index1 = nodeIndexOffset[curLevel] + i1; // the index of s at j

			minShellResE = getReducedShellRotE(pairwiseMinEnergyMatrix, index1, numTotalNodes);
			minIndVoxE = pairwiseMinEnergyMatrix[index1][numTotalNodes];
			sumMinPairE = hSumMinPVE(topLevel, index1, conf);
			sumMinMinPairE = sumMinMinPVE(topLevel + 1, curLevel, index1);

			curE = minShellResE + minIndVoxE + sumMinPairE + sumMinMinPairE;
			if (curE < minE) {
				// compare to the min energy found so far
				minE = curE;
			}
		}

		return minE;
	}

	protected float hSumMinPVE(int topLevel, int index1, int conf[]) {

		float sum = 0.0f;
		int index2;

		for (int level = 0; level <= topLevel; level++) {

			// the index of r at i
			index2 = nodeIndexOffset[level] + conf[level]; 

			// the pairwise energy between the two nodes
			sum += pairwiseMinEnergyMatrix[index2][index1]; 
		}

		return sum;
	}

	protected float sumMinMinPVE(int startLevel, int jLevel, int firstIndex) {

		float sum = 0.0f;
		for (int level = jLevel + 1; level < numTreeLevels; level++) {
			sum += indMinMinPVE(level, firstIndex);
		}

		return sum;
	}

	protected float indMinMinPVE(int kLevel, int firstIndex) {

		float minEn = (float) Math.pow(10, 30);
		float curEn;
		int secondIndex;

		for (int i2 = 0; i2 < numNodesForLevel[kLevel]; i2++) { // u at k

			secondIndex = nodeIndexOffset[kLevel] + i2;

			curEn = pairwiseMinEnergyMatrix[firstIndex][secondIndex];
			if (curEn < minEn) {
				minEn = curEn;
			}
		}

		return minEn;
	}

}
