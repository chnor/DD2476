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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {
	
    /** The index as a hashtable. */
    private TreeMap<String, PostingsList> index = new TreeMap<String,PostingsList>();
	private TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
	
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
		if (!wordCounts.containsKey("" + docID))
			wordCounts.put("" + docID, 0);
		wordCounts.put("" + docID, wordCounts.get("" + docID) + 1);
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
     *  Searches the index for postings matching the query.
     */
    public Collection<PostingsEntry> search(Query query, int queryType, int rankingType, int structureType) {
		// System.out.println("Searching for " + query.terms.peek() + " among " + index.size());
		if (queryType == Index.INTERSECTION_QUERY) {
			PostingsList results = null;
			for (String term : query.terms) {
				if (results == null) {
					results = getPostings(term);
				} else {
					results = results.intersect(getPostings(term));
				}
			}
			if (results == null) return new ArrayList<PostingsEntry>();
			return results.toCollection();
		} else if (queryType == Index.PHRASE_QUERY) {
			PostingsList results = null;
			int offset = -1;
			for (String term : query.terms) {
				offset++;
				if (results == null) {
					results = getPostings(term);
				} else {
					results = results.intersect(getPostings(term), offset, offset);
				}
			}
			if (results == null) return new ArrayList<PostingsEntry>();
			return results.toCollection();
		} else if (queryType == Index.RANKED_QUERY) {
			PostingsList results = null;
			for (String term : query.terms) {
				if (results == null) {
					results = getPostings(term);
				} else {
					results = results.intersect(getPostings(term));
				}
			}
			if (results == null) return new ArrayList<PostingsEntry>();
			List<PostingsEntry> sorted = new ArrayList<PostingsEntry>(results.toCollection());
			Collections.sort(sorted, PostingsEntry.SCORE_COMPARATOR_ASCENDING);
			return sorted;
		}
		
		return null;
    }
	
    /**
     *  Marshal the index to file.
     */
	public void marshal_dump() {
		
		// TreeMap<String, Integer> dict = new TreeMap<String, Integer>();
		
		File dir = new File("store");
		dir.mkdir();
		
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
				doc_info_file.write(entry.getKey());
				doc_info_file.write(" " + entry.getValue());
				doc_info_file.write(" " + wordCounts.get(entry.getKey()));
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
