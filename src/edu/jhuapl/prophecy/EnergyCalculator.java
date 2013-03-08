package edu.jhuapl.prophecy;


/**
 * Calculates the energy estimate of a partial (or complete) path through a tree.
 * 
 * If this were distance rather than energy it would be the current distance from starting place 
 * plus the estimate of the distance to the goal. 
 *
 */
public interface EnergyCalculator {
	/**
	 * Get the energy for this specified branch in the tree
	 * 
	 * @param conformation Array of branch indices (zero-based) 
	 * @param treeLevel    How deep in the tree are we?
	 * @return
	 */
	public float getEnergy(int conformation[], int treeLevel);
}
