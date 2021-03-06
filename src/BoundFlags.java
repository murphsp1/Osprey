/*
	This file is part of OSPREY.

	OSPREY Protein Redesign Software Version 2.0
	Copyright (C) 2001-2012 Bruce Donald Lab, Duke University

	OSPREY is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as 
	published by the Free Software Foundation, either version 3 of 
	the License, or (at your option) any later version.

	OSPREY is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, see:
	      <http://www.gnu.org/licenses/>.

	There are additional restrictions imposed on the use and distribution
	of this open-source code, including: (A) this header must be included
	in any modification or extension of the code; (B) you are required to
	cite our papers in any publications that use this code. The citation
	for the various different modules of our software, together with a
	complete list of requirements and restrictions are found in the
	document license.pdf enclosed with this distribution.

	Contact Info:
			Bruce Donald
			Duke University
			Department of Computer Science
			Levine Science Research Center (LSRC)
			Durham
			NC 27708-0129 
			USA
			e-mail:   www.cs.duke.edu/brd/

	<signature of Bruce Donald>, Mar 1, 2012
	Bruce Donald, Professor of Computer Science
 */

///////////////////////////////////////////////////////////////////////////////////////////////
//	BoundFlags.java
//
//	Version:           2.0
//
//
//	  authors:
// 	  initials    name                 organization                email
//	---------   -----------------    ------------------------    ----------------------------
//	  ISG		 Ivelin Georgiev	  Duke University			  ivelin.georgiev@duke.edu
//     KER        Kyle E. Roberts       Duke University         ker17@duke.edu
//     PGC        Pablo Gainza C.       Duke University         pablo.gainza@duke.edu
///////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Written by Ivelin Georgiev (2004-2009)
 * 
 */

/**
 * Applies the Bounding Flags pruning criteria: computes a lower bound on the
 * energy of all conformations that contain a given rotamer pair (i_r,j_s), for
 * each rotamer pair
 * 
 */
public class BoundFlags {

    // pairwise energy matrix for the min energies
    private float pairwiseMinEnergyMatrix[][][][][][] = null;

    // eliminated rotamers at position i, for all positions
    private PrunedRotamers<Boolean> eliminatedRotAtPos = null;

    // number of residues under consideration
    // private int numSiteResidues;

    // for each residue, number of possible amino acids
    // private int numTotalRot;

    // number of possible rotamers for the ligand
    // int numLigRot;

    // offset of the given rotamer in the total rotamer set (?152?)
    // int rotIndOffset[];

    // the number of AA tyes allowed for each AS residue
    int numAAtypes[] = null;

    // number of rotamers for the current AA type at the given residue
    // int numRotForAAtypeAtRes[];

    // this value depends on the particular value specified in the pairwise
    // energy matrices;
    // in KSParser, this value is 10^38;
    // entries with this particular value will not be examined, as they are not
    // allowed;
    // note that when computing E intervals, if a steric is not allowed,
    // (maxE-minE)=0,
    // so no comparison with stericE is necessary there
    private float bigE = (float) Math.pow(10, 38);

    // steric energy that determines incompatibility of a rotamer with the
    // template
    float stericE = bigE;

    // private double curEw = 0.0f; //the max allowable difference from the GMEC
    // (checkSum<=curEw should not be pruned)

    // PrintStream logPS = null;

    // the minimum lower energy bound for all pruned conformations
    double Ec = bigE;

    // the lowest energy bound involving the current rotamer
    double curEc;

    double minEmin = bigE; // used in Ec computation

    // the number of rotamers for the current mutation sequence only
    int numRotForMut = 0;

    // stores the Ec for each pair of rotamers
    // double pairEc[][] = null;
    double pairEc[][][][][][] = null;

    // determines if a rotamer index is a part of the current mutation sequence
    PrunedRotamers<Boolean> rotInMutInd;

    // the rotamer library
    // RotamerLibrary rl = null;

    // The system rotamer handler
    // StrandRotamers sysLR = null;

    // The mapping from AS position to actual residue numbers
    // int residueMap[] = null;

    double pruningE = bigE; // the lower-bound energy cutoff for pruning

    double Ew = 0.0; // the E window allowed from the best energy

    // the precomputed single and pair interval terms
    double indInt[][] = null;
    double pairInt[][] = null;

    // split flags for all rotamer pairs
    // KER: split flags is now a 6D array like arpMatrix
    boolean splitFlags[][][][][][] = null;

    // determines if split flags are used
    boolean useFlags = true;

    // the percent of pruned non-steric rotamers
    // final double lambda = 0.3;//0.08;

    // int ligAANum = -1; //the ligand amino acid index

    private int numMutable;
    StrandRotamers strandRot[] = null;
    int strandMut[][] = null;
    int mutRes2Strand[] = null;
    int mutRes2MutIndex[] = null;

