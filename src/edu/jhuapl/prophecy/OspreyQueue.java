package edu.jhuapl.prophecy;

import com.google.common.collect.MinMaxPriorityQueue;

public class OspreyQueue {

	private MinMaxPriorityQueue<OspreyQueueNode> thequeue;

	// a pointer to the first node in the expansion list
	public OspreyQueueNode curFront;

	// number of nodes in the list
	public int numNodes;

	// the unique id of each node in the queue
	public int idNUM;

	// constructor
	OspreyQueue() {
		curFront = null;
		numNodes = 0;

		// 5,000,000 is about 1 GB of memory
		thequeue = MinMaxPriorityQueue.maximumSize(5000000).create();
	}

	public void insert(OspreyQueueNode newNode) {
		thequeue.add(newNode);
		curFront = thequeue.peek();
	}

	public void delete(OspreyQueueNode delNode) {
		thequeue.remove(delNode);
		curFront = thequeue.peek();
	}

	public OspreyQueueNode getMin() {
		return thequeue.poll();
	}
}
