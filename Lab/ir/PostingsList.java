/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
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
	
	public Iterator<PostingsEntry> iterator() {
		return list.values().iterator();
	}
	
	// public PostingsList() {
		// this.list = new TreeMap<PostingsEntry>();
	// }
	
	public void add(PostingsEntry entry) {
		assert(!list.containsKey(entry.docID));
		list.put(entry.docID, entry);
	}
	
	public void add(int docID, int score, int offset) {
		if (!list.containsKey(docID))
			list.put(docID, new PostingsEntry(docID, score));
		list.get(docID).addPosition(offset);
	}
	
	// public PostingsEntry get(int docID) {
		// return list.get(docID);
	// }
	
	public void marshalDump(RandomAccessFile doc_file, RandomAccessFile pos_file)
	throws IOException {
		// int offset = 0;
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
			
			// offset += dump.length();
		}
		doc_file.writeBytes("\n");
		// offset += 1;
		// return offset;
	}
	
    //
    //  YOUR CODE HERE
    //
}