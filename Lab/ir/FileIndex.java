/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 */  

package ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

/**
 *   
 */
public class FileIndex implements Index {
	
    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
		// TODO
    }
	
    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
		return null;
		// return index.keySet().iterator();
    }
	
	private String extractLine(String key, RandomAccessFile file)
	throws IOException {
		long b_0 = 0;
		long b_1 = 0;
		
		// Find file seek of last line
		{
			// Assume file longer than 100 chars
			// and last line < 100 chars
			file.seek(file.length() - 100);
			file.readLine();
			b_1 = file.getFilePointer();
			String line = "";
			while ((line = file.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				if (tokens.length > 1 && tokens[0].equals(key)) {
					// Hit!
					return line;
				}
			}
		}
		
		while (b_1 - b_0 > 50) {
			long b_i = b_0 + (b_1 - b_0)/2;
			// System.out.println("Searching at " + b_i + " in [" + b_0 + ", " + b_1 + "]");
			file.seek(b_i);
			file.readLine(); // Seek to next
			String line = file.readLine();
			String[] tokens = line.split("\\s+");
			if (tokens.length < 1) {
				// Handle!
			}
			// System.out.println(key + " <=> " + tokens[0]);
			int comp = tokens[0].compareTo(key);
			if (comp < 0) { // token < key
				// System.out.println(key + " > " + tokens[0]);
				b_0 = b_i;
			} else if (comp > 0) { // token > key
				// System.out.println(key + " < " + tokens[0]);
				b_1 = b_i;
			} else {
				// Hit!
				return line;
			}
		}
		
		b_0 = Math.max(0, b_0-100);
		b_1 = Math.min(file.length(), b_1+100);
		
		{
			String line = "";
			// Linear search through the rest
			file.seek(b_0);
			while ((line = file.readLine()) != null && file.getFilePointer() < b_1) {
				// System.out.println(line);
				String[] tokens = line.split("\\s+");
				if (tokens.length > 1 && tokens[0].equals(key)) {
					// Hit!
					return line;
				}
			}
		}
		
		return "";
		
	}
	
    /**
     *  
     */
    public String getDocName( String docID ) {
		
		RandomAccessFile doc_info_file = null;
		
		System.out.println("Looking up filename of " + docID);
		
		try {
			// doc_info_reader = new BufferedReader(new FileReader(new File("store/doc_info")));
			doc_info_file = new RandomAccessFile(new File("store/doc_info"), "r");
			String line = extractLine(docID, doc_info_file);
			// System.out.println(line);
			String[] tokens = line.split("\\s+");
			if (tokens.length > 1) {
				return tokens[1];
			}
			
			// String line;
			// while ((line = doc_info_reader.readLine()) != null) {
				// String[] tokens = line.split("\\s+");
				// if (tokens.length > 1 && tokens[0].equals(docID)) {
					// return tokens[1];
				// }
			// }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				doc_info_file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
		
	}
	
    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
	 *  For backwards compatibility.
     */
    public PostingsList getPostings(String token) {
		return getPostings(token, false);
	}
	
    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings(String token, boolean skipPositions) {
		
		PostingsList result = new PostingsList();
		
		RandomAccessFile index_file = null;
		RandomAccessFile docs_file = null;
		RandomAccessFile pos_file = null;

		try {
			index_file 	= new RandomAccessFile(new File("store/index"), "r");
			docs_file 	= new RandomAccessFile(new File("store/docs"), "r");
			pos_file 	= new RandomAccessFile(new File("store/pos"), "r");

			// String line;
			// while ((line = index_file.readLine()) != null) {
				// String[] tokens = line.split("\\s+");
				// if (tokens.length > 1 && tokens[0].equals(token)) {
				
			String line = extractLine(token, index_file);
			String[] tokens = line.split("\\s+");
			if (tokens.length > 1) {
				// System.out.println(line);
				long doc_pos = Long.parseLong(tokens[1]);
				docs_file.seek(doc_pos);
				String docs_string = docs_file.readLine();
				// System.out.println(docs_string);
				String[] doc_ID_strings = docs_string.split("\\s+");
				for (String doc_ID_string: doc_ID_strings) {
					String[] tok2 = doc_ID_string.split(":");
					int docID = Integer.parseInt(tok2[0]);
					if (skipPositions) {
						result.add(docID, 0, 0);
					} else {
						long posRef = Long.parseLong(tok2[1]);
						pos_file.seek(posRef);
						String poss_string = pos_file.readLine();
						// System.out.println(poss_string);
						String[] pos_toks = poss_string.split("\\s+");
						for (String pos_tok : pos_toks) {
							int pos = Integer.parseInt(pos_tok);
							result.add(docID, 0, pos);
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				index_file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// for (PostingsEntry entry : result) {
			// System.out.print(entry.docID + ": ");
			// Iterator<Integer> pos_iter = entry.getPositionIterator();
			// while (pos_iter.hasNext()) {
				// int pos = pos_iter.next();
				// System.out.print("" + pos + " ");
			// }
			// System.out.println();
		// }
		
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
					results = getPostings(term, true);
				} else {
					results = intersect(results, getPostings(term, true));
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
					results = getPostings(term, false);
				} else {
					results = intersect(results, getPostings(term, false), offset, offset);
				}
			}
			if (results == null) return new PostingsList();
			return results;
		}
		
		return getPostings(query.terms.peek(), true);
    }
	
    /**
     *  
     */
    public void cleanup() {
    }
}
