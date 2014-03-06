/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
	public static final Comparator<PostingsEntry> DOC_ID_COMPARATOR =
		new Comparator<PostingsEntry>() {
			public int compare(PostingsEntry p1, PostingsEntry p2) {
				return p1.docID - p2.docID;
			}
		};
	
    public int docID;
    public double score;
	private TreeSet<Integer> positions = new TreeSet<Integer>();
	
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
		return Double.compare( other.score, score );
    }
	
	public PostingsEntry(int docID, double score) {
		this.docID = docID;
		this.score = score;
	}
	
	public void addPosition(int pos) {
		positions.add(pos);
	}
	
	public Iterator<Integer> getPositionIterator() {
		return positions.iterator();
	}
	
    //
    //  YOUR CODE HERE
    //

}
