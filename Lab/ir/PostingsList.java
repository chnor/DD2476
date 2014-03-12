/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable, Iterable<PostingsEntry> {
    
    /** The postings list as a linked list. */
    private TreeMap<Integer, PostingsEntry> list =
		new TreeMap<Integer, PostingsEntry>();
	
    /**  Number of postings in this list  */
    public int size() {
		return list.size();
    }
	
    /**  Returns the ith posting */
    // public PostingsEntry get( int i ) {
		// Use iterators instead...
		// return list.get( i );
    // }
	
	public Collection<PostingsEntry> toCollection() {
		return list.values();
	}
	
	public Iterator<PostingsEntry> iterator() {
		return list.values().iterator();
	}
	
	// public PostingsList() {
		// this.list = new TreeMap<PostingsEntry>();
	// }
	
	public boolean contains(PostingsEntry entry) {
		return list.containsKey(entry.docID);
	}
	
	public void add(PostingsEntry entry) {
		assert(!list.containsKey(entry.docID));
		list.put(entry.docID, entry);
	}
	
	public void add(int docID, double score, int offset) {
		if (!list.containsKey(docID))
			list.put(docID, new PostingsEntry(docID, score));
		list.get(docID).addPosition(offset);
	}
	
	public PostingsEntry get(int docID) {
		return list.get(docID);
	}
	
	public void marshalDump(RandomAccessFile doc_file, RandomAccessFile pos_file)
	throws IOException {
		for (PostingsEntry entry : this) {
			long pos_pointer = pos_file.getFilePointer();
			String dump = "" + entry.docID + ":" + pos_pointer + " ";
			doc_file.writeBytes(dump);
			Iterator<Integer> pos_iter = entry.getPositionIterator();
			while (pos_iter.hasNext()) {
				int pos = pos_iter.next();
				pos_file.writeBytes("" + pos + " ");
			}
			pos_file.writeBytes("\n");
		}
		doc_file.writeBytes("\n");
	}
	
	
	/**
	 * Take the union of this list and P2.
	 */
	public PostingsList union(PostingsList P2) {
		PostingsList P1 = this;
		PostingsList res = new PostingsList();
		
		// /*
		// Sanity:
		// ID_union is guaranteed to be the union of all
		// docIDs in P1 and P2, check for consistency!
		Set<Integer> ID_union = new HashSet<Integer>();
		for (PostingsEntry entry : P1) {
			ID_union.add(entry.docID);
		}
		for (PostingsEntry entry : P2) {
			ID_union.add(entry.docID);
		}
		// */
		
		// /* Textbook algorithm in O(n)
		Iterator<PostingsEntry> iter_1 = P1.iterator();
		Iterator<PostingsEntry> iter_2 = P2.iterator();
		PostingsEntry p1 = null;
		PostingsEntry p2 = null;
		if (iter_1.hasNext()) p1 = iter_1.next();
		if (iter_2.hasNext()) p2 = iter_2.next();
		while (p1 != null && p2 != null) {
			if (p1.docID == p2.docID) {
				res.add(p1.docID, p1.score + p2.score, 0);
				if (iter_1.hasNext()) p1 = iter_1.next();
				else p1 = null;
				if (iter_2.hasNext()) p2 = iter_2.next();
				else p2 = null;
			} else {
				if (p1.docID < p2.docID) {
					res.add(p1.docID, p1.score, 0);
					if (iter_1.hasNext()) {
						p1 = iter_1.next();
					} else {
						p1 = null;
					}
				} else {
					res.add(p2.docID, p2.score, 0);
					if (iter_2.hasNext()) {
						p2 = iter_2.next();
					} else {
						p2 = null;
					}
				}
			}
		}
		while (p2 != null) {
			res.add(p2.docID, p2.score, 0);
			if (iter_2.hasNext()) {
				p2 = iter_2.next();
			} else {
				p2 = null;
			}
		}
		while (p1 != null) {
			res.add(p1.docID, p1.score, 0);
			if (iter_1.hasNext()) {
				p1 = iter_1.next();
			} else {
				p1 = null;
			}
		}
		
		System.out.println("Expected size: " + ID_union.size());
		System.out.println("Actual size: " + res.size());
		assert ID_union.size() == res.size();
		
		/*
		for (PostingsEntry entry : P1) {
			if (P2.contains(entry)) {
				// Intersection set of P1 and P2
				double P2_score = P2.get(entry.docID).score;
				res.add(entry.docID, entry.score + P2_score, 0);
			} else {
				// Set: P1 \ P2
				res.add(entry.docID, entry.score, 0);
			}
		}
		for (PostingsEntry entry : P2) {
			if (!P1.contains(entry)) {
				// Set: P2 \ P1
				res.add(entry.docID, entry.score, 0);
			}
		}
		*/
		
		return res;
		
	}
	
	/**
	 * Take the intersection of this list and P2.
	 */
	public PostingsList intersect(PostingsList P2) {
		PostingsList P1 = this;
		PostingsList res = new PostingsList();
		// System.out.println("Intersecting");
		
		// /* Textbook algorithm in O(n)
		Iterator<PostingsEntry> iter_1 = P1.iterator();
		Iterator<PostingsEntry> iter_2 = P2.iterator();
		PostingsEntry p1 = null;
		PostingsEntry p2 = null;
		if (iter_1.hasNext()) p1 = iter_1.next();
		if (iter_2.hasNext()) p2 = iter_2.next();
		while (p1 != null && p2 != null) {
			if (p1.docID == p2.docID) {
				res.add(p1.docID, p1.score + p2.score, 0);
				if (iter_1.hasNext()) p1 = iter_1.next();
				else p1 = null;
				if (iter_2.hasNext()) p2 = iter_2.next();
				else p2 = null;
			} else {
				if (p1.docID < p2.docID) {
					if (iter_1.hasNext()) p1 = iter_1.next();
					else p1 = null;
				} else {
					if (iter_2.hasNext()) p2 = iter_2.next();
					else p2 = null;
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Take the intersection of this list with P2 with an offset
	 * window between [k1, k2] (both limits inclusive).
	 */
	public PostingsList intersect(PostingsList P2, int k1, int k2) {
		PostingsList P1 = this;
		PostingsList res = new PostingsList();
		System.out.println("Intersecting in window [" + k1 + ", " + k2 + "]");
		
		Iterator<PostingsEntry> iter_1 = P1.iterator();
		Iterator<PostingsEntry> iter_2 = P2.iterator();
		PostingsEntry p1 = null;
		PostingsEntry p2 = null;
		if (iter_1.hasNext()) p1 = iter_1.next();
		if (iter_2.hasNext()) p2 = iter_2.next();
		while (p1 != null && p2 != null) {
			if (p1.docID == p2.docID) {
				
				System.out.println("Checking document " + p1.docID);
				
				Iterator<Integer> pos_iter_1 = p1.getPositionIterator();
				Iterator<Integer> pos_iter_2 = p2.getPositionIterator();
				int pos_1 = -1;
				int pos_2 = -1;
				if (pos_iter_1.hasNext()) pos_1 = pos_iter_1.next();
				if (pos_iter_2.hasNext()) pos_2 = pos_iter_2.next();
				while (pos_1 != -1 && pos_2 != -1) {
					int w1 = pos_1 + k1;
					int w2 = pos_1 + k2;
					System.out.println("Searching for " + pos_2 + " in window [" + w1 + ", " + w2 + "]");
					if ( w1 <= pos_2 && pos_2 <= w2 ) {
						res.add(p1.docID, p1.score + p2.score, 0);
						// if (pos_iter_1.hasNext()) pos_1 = pos_iter_1.next();
						// else pos_1 = -1;
						// if (pos_iter_2.hasNext()) pos_2 = pos_iter_2.next();
						// else pos_2 = -1;
						break;
					} else if ( w1 > pos_2 ) {
						if (pos_iter_2.hasNext()) pos_2 = pos_iter_2.next();
						else pos_2 = -1;
					} else if ( w2 < pos_2 ) {
						if (pos_iter_1.hasNext()) pos_1 = pos_iter_1.next();
						else pos_1 = -1;
					}
				}
				
				if (iter_1.hasNext()) p1 = iter_1.next();
				else p1 = null;
				if (iter_2.hasNext()) p2 = iter_2.next();
				else p2 = null;
			} else {
				if (p1.docID < p2.docID) {
					if (iter_1.hasNext()) p1 = iter_1.next();
					else p1 = null;
				} else {
					if (iter_2.hasNext()) p2 = iter_2.next();
					else p2 = null;
				}
			}
		}
		return res;
	}
	
}