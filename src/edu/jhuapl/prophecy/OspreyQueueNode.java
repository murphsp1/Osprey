package edu.jhuapl.prophecy;

public class OspreyQueueNode implements Comparable {

	// the f(n) score associated with the current node
	public double fScore;

	// corresponding level
	public int level;

	// the numbers of the nodes in the considered conformation up to the current
	// level
	public int confSoFar[];

	// the number of the corresponding node at that level
	public int nodeNum;

	// a pointer to the previous and next nodes in the expansion list
	public OspreyQueueNode prevNode;
	public OspreyQueueNode nextNode;

	// constructor
	OspreyQueueNode(int curNode, int curLevel, int curConf[], double fn) {

		nodeNum = curNode;
		level = curLevel;

		confSoFar = new int[level + 1];

		for (int i = 0; i <= level; i++) {
			confSoFar[i] = curConf[i];
		}
		fScore = fn;

		prevNode = null;
		nextNode = null;
	}

	public int compareTo(Object otherObject) throws ClassCastException {
		if (!(otherObject instanceof OspreyQueueNode)) {
			throw new ClassCastException("A QueueNode object expected.");
		}
		OspreyQueueNode other = (OspreyQueueNode) otherObject;
		if (this.fScore > other.fScore) {
			return 1;
		} else if (this.fScore < other.fScore) {
			return -1;
		} else { // Nodes have the same score
			if (this.level > other.level || this.nodeNum != other.nodeNum) {
				return -1; // but different levels, this is larger by default
			} else if (checkConf(this, other)) {
				return 0;
			} else { // Two distinct nodes have the same fScore, say this one is
				// larger.
				return 1;
			}
		}
	}

	// Checks if the two given nodes have the same partially assigned
	// conformation
	private boolean checkConf(OspreyQueueNode node1, OspreyQueueNode node2) {

		if (node1.level != node2.level) // different level
			return false;

		for (int l = 0; l < node1.confSoFar.length; l++) {
			if (node1.confSoFar[l] != node2.confSoFar[l])
				return false;
		}

		// The partially assigned conformations are the same
		return true;
	}

}