    // constructor
    BoundFlags(float arpMatrix[][][][][][], int numResMutable, int strMut[][],
	    StrandRotamers strandLRot[], double pruneE,
	    PrunedRotamers<Boolean> prunedRotAtRes,
	    boolean spFlags[][][][][][], boolean useSF, float initEw,
	    int mutRes2StrandP[], int mutRes2MutIndexP[]) {

	splitFlags = spFlags;
	pairwiseMinEnergyMatrix = arpMatrix;

	eliminatedRotAtPos = prunedRotAtRes;
	// residueMap = resMap;
	// sysLR = systemLRot;
	// rl = rlP;
	useFlags = useSF;

	strandMut = strMut;
	strandRot = strandLRot;
	mutRes2Strand = mutRes2StrandP;
	mutRes2MutIndex = mutRes2MutIndexP;
	numMutable = numResMutable;

	// numSiteResidues = numResInActiveSite; // tested with 9 AS

	/*
	 * numLigRot = numLigRotamers; // 0 if no ligand if (numLigRot>0)
	 * ligAANum = ligROT.getIndexOfNthAllowable(0,0);
	 */

	pruningE = pruneE;
	Ew = initEw;

	rotInMutInd = new PrunedRotamers<Boolean>(eliminatedRotAtPos, false);

	/********* Set up pairEc as a 6D Array like the pair E matrix **********/
	// pairEc = new
	// double[eliminatedRotAtPos.length][eliminatedRotAtPos.length];
	pairEc = initPairEc(arpMatrix);

	// KER: Already Initialized
	/*
	 * for (int i=0; i<eliminatedRotAtPos.length; i++){ rotInMutInd[i] =
	 * false; //true only if the given rotamer index is used in the current
	 * mutation sequence
	 * 
	 * //for (int j=0; j<eliminatedRotAtPos.length; j++) // pairEc[i][j] =
	 * bigE; }
	 */

	numAAtypes = new int[numMutable];

	int ctr = 0;
	for (int str = 0; str < strandMut.length; str++) { // the number of AAs
							   // allowed for each
							   // AS residue
	    for (int i = 0; i < strandMut[str].length; i++) {
		numAAtypes[ctr] = strandRot[str]
			.getNumAllowable(strandMut[str][i]);
		ctr++;
	    }
	}

	Ec = bigE;
	curEc = 0.0;
    }

