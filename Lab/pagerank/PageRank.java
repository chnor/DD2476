/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

package pagerank;

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


	/**
	 * Generate starting probabilities randomly.
	 */
	private double[] randomStart(int numberOfDocs) {
		double[] pi = new double[numberOfDocs];
		Random RNG = new Random(System.currentTimeMillis());
		{
			double total = 0;
			for (int i = 0; i < numberOfDocs; i++) {
				pi[i] = RNG.nextDouble();
				total += pi[i];
			}
			for (int i = 0; i < numberOfDocs; i++) {
				pi[i] /= total;
			}
		}
		return pi;
	}
	
	private void normalize(double[] pi) {
		double total = 0;
		for (int i = 0; i < pi.length; i++) {
			total += pi[i];
		}
		for (int i = 0; i < pi.length; i++) {
			pi[i] /= total;
		}
	}
	
	private double[] powerIteration(double[] pi, int maxIter, double eps) {
		
		double[] pi_new = new double[pi.length];
		System.arraycopy(pi, 0, pi_new, 0, pi.length);
		
		double pi_diff = 1.0;
		
		for (int iter = 0; iter < maxIter && pi_diff > eps; iter++) {
			System.out.println("Iteration " + iter+1);
			Arrays.fill(pi_new, 0);
			for (int i = 0; i < pi.length; i++) {
				for (int j = 0; j < pi.length; j++) {
					double P_ij = BORED / pi.length;
					if (link.get(i) != null && link.get(i).get(j) != null) {
						P_ij += 1.0 / out[i];
					}
					// System.out.println("" + i + " -> " + j);
					pi_new[j] += pi[i] * P_ij;
				}
			}
			
			pi_diff = 0.0;
			for (int i = 0; i < pi.length; i++) {
				pi_diff += Math.abs(pi[i] - pi_new[i]);
			}
			
			System.arraycopy(pi_new, 0, pi, 0, pi.length);
			
			/*
			double total = 0;
			for (int i = 0; i < pi.length; i++) {
				total += pi[i];
			}
			for (int i = 0; i < pi.length; i++) {
				pi[i] /= total;
			}
			// */
			
		}
		
		return pi;
	}
	
	private double[] approximation_1(double[] pi, int maxIter, double eps) {
		
		double[] pi_new = new double[pi.length];
		System.arraycopy(pi, 0, pi_new, 0, pi.length);
		
		double pi_diff = 1.0;
		
		for (int iter = 0; iter < maxIter && pi_diff > eps; iter++) {
			Arrays.fill(pi_new, 0);
			for (int i = 0; i < pi.length; i++) {
				if (out[i] > 0) {
					for (Integer j : link.get(i).keySet()) {
						double P_ij = (1-BORED) / out[i];
						pi_new[j] += pi[i] * P_ij;
					}
				}
				pi_new[i] += BORED / pi.length;
				pi_new[i] += numberOfSinks / pi.length / pi.length;
			}
			
			pi_diff = 0.0;
			for (int i = 0; i < pi.length; i++) {
				pi_diff += Math.abs(pi[i] - pi_new[i]);
			}
			
			System.arraycopy(pi_new, 0, pi, 0, pi.length);
			
		}
		
		return pi;
	}
	
	private double[] monte_carlo_1(int numberOfDocs, int maxIter) {
		
		int[] counts = new int[numberOfDocs];
		
		Random RNG = new Random(System.currentTimeMillis());
		
		double next_target = 0.0;
		for (int iter = 0; iter < maxIter; iter++) {
			if (iter > next_target*maxIter) {
				System.out.println("" + Math.round(100*next_target) + "% done");
				next_target += 0.1;
			}
			int i = RNG.nextInt(numberOfDocs);
			// System.out.println("Starting i:" + i);
			while (RNG.nextDouble() > BORED) {
				if (out[i] > 0) {
					Integer[] outlinks = link.get(i).keySet().toArray(new Integer[0]);
					int next_i = RNG.nextInt(outlinks.length);
					// System.out.println("Next i:" + next_i);
					i = outlinks[next_i];
				} else {
					i = RNG.nextInt(numberOfDocs);
				}
			}
			counts[i]++;
			
		}
		
		double[] pi = new double[numberOfDocs];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1.0 * counts[i] / maxIter;
		}
		normalize(pi);
		return pi;
	}
	
	private double[] monte_carlo_2(int numberOfDocs, int maxIter) {
		
		int[] counts = new int[numberOfDocs];
		
		Random RNG = new Random(System.currentTimeMillis());
		
		double next_target = 0.0;
		for (int iter = 0; iter < maxIter; iter++) {
			if (iter > next_target*maxIter) {
				System.out.println("" + Math.round(100*next_target) + "% done");
				next_target += 0.1;
			}
			for (int start_i = 0; start_i < numberOfDocs; start_i++) {
				int i = start_i;
				// System.out.println("Starting i:" + i);
				while (RNG.nextDouble() > BORED) {
					if (out[i] > 0) {
						Integer[] outlinks = link.get(i).keySet().toArray(new Integer[0]);
						int next_i = RNG.nextInt(outlinks.length);
						// System.out.println("Next i:" + next_i);
						i = outlinks[next_i];
					} else {
						i = RNG.nextInt(numberOfDocs);
					}
				}
				counts[i]++;
			}
		}
		
		double[] pi = new double[numberOfDocs];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1.0 * counts[i] / maxIter;
		}
		normalize(pi);
		return pi;
	}
	
	private double[] monte_carlo_3(int numberOfDocs, int maxIter) {
		
		int[] counts = new int[numberOfDocs];
		
		Random RNG = new Random(System.currentTimeMillis());
		
		double next_target = 0.0;
		for (int iter = 0; iter < maxIter; iter++) {
			if (iter > next_target*maxIter) {
				System.out.println("" + Math.round(100*next_target) + "% done");
				next_target += 0.1;
			}
			for (int start_i = 0; start_i < numberOfDocs; start_i++) {
				int i = start_i;
				// System.out.println("Starting i:" + i);
				while (RNG.nextDouble() > BORED) {
					if (out[i] > 0) {
						Integer[] outlinks = link.get(i).keySet().toArray(new Integer[0]);
						int next_i = RNG.nextInt(outlinks.length);
						// System.out.println("Next i:" + next_i);
						i = outlinks[next_i];
					} else {
						i = RNG.nextInt(numberOfDocs);
					}
					counts[i]++;
				}
			}
		}
		
		double[] pi = new double[numberOfDocs];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1.0 * counts[i] / maxIter;
		}
		normalize(pi);
		return pi;
	}
	
	private double[] monte_carlo_4(int numberOfDocs, int maxIter) {
		
		int[] counts = new int[numberOfDocs];
		
		Random RNG = new Random(System.currentTimeMillis());
		
		double next_target = 0.0;
		for (int iter = 0; iter < maxIter; iter++) {
			if (iter > next_target*maxIter) {
				System.out.println("" + Math.round(100*next_target) + "% done");
				next_target += 0.1;
			}
			for (int start_i = 0; start_i < numberOfDocs; start_i++) {
				int i = start_i;
				// System.out.println("Starting i:" + i);
				while (RNG.nextDouble() > BORED) {
					if (out[i] > 0) {
						Integer[] outlinks = link.get(i).keySet().toArray(new Integer[0]);
						int next_i = RNG.nextInt(outlinks.length);
						// System.out.println("Next i:" + next_i);
						i = outlinks[next_i];
					} else {
						break;
					}
					counts[i]++;
				}
			}
		}
		
		double[] pi = new double[numberOfDocs];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1.0 * counts[i] / maxIter;
		}
		normalize(pi);
		return pi;
	}
	
	private double[] monte_carlo_5(int numberOfDocs, int maxIter) {
		
		int[] counts = new int[numberOfDocs];
		
		Random RNG = new Random(System.currentTimeMillis());
		
		double next_target = 0.0;
		for (int iter = 0; iter < maxIter; iter++) {
			if (iter > next_target*maxIter) {
				System.out.println("" + Math.round(100*next_target) + "% done");
				next_target += 0.1;
			}
			int i = RNG.nextInt(numberOfDocs);
			// System.out.println("Starting i:" + i);
			while (RNG.nextDouble() > BORED) {
				if (out[i] > 0) {
					Integer[] outlinks = link.get(i).keySet().toArray(new Integer[0]);
					int next_i = RNG.nextInt(outlinks.length);
					// System.out.println("Next i:" + next_i);
					i = outlinks[next_i];
				} else {
					break;
				}
				counts[i]++;
			}
			
		}
		
		double[] pi = new double[numberOfDocs];
		for (int i = 0; i < pi.length; i++) {
			pi[i] = 1.0 * counts[i] / maxIter;
		}
		normalize(pi);
		return pi;
	}
	
	private void printHighest(double[] pi, int nToPrint) {
		
		ArrayList<Map.Entry<Integer, Double> > pi_res = new ArrayList<Map.Entry<Integer, Double> >();
		for (int i = 0; i < pi.length; i++) {
			pi_res.add(new AbstractMap.SimpleEntry<Integer, Double>(i, pi[i]));
		}
		Collections.sort(pi_res, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> p1, Map.Entry<Integer, Double> p2) {
				if (p1.getValue() > p2.getValue()) {
					return -1;
				} else if (p1.getValue() < p2.getValue()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		int i = 0;
		for (Map.Entry<Integer, Double> entry : pi_res) {
			i++;
			System.out.println("" + i + ":\t" + docName[entry.getKey()] + "\t" + Math.round(1E5*entry.getValue())/1E5);
			if (i >= nToPrint) break;
		}
		
	}
	
    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
		
		double[] pi = null;
		// double[] pi = randomStart(numberOfDocs);
		// pi = powerIteration(pi, 10, EPSILON);
		// pi = approximation_1(pi, 20, EPSILON);
		
		// pi = monte_carlo_1(numberOfDocs, 100*10000);
		// pi = monte_carlo_2(numberOfDocs, 100);
		// pi = monte_carlo_3(numberOfDocs, 100);
		// pi = monte_carlo_4(numberOfDocs, 100);
		pi = monte_carlo_5(numberOfDocs, 100*10000);
		
		printHighest(pi, 50);
		
    }

	
    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
