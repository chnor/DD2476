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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

/**
 *   
 */
public class FileIndex implements Index {
	
	private static class DocInfo {
		public String filename;
		public int wordCount;
		public DocInfo(String filename, int wordCount) {
			this.filename = filename;
			this.wordCount = wordCount;
		}
	}
	
	private int totalNumberOfDocuments = -1;
	private HashMap<String, DocInfo> docInfo = new HashMap<String, DocInfo>();
	
	public FileIndex() {
		super();
		
		BufferedReader doc_info_reader = null;
		
		try {
			doc_info_reader = new BufferedReader(new FileReader(new File("store/doc_info")));
			String line;
			int docCount = 0;
			while ((line = doc_info_reader.readLine()) != null) {
				docCount++;
				String[] tokens = line.split("\\s+");
				String docId = tokens[0];
				String filename = tokens[1];
				int wordCount = Integer.parseInt(tokens[2]);
				docInfo.put(docId, new DocInfo(filename, wordCount));
			}
			totalNumberOfDocuments = docCount;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				doc_info_reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
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
	
	/*
	private int retrieveTotalNumberOfDocuments() {
		
		// Use a mutex!
		synchronized(this) {
			if (totalNumberOfDocuments == -1) {
				// Not set. Look it up
				BufferedReader doc_info_file = null;
				System.out.println("Looking up number of documents...");
				try {
					doc_info_file = new BufferedReader(new FileReader(new File("store/doc_info")));
					int count = 0;
					String line;
					while ((line = doc_info_file.readLine()) != null) {
						count++;
					}
					System.out.println("Done!");
					totalNumberOfDocuments = count;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						doc_info_file.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return totalNumberOfDocuments;
		}
	}
	*/
	
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
		
		return docInfo.get(docID).filename;
		
		/*
		RandomAccessFile doc_info_file = null;
		
		System.out.println("Looking up filename of " + docID);
		
		try {
			doc_info_file = new RandomAccessFile(new File("store/doc_info"), "r");
			String line = extractLine(docID, doc_info_file);
			String[] tokens = line.split("\\s+");
			if (tokens.length > 1) {
				return tokens[1];
			}
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
		*/
	}
	
    /**
     *  
     */
    public int getDocWordCount( String docID ) {
		
		return docInfo.get(docID).wordCount;
		
		/*
		RandomAccessFile doc_info_file = null;
		
		System.out.println("Looking up word count of document: " + docID);
		
		try {
			doc_info_file = new RandomAccessFile(new File("store/doc_info"), "r");
			String line = extractLine(docID, doc_info_file);
			String[] tokens = line.split("\\s+");
			if (tokens.length > 2) {
				return Integer.parseInt(tokens[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				doc_info_file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return -1;
		*/
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
							
							int tf_dt = pos_toks.length;
							int N = totalNumberOfDocuments;
							int df_t = doc_ID_strings.length;
							int len_d = getDocWordCount(tok2[0]);
							
							double idf_t = Math.log(1.0*N/df_t);
							double tf_idf_dt = 1.0 * tf_dt * idf_t / len_d;
							// System.out.println("tf_dt: " + tf_dt);
							// System.out.println("N: " + N);
							// System.out.println("df_t: " + df_t);
							// System.out.println("len_d: " + len_d);
							// System.out.println("Score: " + tf_idf_dt);
							
							result.add(docID, tf_idf_dt, pos);
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
     *  Searches the index for postings matching the query.
     */
    public Collection<PostingsEntry> search(Query query, int queryType, int rankingType, int structureType) {
		// System.out.println("Searching for " + query.terms.peek() + " among " + index.size());
		if (queryType == Index.INTERSECTION_QUERY) {
			PostingsList results = null;
			for (String term : query.terms) {
				if (results == null) {
					results = getPostings(term, true);
				} else {
					results = results.intersect(getPostings(term, true));
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
					results = getPostings(term, false);
				} else {
					results = results.intersect(getPostings(term, false), offset, offset);
				}
			}
			if (results == null) return new ArrayList<PostingsEntry>();
			return results.toCollection();
		} else if (queryType == Index.RANKED_QUERY) {
			PostingsList results = null;
			for (String term : query.terms) {
				if (results == null) {
					results = getPostings(term, false);
				} else {
					results = results.union(getPostings(term, false));
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
     *  
     */
    public void cleanup() {
    }
}