    public double[][][][][][] initPairEc(float[][][][][][] fromMatrix) {

	double toMatrix[][][][][][] = new double[fromMatrix.length][][][][][];
	// KER: we only want the pairwise interactions and not the shell-shell
	// in the last row
	for (int p1 = 0; p1 < toMatrix.length - 1; p1++) {
	    if (fromMatrix[p1] != null) {
		toMatrix[p1] = new double[fromMatrix[p1].length][][][][];
		for (int a1 = 0; a1 < toMatrix[p1].length; a1++) {
		    if (fromMatrix[p1][a1] != null) {
			toMatrix[p1][a1] = new double[fromMatrix[p1][a1].length][][][];
			for (int r1 = 0; r1 < toMatrix[p1][a1].length; r1++) {
			    if (fromMatrix[p1][a1][r1] != null) {
				toMatrix[p1][a1][r1] = new double[fromMatrix[p1][a1][r1].length][][];
				for (int p2 = 0; p2 < toMatrix[p1][a1][r1].length; p2++) {
				    if (fromMatrix[p1][a1][r1][p2] != null) {
					toMatrix[p1][a1][r1][p2] = new double[fromMatrix[p1][a1][r1][p2].length][];
					for (int a2 = 0; a2 < toMatrix[p1][a1][r1][p2].length; a2++) {
					    if (fromMatrix[p1][a1][r1][p2][a2] != null) {
						toMatrix[p1][a1][r1][p2][a2] = new double[fromMatrix[p1][a1][r1][p2][a2].length];
						for (int r2 = 0; r2 < toMatrix[p1][a1][r1][p2][a2].length; r2++) {
						    toMatrix[p1][a1][r1][p2][a2][r2] = bigE;
						}
						// System.arraycopy(fromMatrix[p1][a1][r1][p2][a2],
						// 0,
						// toMatrix[p1][a1][r1][p2][a2],
						// 0,
						// fromMatrix[p1][a1][r1][p2][a2].length);
					    }
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	return toMatrix;

    }

    public double getEc() {
	return Ec;
    }

    // Precompute the int terms for indInt[][] and pairInt[][]
    private void precomputeInt() {

	int numRes = numMutable;
	/*
	 * if (numLigRot!=0) //ligand is present numRes++;
	 */

	indInt = new double[numRes][numRes];
	pairInt = new double[numRes][numRes];

	for (int posNum1 = 0; posNum1 < numRes; posNum1++) {
	    for (int posNum2 = posNum1 + 1; posNum2 < numRes; posNum2++) {

		curEc = 0.0;
		SumMaxIndInt(posNum1, posNum2);
		indInt[posNum1][posNum2] = curEc; // formula term 3 KER: sum_{j
						  // (j!=h,i)} min_s {E(j_s)}
		indInt[posNum2][posNum1] = indInt[posNum1][posNum2];

		curEc = 0.0;
		SumSumMaxPairInt(posNum1, posNum2);
		pairInt[posNum1][posNum2] = curEc; // formula term 4 sum_{j}
						   // sum_{k (j,k != h,i)}
						   // min_{s,u} {E(j_s,k_u)}
		pairInt[posNum2][posNum1] = pairInt[posNum1][posNum2];

		curEc = 0.0;
	    }
	}
    }

    // Compute the conformations that can be eliminated
    // Return a boolean matrix in which an element is true if
    // the corresponding r at i can be eliminated, and false otherwise
    public boolean[][][][][][] ComputeEliminatedRotConf() {

	precomputeInt();

	boolean done = false;
	int prunedCurRun = 0;
	int numRuns = 1;

	while (!done) {

	    prunedCurRun = 0;

	    System.out.println("Current run: " + numRuns);

	    // Compute for the AS residues first
	    for (int curPos1 = 0; curPos1 < numMutable; curPos1++) {

		int str1 = mutRes2Strand[curPos1];
		int strResNum1 = strandMut[str1][mutRes2MutIndex[curPos1]];
		// System.out.print("Starting AS residue "+curPos1);

		for (int AA1 = 0; AA1 < numAAtypes[curPos1]; AA1++) {

		    // System.out.print(".");

		    int curAA1 = strandRot[str1].getIndexOfNthAllowable(
			    strResNum1, AA1);

		    // find how many rotamers are allowed for the current AA
		    // type at the given residue;
		    // note that ala and gly have 0 possible rotamers
		    int numRotForCurAAatPos1 = strandRot[str1].rl
			    .getNumRotForAAtype(curAA1);
		    if (numRotForCurAAatPos1 == 0) // ala or gly
			numRotForCurAAatPos1 = 1;

		    for (int curRot1 = 0; curRot1 < numRotForCurAAatPos1; curRot1++) {

			numRotForMut++;

			// int index1 = curPos1*numTotalRot +
			// rotIndOffset[curAA1] + curRot1;
			rotInMutInd.set(curPos1, curAA1, curRot1, true); // rot
									 // index
									 // is
									 // in
									 // cur
									 // mut
									 // sequence

			if ((!eliminatedRotAtPos.get(curPos1, curAA1, curRot1))) { // not
										   // already
										   // pruned

			    for (int curPos2 = curPos1 + 1; curPos2 < numMutable; curPos2++) {

				int str2 = mutRes2Strand[curPos2];
				int strResNum2 = strandMut[str2][mutRes2MutIndex[curPos2]];
				for (int AA2 = 0; AA2 < numAAtypes[curPos2]; AA2++) {

				    int curAA2 = strandRot[str2]
					    .getIndexOfNthAllowable(strResNum2,
						    AA2);

				    // find how many rotamers are allowed for
				    // the current AA type at the given residue;
				    // note that ala and gly have 0 possible
				    // rotamers
				    int numRotForCurAAatPos2 = strandRot[str2].rl
					    .getNumRotForAAtype(curAA2);
				    if (numRotForCurAAatPos2 == 0) // ala or gly
					numRotForCurAAatPos2 = 1;

				    for (int curRot2 = 0; curRot2 < numRotForCurAAatPos2; curRot2++) {

					Index3 index2 = new Index3(curPos2,
						curAA2, curRot2);// curPos2*numTotalRot
								 // +
								 // rotIndOffset[curAA2]
								 // + curRot2;
					rotInMutInd.set(index2, true); // rot
								       // index
								       // is in
								       // cur
								       // mut
								       // sequence

					if ((!eliminatedRotAtPos.get(index2))) { // not
										 // already
										 // pruned

					    if (!splitFlags[curPos1][curAA1][curRot1][curPos2][curAA2][curRot2]) {// pair
														  // not
														  // already
														  // flagged

						// logPS.println((curPos*numTotalRot
						// + rotIndOffset[curAA] +
						// curRot));logPS.flush();
						curEc = RotamerSearch
							.getShellShellE(pairwiseMinEnergyMatrix);// pairwiseMinEnergyMatrix[pairwiseMinEnergyMatrix.length-1][0][0][0][0][0];
												 // //initialize
												 // to
												 // Et'
												 // for
												 // each
												 // rotamer

						CanEliminate(curPos1, curAA1,
							curRot1, curPos2,
							curAA2, curRot2);
						// KER: Changing from 2D to 6D
						// array to save memory space
						// pairEc[index1][index2] =
						// Math.min(pairEc[index1][index2],
						// curEc); //update the lowest
						// energy bound if necessary
						pairEc[curPos1][curAA1][curRot1][curPos2][curAA2][curRot2] = Math
							.min(curEc,
								pairEc[curPos1][curAA1][curRot1][curPos2][curAA2][curRot2]);
						// pairEc[index2][index1] =
						// pairEc[index1][index2];
						pairEc[curPos2][curAA2][curRot2][curPos1][curAA1][curRot1] = pairEc[curPos1][curAA1][curRot1][curPos2][curAA2][curRot2];
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		// System.out.println("done");
	    }

	    // If there is a ligand, compute MinDEE for the lig rotamers as well
	    /*
	     * if (numLigRot!=0){ System.out.print("Starting ligand run");
	     * System.out.print(".."); for (int curRot=0; curRot<numLigRot;
	     * curRot++){
	     * 
	     * numRotForMut++;
	     * 
	     * int index1 = numSiteResidues*numTotalRot+curRot;
	     * rotInMutInd[index1] = true; //rot index is in cur mut sequence
	     * 
	     * if ((!eliminatedRotAtPos[index1])){ //not already pruned
	     * 
	     * for (int curPos2=0; curPos2<numSiteResidues; curPos2++){
	     * 
	     * for (int AA2=0; AA2<numAAtypes[curPos2]; AA2++){
	     * 
	     * int curAA2 =
	     * sysLR.getIndexOfNthAllowable(residueMap[curPos2],AA2);
	     * 
	     * //find how many rotamers are allowed for the current AA type at
	     * the given residue; //note that ala and gly have 0 possible
	     * rotamers int numRotForCurAAatPos2 =
	     * rl.getNumRotForAAtype(curAA2); if (numRotForCurAAatPos2==0) //ala
	     * or gly numRotForCurAAatPos2 = 1;
	     * 
	     * for(int curRot2=0; curRot2<numRotForCurAAatPos2; curRot2++){
	     * 
	     * int index2 = curPos2*numTotalRot + rotIndOffset[curAA2] +
	     * curRot2; rotInMutInd[index2] = true; //rot index is in cur mut
	     * sequence
	     * 
	     * if ((!eliminatedRotAtPos[index2])){ //not already pruned
	     * 
	     * if (!splitFlags[index1][index2]) {//pair not already flagged
	     * 
	     * curEc =
	     * pairwiseMinEnergyMatrix[pairwiseMinEnergyMatrix.length-1][
	     * 0][0][0][0][0]; //initialize to Et' for each rotamer
	     * 
	     * CanEliminateLig(curRot, curPos2, curAA2, curRot2);
	     * pairEc[index1][index2] = Math.min(pairEc[index1][index2], curEc);
	     * //update the lowest energy bound if necessary
	     * pairEc[index2][index1] = pairEc[index1][index2]; } } } } } } }
	     * System.out.println("done"); }
	     */

	    // Determine the pruned rotamers: all rotamers whose lower energy
	    // bound is above the given
	    // cutoff value are pruned
	    double minE = bigE;
	    double maxE = -bigE;
	    double minEc = bigE;
	    int numPairs = 0;
	    int numPrunedPairs = 0;

	    // KER: now we have to loop through 6 levels instead of two
	    // for (int i=0; i<pairEc.length; i++){
	    // for (int j=i+1; j<pairEc.length; j++){
	    // loop
	    // KER: indices should be position1, aminoAcid1, rotamer1,
	    // KER: position2, aminoAcid2, rotamer2
	    try {
		for (int p1 = 0; p1 < pairEc.length; p1++) {
		    if (pairEc[p1] != null)
			for (int a1 = 0; a1 < pairEc[p1].length; a1++) {
			    if (pairEc[p1][a1] != null)
				for (int r1 = 0; r1 < pairEc[p1][a1].length; r1++) {
				    if (pairEc[p1][a1][r1] != null)
					for (int p2 = 0; p2 < pairEc[p1][a1][r1].length; p2++) {
					    if (pairEc[p1][a1][r1][p2] != null)
						for (int a2 = 0; a2 < pairEc[p1][a1][r1][p2].length; a2++) {
						    if (pairEc[p1][a1][r1][p2][a2] != null)
							for (int r2 = 0; r2 < pairEc[p1][a1][r1][p2][a2].length; r2++) {
							    // KER: index 1
							    Index3 i = new Index3(
								    p1, a1, r1);// p1*numTotalRot
										// +
										// rotIndOffset[a1]
										// +
										// r1;
							    // KER: index 2
							    Index3 j = new Index3(
								    p2, a2, r2);// p2*numTotalRot
										// +
										// rotIndOffset[a2]
										// +
										// r2;
							    if ((rotInMutInd
								    .get(i))
								    && (rotInMutInd
									    .get(j))
								    && ((p1) != (p2))) { // rot
											 // indices
											 // in
											 // current
											 // mutation
											 // and
											 // at
											 // different
											 // residue
											 // positions

								if ((!eliminatedRotAtPos
									.get(i))
									&& (!eliminatedRotAtPos
										.get(j))) { // not
											    // already
											    // pruned

								    if (i != j) {

									numPairs++;

									if (!splitFlags[p1][a1][r1][p2][a2][r2]) { // pair
														   // not
														   // already
														   // flagged

									    if (pairEc[p1][a1][r1][p2][a2][r2] > pruningE
										    + Ew) { // higher
											    // than
											    // the
											    // cutoff
											    // energy,
											    // so
											    // prune
										splitFlags[p1][a1][r1][p2][a2][r2] = true;
										splitFlags[p2][a2][r2][p1][a1][r1] = true;
										numPrunedPairs++;
										prunedCurRun++;

										minE = Math
											.min(minE,
												pairEc[p1][a1][r1][p2][a2][r2]);
										maxE = Math
											.max(maxE,
												pairEc[p1][a1][r1][p2][a2][r2]);

										minEc = Math
											.min(minEc,
												pairEc[p1][a1][r1][p2][a2][r2]);
									    }
									} else
									    // pair
									    // already
									    // pruned
									    minEc = Math
										    .min(minEc,
											    pairEc[p1][a1][r1][p2][a2][r2]);
								    }
								}
							    }
							}
						}
					}
				}
			}
		}
	    } catch (Exception e) {
		System.out.println("");
	    }
	    System.out.println("Number of pairs pruned this run: "
		    + prunedCurRun);
	    System.out.println();

	    if (prunedCurRun == 0) // no rotamers pruned this run, so done
		done = true;
	    else
		numRuns++;

	    Ec = Math.min(Ec, minEc); // the minimum pruned lower bound (among
				      // all pruned pairs, not just from
				      // BoundFlags)

	    System.out.println("minE: " + minE + " maxE: " + maxE
		    + " pruningE: " + pruningE + " Ew: " + Ew + " Ec: " + Ec);
	    System.out.println("Number of pruned pairs: " + numPrunedPairs);
	    System.out.println();
	    /*
	     * for (int i=0; i<indBounds.length; i++){ if
	     * (rotInMutInd[indBounds[i].index]){
	     * System.out.print("rank: "+i+" rotIndex: "
	     * +indBounds[i].index+" minBound: "+indBounds[i].Ec);
	     * System.out.println
	     * (" pruned: "+eliminatedRotAtPos[indBounds[i].index]); } }
	     */
	}

	return splitFlags;
    }

    // Called only by ComputeEliminatedRotConf(.)
    private void CanEliminate(int posNum1, int AANumAtPos1, int rotNumAtPos1,
	    int posNum2, int AANumAtPos2, int rotNumAtPos2) {

	double minIndVoxelE_ir, minIndVoxelE_js;
	double minShellResE_ir, minShellResE_js;

	minIndVoxelE_ir = pairwiseMinEnergyMatrix[posNum1][AANumAtPos1][rotNumAtPos1][posNum1][0][0]; // formula
												      // term
												      // 1
	minShellResE_ir = RotamerSearch.getShellRotE(pairwiseMinEnergyMatrix,
		posNum1, AANumAtPos1, rotNumAtPos1);// pairwiseMinEnergyMatrix[posNum1][AANumAtPos1][rotNumAtPos1][posNum1][0][1];

	minIndVoxelE_js = pairwiseMinEnergyMatrix[posNum2][AANumAtPos2][rotNumAtPos2][posNum2][0][0]; // formula
												      // term
												      // 1
	minShellResE_js = RotamerSearch.getShellRotE(pairwiseMinEnergyMatrix,
		posNum2, AANumAtPos2, rotNumAtPos2);// pairwiseMinEnergyMatrix[posNum2][AANumAtPos2][rotNumAtPos2][posNum2][0][1];

	curEc += (minIndVoxelE_ir + minShellResE_ir)
		+ (minIndVoxelE_js + minShellResE_js);// System.out.println(++count+" "+curEc);

	if (curEc >= stericE) // rotamer incompatible with template, so prune
	    return;

	curEc += indInt[posNum1][posNum2]; // formula term 3
	curEc += pairInt[posNum1][posNum2]; // formula term 5
	SumMinDiffPVE(posNum1, AANumAtPos1, rotNumAtPos1, posNum2, AANumAtPos2,
		rotNumAtPos2); // formula term 4
    }

    // //////////////////////////////////////////////////////////////////////

    // Called only by CanEliminate(.)
    private void SumMinDiffPVE(int atPos1, int withAA1, int withRot1,
	    int atPos2, int withAA2, int withRot2) {

	// get the contribution from the active site residue rotamers
	curEc += pairwiseMinEnergyMatrix[atPos1][withAA1][withRot1][atPos2][withAA2][withRot2];
	for (int curPos = 0; curPos < numMutable; curPos++) {

	    if ((curPos != atPos1) && (curPos != atPos2)) {
		IndMinDiffPVE(atPos1, withAA1, withRot1, curPos);
		IndMinDiffPVE(atPos2, withAA2, withRot2, curPos);
	    }
	}

	/*
	 * if (numLigRot!=0){ //there is a ligand //add the contribution from
	 * the ligand rotamers: there is only one ligand residue, //so there is
	 * only one position j here for which to add LigandIndMinMinPVE(atPos1,
	 * withAA1, withRot1); LigandIndMinMinPVE(atPos2, withAA2, withRot2); }
	 */
    }

    // Called by SumMinMinPVE(.)
    private void IndMinDiffPVE(int firstPos, int firstAA, int firstRot1,
	    int secondPos) {

	double curEmin;

	Index3 index1, index2;
	int numRotForAAatPos;

	int str2 = mutRes2Strand[secondPos];
	int strResNum2 = strandMut[str2][mutRes2MutIndex[secondPos]];

	// r at i
	index1 = new Index3(firstPos, firstAA, firstRot1);// firstPos*numTotalRot
							  // +
							  // rotIndOffset[firstAA]
							  // + firstRot1;

	if ((!eliminatedRotAtPos.get(index1))) { // not pruned

	    // find the minimum E among all the rotamers (all the rotamers for
	    // the given AA assignment)
	    // for the given residue
	    for (int AA = 0; AA < numAAtypes[secondPos]; AA++) {

		int curAA = strandRot[str2].getIndexOfNthAllowable(strResNum2,
			AA);

		numRotForAAatPos = strandRot[str2].rl.getNumRotForAAtype(curAA);
		if (numRotForAAatPos == 0) // ala or gly
		    numRotForAAatPos = 1;

		for (int curRot = 0; curRot < numRotForAAatPos; curRot++) {

		    // s at j
		    index2 = new Index3(secondPos, curAA, curRot);// secondPos*numTotalRot
								  // +
								  // rotIndOffset[curAA]
								  // + curRot;

		    if ((!eliminatedRotAtPos.get(index2))) { // not pruned

			if ((!useFlags)
				|| (!splitFlags[firstPos][firstAA][firstRot1][secondPos][curAA][curRot])) { // not
													    // using
													    // split
													    // flags
													    // or
													    // not
													    // flagged

			    curEmin = pairwiseMinEnergyMatrix[firstPos][firstAA][firstRot1][secondPos][curAA][curRot];
			    minEmin = Math.min(minEmin, curEmin);
			}
		    }
		}
	    }
	    curEc += minEmin;// System.out.println(++count+" "+curEc+" "+minEmin);
	}
	minEmin = bigE; // re-initialize
    }

    // Called by SumMinMinPVE(.)
    /*
     * private void LigandIndMinMinPVE(int firstPos, int firstAA, int
     * firstRot1){
     * 
     * double curEmin;
     * 
     * int index1, index2;
     * 
     * //r at i index1 = firstPos*numTotalRot + rotIndOffset[firstAA] +
     * firstRot1;
     * 
     * if ((!eliminatedRotAtPos[index1])){ //not pruned
     * 
     * for (int curLigPos=0; curLigPos<numLigRot; curLigPos++){
     * 
     * //s at j (the ligand residue) index2 = numSiteResidues*numTotalRot +
     * curLigPos;
     * 
     * if ((!eliminatedRotAtPos[index2])){ //not pruned
     * 
     * if ((!useFlags)||(!splitFlags[index1][index2])){ //not using split flags
     * or not flagged
     * 
     * curEmin =
     * pairwiseMinEnergyMatrix[firstPos][firstAA][firstRot1][numSiteResidues
     * ][ligAANum][curLigPos]; minEmin = Math.min(minEmin,curEmin); } } } curEc
     * += minEmin;//System.out.println(++count+" "+curEc); } minEmin = bigE; }
     */
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Called only by CanEliminate(.)
    private void SumMaxIndInt(int withoutPos1, int withoutPos2) {

	// get the contribution from the active site residue rotamers
	for (int curPos = 0; curPos < numMutable; curPos++) {
	    if ((curPos != withoutPos1) && (curPos != withoutPos2))
		MaxIndInt(curPos);
	}

	/*
	 * if (numLigRot!=0){ //ther is a ligand //get the contribution from the
	 * ligand rotamers: there is only one ligand residue, //so there is only
	 * one position j here for which to add if
	 * ((withoutPos1!=numSiteResidues)&&(withoutPos2!=numSiteResidues)) //if
	 * we are not currently checking ligand rotamers for pruning
	 * LigandMaxIndInt(); }
	 */
    }

    // Called by SumMaxIndInt(.)
    private void MaxIndInt(int atPos) {

	int numRotForAAatPos;

	int str = mutRes2Strand[atPos];
	int strResNum = strandMut[str][mutRes2MutIndex[atPos]];

	// KER: min_{s} E(j_s)
	for (int AA = 0; AA < numAAtypes[atPos]; AA++) {

	    int curAA = strandRot[str].getIndexOfNthAllowable(strResNum, AA);

	    numRotForAAatPos = strandRot[str].rl.getNumRotForAAtype(curAA);
	    if (numRotForAAatPos == 0) // ala or gly
		numRotForAAatPos = 1;

	    for (int curRot = 0; curRot < numRotForAAatPos; curRot++) {
		IndInt(atPos, curAA, curRot);
	    }
	}

	curEc += minEmin;// System.out.println(++count+" "+curEc);
	minEmin = bigE;
    }

    // Called by MaxIndInt(.)
    private void IndInt(int atPos, int atAA, int atRot) {

	// s at j
	// int index1 = atPos*numTotalRot + rotIndOffset[atAA] + atRot;

	if ((!eliminatedRotAtPos.get(atPos, atAA, atRot))) { // not pruned

	    double minE = pairwiseMinEnergyMatrix[atPos][atAA][atRot][atPos][0][0];
	    double minShell = RotamerSearch.getShellRotE(
		    pairwiseMinEnergyMatrix, atPos, atAA, atRot);// pairwiseMinEnergyMatrix[atPos][atAA][atRot][atPos][0][1];

	    minEmin = Math.min(minEmin, minE + minShell);
	}
    }

    // Called by SumMaxIndInt(.)
    /*
     * private void LigandMaxIndInt(){
     * 
     * for (int curLigPos=0; curLigPos<numLigRot; curLigPos++){
     * LigandIndInt(curLigPos); }
     * 
     * curEc += minEmin;//System.out.println(++count+" "+curEc); minEmin = bigE;
     * }
     */

    // Called by LigandMaxIndInt(.)
    /*
     * private void LigandIndInt (int ligRot){
     * 
     * //s at j (the ligand residue) int index1 = numSiteResidues*numTotalRot +
     * ligRot;
     * 
     * if ((!eliminatedRotAtPos[index1])){ //not pruned
     * 
     * double minE =
     * pairwiseMinEnergyMatrix[numSiteResidues][ligAANum][ligRot][numSiteResidues
     * ][0][0];; double minShell =
     * pairwiseMinEnergyMatrix[numSiteResidues][ligAANum
     * ][ligRot][numSiteResidues][0][1];
     * 
     * minEmin = Math.min(minEmin,minE+minShell); } }
     */
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Called only by CanEliminate(.)
    private void SumSumMaxPairInt(int withoutPos1, int withoutPos2) {

	// get the contribution from the active site residue rotamers
	for (int curPos1 = 0; curPos1 < numMutable; curPos1++) {
	    if ((curPos1 != withoutPos1) && (curPos1 != withoutPos2)) {
		for (int curPos2 = 0; curPos2 < curPos1; curPos2++) {
		    if ((curPos2 != withoutPos1) && (curPos2 != withoutPos2)) {
			MaxPairInt(curPos1, curPos2);
		    }
		}
	    }
	}

	/*
	 * if (numLigRot!=0){ //there is a ligand //get the contribution from
	 * the ligand rotamers: there is only one ligand residue, //so there is
	 * only one position k here for which to add; //the range of j is the
	 * number of active site residues if
	 * ((withoutPos1!=numSiteResidues)&&(withoutPos2!=numSiteResidues)){
	 * //if we are not currently checking ligand rotamers for pruning for
	 * (int curPos=0; curPos<numSiteResidues; curPos++){ if ((curPos !=
	 * withoutPos1)&&(curPos != withoutPos2)){ LigandMaxPairInt(curPos); } }
	 * } }
	 */
    }

    // Called by SumSumMaxPairInt(.)
    private void MaxPairInt(int atPos1, int atPos2) {

	int numRotForAAatPos1;

	int str1 = mutRes2Strand[atPos1];
	int strResNum1 = strandMut[str1][mutRes2MutIndex[atPos1]];
	int str2 = mutRes2Strand[atPos2];
	int strResNum2 = strandMut[str2][mutRes2MutIndex[atPos2]];

	// KER: min_{s,u} E(j_s,k_u)
	for (int AA1 = 0; AA1 < numAAtypes[atPos1]; AA1++) {

	    int curAA1 = strandRot[str1]
		    .getIndexOfNthAllowable(strResNum1, AA1);

	    numRotForAAatPos1 = strandRot[str1].rl.getNumRotForAAtype(curAA1);
	    if (numRotForAAatPos1 == 0) // ala or gly
		numRotForAAatPos1 = 1;

	    for (int curRot1 = 0; curRot1 < numRotForAAatPos1; curRot1++) {

		int numRotForAAatPos2;

		for (int AA2 = 0; AA2 < numAAtypes[atPos2]; AA2++) {

		    int curAA2 = strandRot[str2].getIndexOfNthAllowable(
			    strResNum2, AA2);
		    ;

		    numRotForAAatPos2 = strandRot[str2].rl
			    .getNumRotForAAtype(curAA2);
		    if (numRotForAAatPos2 == 0) // ala or gly
			numRotForAAatPos2 = 1;

		    for (int curRot2 = 0; curRot2 < numRotForAAatPos2; curRot2++) {
			PairInt(atPos1, curAA1, curRot1, atPos2, curAA2,
				curRot2);
		    }
		}
	    }
	}

	curEc += minEmin;// System.out.println(++count+" "+curEc);
	minEmin = bigE;
    }

    // Called by MaxPairInt(.)
    // KER: min E(j_s,k_u)
    private void PairInt(int atPos1, int atAA1, int atRot1, int atPos2,
	    int atAA2, int atRot2) {

	Index3 index1 = new Index3(atPos1, atAA1, atRot1);// atPos1*numTotalRot
							  // +
							  // rotIndOffset[atAA1]
							  // + atRot1;//u at k
	Index3 index2 = new Index3(atPos2, atAA2, atRot2);// atPos2*numTotalRot
							  // +
							  // rotIndOffset[atAA2]
							  // + atRot2;//s at j

	if ((!eliminatedRotAtPos.get(index1))
		&& (!eliminatedRotAtPos.get(index2))) { // not pruned

	    if ((!useFlags)
		    || (!splitFlags[atPos1][atAA1][atRot1][atPos2][atAA2][atRot2])) { // not
										      // using
										      // split
										      // flags
										      // or
										      // not
										      // flagged

		double minE = pairwiseMinEnergyMatrix[atPos1][atAA1][atRot1][atPos2][atAA2][atRot2];

		minEmin = Math.min(minEmin, minE);
	    }
	}
    }

    // Called by SumSumMaxPairInt(.)
    /*
     * private void LigandMaxPairInt (int atPos){
     * 
     * int numRotForAAatPos;
     * 
     * for (int AA=0; AA<numAAtypes[atPos]; AA++){
     * 
     * int curAA = sysLR.getIndexOfNthAllowable(residueMap[atPos],AA);
     * 
     * numRotForAAatPos = rl.getNumRotForAAtype(curAA); if (numRotForAAatPos==0)
     * //ala or gly numRotForAAatPos = 1;
     * 
     * for (int curRot=0; curRot<numRotForAAatPos; curRot++){
     * 
     * for (int curLigPos=0; curLigPos<numLigRot; curLigPos++){
     * LigandPairInt(atPos, curAA, curRot, curLigPos); } } }
     * 
     * curEc += minEmin;//System.out.println(++count+" "+curEc); minEmin = bigE;
     * }
     */

    // Called by LigandMaxPairInt(.)
    /*
     * private void LigandPairInt (int atPos, int atAA, int atRot, int ligRot){
     * 
     * int index1 = numSiteResidues*numTotalRot + ligRot;//u at k (the ligand
     * residue) int index2 = atPos*numTotalRot + rotIndOffset[atAA] + atRot;//s
     * at j
     * 
     * if ((!eliminatedRotAtPos[index1])&&(!eliminatedRotAtPos[index2])){ //not
     * pruned
     * 
     * if ((!useFlags)||(!splitFlags[index1][index2])){ //not using split flags
     * or not flagged
     * 
     * double minE =
     * pairwiseMinEnergyMatrix[numSiteResidues][ligAANum][ligRot][atPos
     * ][atAA][atRot];
     * 
     * minEmin = Math.min(minEmin,minE); } } }
     */
    // /////////////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////////////
    // Same as CanEliminate(), just checks the ligand rotamers for pruning
    // Called by ComputeEliminatedRotConf()
    /*
     * private void CanEliminateLig (int curLigRot, int posNum2, int
     * AANumAtPos2, int rotNumAtPos2){
     * 
     * double minIndVoxelE_ir, minIndVoxelE_js; double minShellResE_ir,
     * minShellResE_js;
     * 
     * minIndVoxelE_ir =
     * pairwiseMinEnergyMatrix[numSiteResidues][ligAANum][curLigRot
     * ][numSiteResidues][0][0]; //formula term 1 minShellResE_ir =
     * pairwiseMinEnergyMatrix
     * [numSiteResidues][ligAANum][curLigRot][numSiteResidues][0][1];
     * 
     * minIndVoxelE_js =
     * pairwiseMinEnergyMatrix[posNum2][AANumAtPos2][rotNumAtPos2
     * ][posNum2][0][0]; //formula term 1 minShellResE_js =
     * pairwiseMinEnergyMatrix
     * [posNum2][AANumAtPos2][rotNumAtPos2][posNum2][0][1];
     * 
     * curEc += (minIndVoxelE_ir + minShellResE_ir) + (minIndVoxelE_js +
     * minShellResE_js);//System.out.println(++count+" "+curEc);
     * 
     * if (curEc>=stericE) //rotamer incompatible with template, so prune
     * return;
     * 
     * curEc += indInt[numSiteResidues][posNum2]; //formula term 3 curEc +=
     * pairInt[numSiteResidues][posNum2]; //formula term 5
     * SumMinDiffPVELig(curLigRot, posNum2, AANumAtPos2, rotNumAtPos2);
     * //formula term 4 }
     */

    // Same as SumMinDiffPVE(), just checks the ligand rotamers for pruning;
    // Called by CanEliminateLig()
    /*
     * private void SumMinDiffPVELig (int withRot1, int atPos2, int withAA2, int
     * withRot2){
     * 
     * //get the contribution from the active site residue rotamers curEc +=
     * pairwiseMinEnergyMatrix
     * [numSiteResidues][ligAANum][withRot1][atPos2][withAA2][withRot2]; for
     * (int curPos=0; curPos<numSiteResidues; curPos++){ if ((curPos != atPos2))
     * { IndMinDiffPVELig(withRot1, curPos); IndMinDiffPVE(atPos2, withAA2,
     * withRot2, curPos); } } }
     */

    // Same as IndMinDiffPVE(), just checks the ligand rotamers for pruning
    // Called by SumMinDiffPVELig()
    /*
     * private void IndMinDiffPVELig (int firstRot1, int secondPos){
     * 
     * double curEmin;
     * 
     * int index1, index2; int numRotForAAatPos;
     * 
     * //r at i index1 = numSiteResidues*numTotalRot + firstRot1;
     * 
     * if ((!eliminatedRotAtPos[index1])){ //not pruned
     * 
     * //find the minimum E among all the rotamers (all the rotamers for the
     * given AA assignment) //for the given residue for (int AA=0;
     * AA<numAAtypes[secondPos]; AA++){
     * 
     * int curAA = sysLR.getIndexOfNthAllowable(residueMap[secondPos],AA);
     * 
     * numRotForAAatPos = rl.getNumRotForAAtype(curAA); if (numRotForAAatPos==0)
     * //ala or gly numRotForAAatPos = 1;
     * 
     * for (int curRot=0; curRot<numRotForAAatPos; curRot++){
     * 
     * //There is a displacement: column 0 and row 0 have special entries, //so
     * pairwise energies start from row 1, column 1
     * 
     * //s at j index2 = secondPos*numTotalRot + rotIndOffset[curAA] + curRot;
     * 
     * if ((!eliminatedRotAtPos[index2])){ //not pruned
     * 
     * if ((!useFlags)||(!splitFlags[index1][index2])){ //not using split flags
     * or not flagged curEmin =
     * pairwiseMinEnergyMatrix[numSiteResidues][ligAANum
     * ][firstRot1][secondPos][curAA][curRot]; minEmin =
     * Math.min(minEmin,curEmin); } } } } curEc += minEmin; } minEmin = bigE; }
     */
}
