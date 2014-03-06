/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  

package ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
	
    /** The index as a hashtable. */
    private TreeMap<String,PostingsList> index = new TreeMap<String,PostingsList>();
	
    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
		// System.out.println("Inserting token: " + token + " at " + offset);
		if (!index.containsKey(token)) {
			index.put(token, new PostingsList());
		}
		// index.get(token).add(new PostingsEntry(docID, 0));
		index.get(token).add(docID, 0, offset);
    }
	
	/**
     *  Lookup the document with specified ID.
     */
	public String getDocName(String docID) {
		return docIDs.get(docID);
	}
	
    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
		return index.keySet().iterator();
    }
	
    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
		PostingsList result = null;
		if (index.containsKey(token))
			result = index.get(token);
		else
			result = new PostingsList();
		for (PostingsEntry entry : result) {
			System.out.print(entry.docID + ": ");
			Iterator<Integer> pos_iter = entry.getPositionIterator();
			while (pos_iter.hasNext()) {
				int pos = pos_iter.next();
				System.out.print("" + pos + " ");
			}
			System.out.println();
		}
		return result;
    }
	
	/**
	 * Helper function.
	 */
	private PostingsList intersect(PostingsList P1, PostingsList P2) {
		PostingsList res = new PostingsList();
		// System.out.println("Intersecting");
		
		/*
		for (PostingsEntry entry : P1) {
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
				res.add(p1);
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
		// */
		/*
		PostingsEntry p1 = iter_1.next();
		PostingsEntry p2 = iter_2.next();
		while (iter_1.hasNext() && iter_2.hasNext()) {
			while (p1.docID < p2.docID) {
				// System.out.println("Comparing: " + p1.docID + " < " + p2.docID);
				if (!iter_1.hasNext()) return res;
				p1 = iter_1.next();
			}
			while (p2.docID < p1.docID) {
				// System.out.println("Comparing: " + p1.docID + " > " + p2.docID);
				if (!iter_2.hasNext()) return res;
				p2 = iter_2.next();
			}
			// System.out.println("Comparing: " + p1.docID + " == " + p2.docID);
			assert(p1.docID == p2.docID);
			res.add(p1);
			p1 = iter_1.next();
			p2 = iter_2.next();
		}
		// */
		return res;
	}
	
	/**
	 * Helper function.
	 */
	private PostingsList intersect(PostingsList P1, PostingsList P2, int k1, int k2) {
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
						res.add(p1);
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
	
    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
		// System.out.println("Searching for " + query.terms.peek() + " among " + index.size());
		if (queryType == Index.INTERSECTION_QUERY) {
			PostingsList results = null;
			for (String term : query.terms) {
				if (results == null) {
					results = getPostings(term);
				} else {
					results = intersect(results, getPostings(term));
				}
			}
			if (results == null) return new PostingsList();
			return results;
		} else if (queryType == Index.PHRASE_QUERY) {
			PostingsList results = null;
			int offset = -1;
			for (String term : query.terms) {
				offset++;
				if (results == null) {
					results = getPostings(term);
				} else {
					results = intersect(results, getPostings(term), offset, offset);
				}
			}
			if (results == null) return new PostingsList();
			return results;
		}
		return getPostings(query.terms.peek());
    }
	
    /**
     *  Marshal the index to file.
     */
	public void marshal_dump() {
		
		// TreeMap<String, Integer> dict = new TreeMap<String, Integer>();
		
		// /*
		BufferedWriter index_file = null;
		RandomAccessFile doc_file = null;
		RandomAccessFile pos_file = null;
		try {
			index_file = new BufferedWriter(new FileWriter("store/index", false));
			doc_file = new RandomAccessFile(new File("store/docs"), "rw");
			pos_file = new RandomAccessFile(new File("store/pos"),	"rw");
			
			// int offset = 0;
			int iter = 0;
			for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
				iter++;
				if (iter % 1000 == 0) {
					System.out.println("Marshalling posting for " + entry.getKey() + " to file");
					System.out.println("" + (int)(1000.0*iter/index.size()) / 10.0 + "% done");
				}
				
				index_file.write(entry.getKey() + " " + doc_file.getFilePointer() + "\n");
				// offset += 1;
				
				entry.getValue().marshalDump(doc_file, pos_file);
				
				// dict.put(token, offset);
			}
			
			// dict_out = new BufferedWriter(new FileWriter("store/dict", false));
			// for (Map.Entry<String, int> entry : dict.entrySet()) {
				// dict_out.write(entry.getKey() + ", " + entry.getValue());
			// }
			
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if(index_file != null){
				try {
					index_file.close();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
			if(doc_file != null){
				try {
					doc_file.close();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}
		
		BufferedWriter doc_info_file = null;
		try {
			doc_info_file = new BufferedWriter(new FileWriter("store/doc_info", false));
			for (Map.Entry<String, String> entry : docIDs.entrySet()) {
				doc_info_file.write(entry.getKey() + " " + entry.getValue());
				doc_info_file.write("\n");
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if(doc_info_file != null){
				try {
					doc_info_file.close();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}
		// */
		
	}
	
    /**
     *  Clean up index (persist).
     */
    public void cleanup() {
		marshal_dump();
    }
}
