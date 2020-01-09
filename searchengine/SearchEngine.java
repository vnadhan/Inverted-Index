package searchengine;

/**
 * 
 * The Simple Search Engine
 * 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;

public class SearchEngine {

	/*
	 * index : The Inverted Index It is a map between a word and a list of
	 * instances of Posting. When there is a document containing this word, a
	 * new posting is created and added to the list.
	 */
	private HashMap<String, List<Posting>> index;

	private HashMap<String, Integer> term_identifier;

	// Mapping between an index and a document.
	private HashMap<Integer, Document> documents;
	// Name of the input file
	private String fileName;

	// term-document tf-idf mapping
	private double[][] termDocumentMapping;
	
	// inverse of term-document tf-idf mapping
	private double[][] termDocumentMapping_T;

	// Counter to store the number of documents.
	private Integer termCounter;

	/*
	 * tf_strategy: Term Frequency strategy. doc - term frequency adjusted for
	 * document length aug - augmented frequency
	 */
	private String tf_strategy;
	// Regex to split a document into words
	public final String WORD_REGEX = "[^\\w\\-]+";

	// Search Engine Constructor
	public SearchEngine(String fileName, String tf_strategy) {
		super();
		this.fileName = fileName;
		this.tf_strategy = tf_strategy;

		// Init
		this.documents = new HashMap<Integer, Document>();
		this.index = new HashMap<String, List<Posting>>();
		this.term_identifier = new HashMap<String, Integer>();
		termCounter = 0;
	}

	// Add a document (text) into document map
	private void addDocumentToMap(Integer documentIndex, String text) {
		// get all words from the text
		String[] words = text.split(WORD_REGEX);
		// Create a new Document instance for the text
		Document document = new Document(words.length, text, documentIndex);
		// Add it to map
		this.documents.put(documentIndex, document);
		// Add the words in this document into the Inverted Index.
		addtToIndex(words, document);
	}

	// Add words to the Inverted Index
	private void addtToIndex(String[] wordsInDocument, Document document) {
		// Map between a word and its occurrence frequency in the document.
		Map<String, Integer> wordCountMap = new HashMap<String, Integer>();

		// Temporary variables.
		String word = "";
		Integer wordCount = 0;
		List<Posting> temp_postings = null;

		// Iterate through all words in the document, find its occurrence
		// frequency and both of them to wordCountMap
		for (String w : wordsInDocument) {
			w = w.trim().toLowerCase();

			// Ignore empty words
			if (w.length() > 0) {
				if (wordCountMap.containsKey(w)) {
					wordCountMap.put(w, wordCountMap.get(w) + 1);
				} else {
					wordCountMap.put(w, 1);
				}
			}
		}

		// For this document, store the count of the frequently occurring word
		// This count is used when the Term Frequency strategy is Augmented
		// Frequency.
		documents.get(document.getId()).setFreq_of_max_occuring_word(
				Collections.max(wordCountMap.values()));

		// For each word in the wordcountMap, add the word to the Inverted Index
		// and also add
		// a posting corresponding to the document.
		for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
			word = entry.getKey();
			wordCount = entry.getValue();

			if (index.containsKey(word)) {
				// Already contains the word
				// Add a new posting.
				index.get(word).add(
						new Posting(wordCount, document, tf_strategy));
			} else {
				// Word does not exist in the index.
				temp_postings = new ArrayList<Posting>();
				temp_postings
						.add(new Posting(wordCount, document, tf_strategy));

				index.put(word, temp_postings);
				term_identifier.put(word, termCounter);
				++termCounter;
			}
		}
	}

	// Method to find the query in the Inverted Index and to return the list of
	// relevant documents.
	private void findQueryInIndex(String query) {
		// Use regex to split the query into words.
		String[] words = query.split(WORD_REGEX);

		// To store the name of the documents relevant to the query
		List<String> result = new ArrayList<String>();

		// Get list of instances of Posting for the query word.
		List<Posting> postings = null;

		// if the query has only ONE word, then get relevant documents and sort
		// according to TF-IDF values.
		if (words.length == 1) {
			postings = index.get(words[0]);

			// Update IDF and TF-IDF for every posting
			if (postings != null && postings.size() > 0) {
				// Calculate only if it is not already calculated.
//				if (postings.get(0).getTf_idf() == null) {
//					for (Posting posting : postings) {
//						posting.setInverseDocFreq(documents.size(), postings.size());
//					}
//				}

				// Now sort the instances of Posting according to the respective
				// values of TF-IDF
				Collections.sort(postings);

				// Print results
				// Push the name of the documents into the result list.
				for (Posting posting : postings) {
					// System.out.println(posting.getTf_idf());
					result.add(posting.getDocument().getDocumentIdAsString());
				}

				// Print the list of relevant documents
				System.out.println(result);

			} else {
				// No relevant documents found!
				System.out.println("[]");
			}
		} else {
			// This case is when the query is multi-word
			// Find all relevant documents that contain all the words in the
			// query.
			// If the word does not exist in the Inverted Index, ignore it and
			// continue
			// with other words in the query.

			// List<Posting> p_for_all_words_in_query = new
			// ArrayList<Posting>();
			Set<Posting> all_postings = new HashSet<Posting>();

			// For each word get the corresponding instances of Posting.
			// The purpose of the below loop is to find the list of Documents
			// which contains
			// all the words in the query.
			for (String w : words) {
				postings = index.get(w);

				if (postings != null) {
					all_postings.addAll(postings);
				}
			}

			// for(Posting p : all_postings) {
			// System.out.println(p.getDocument().getId());
			// }

			// We have got the relevant documents, now sort it using
			// cosine-similarity method.
			sortResultsForMultiWordQuery(words, all_postings);
		}
	}

	// Sort and display the relevant documents for a multi-word query
	private void sortResultsForMultiWordQuery(String[] queryWords,
			Set<Posting> relevantDocuments) {

		// An index for the words in the query
		Map<String, TermMetrics> index_for_query = new HashMap<String, TermMetrics>();

		// Temporary variable
		// IDF for a term
		Double term_idf;

		// Iterate through every word in the query and add it to an index.
		for (String w : queryWords) {
			if (index.containsKey(w)) {
				if (index_for_query.containsKey(w)) {
					index_for_query.get(w).setCount(
							index_for_query.get(w).getCount() + 1);
				} else {
					term_idf = index.get(w).get(0).getIdf();
					index_for_query.put(w, new TermMetrics(term_idf));
				}
			}
		}

		// for(String term : index_for_query.keySet()){
		// System.out.println(term + " " +
		// index_for_query.get(term).getCount());
		// }

		// Temporary variables.
		TermMetrics tm = null;
		Double q_square = 0.0;
		String term = null;
		Integer documentId = null;
		Integer termId = null;
		Double q_d_dot_product;
		Double d_square;
		ComparableEntity newEntity = null;

		// Go through the index for the query and update it with TF, IDF, TF-IDF
		// values.
		// Also, calculate ||Query|| which is required for cosine-similarity
		for (Map.Entry<String, TermMetrics> entry : index_for_query.entrySet()) {
			tm = entry.getValue();
			tm.setTermFreq(queryWords.length);
			tm.calculateTf_idf();
			q_square += Math.pow(tm.getTf_idf(), 2);
			entry.setValue(tm);
		}
		q_square = Math.sqrt(q_square);

		// To hold the sorted document list.
		List<ComparableEntity> resultList = new ArrayList<ComparableEntity>();
		
		// A double loops to calculate the cosine similarity between the query
		// and a relevant document
		for (Posting posting : relevantDocuments) {
			documentId = posting.getDocument().getId();
			q_d_dot_product = 0.0;
			
			d_square = Arrays.stream(termDocumentMapping_T[documentId])
					.map(n -> n*n)
					.reduce(0.0, Double::sum);
			
			d_square = Math.sqrt(d_square);
			
			for (Map.Entry<String, TermMetrics> entry : index_for_query
					.entrySet()) {
				tm = entry.getValue();
				term = entry.getKey();
				termId = term_identifier.get(term);
				q_d_dot_product += (termDocumentMapping[termId][documentId] * tm
						.getTf_idf());
			}
			
			newEntity = new ComparableEntity(posting.getDocument(),
					q_d_dot_product, d_square * q_square);
			resultList.add(newEntity);
		}

		// Sort the results using comparator defined in ComparableEntity
		if (resultList.size() > 0) {
			Collections.sort(resultList);

			// To store the name of the documents
			List<String> result = new ArrayList<String>();

			for (ComparableEntity entity : resultList) {
				// System.out.println(posting.getTf_idf());
				result.add(entity.getDocument().getDocumentIdAsString());
			}

			// Print the list of relevant documents
			System.out.println(result);
		} else {
			// No relevant documents found!
			System.out.println("[]");
		}
	}

	// Stater method.
	public static void main(String[] args) {

		// Expecting 2 arguments
		if (args.length > 0 && args.length == 2) {
			String file = args[0];
			BufferedReader reader = null;
			Scanner scanner = null;

			try {
				reader = new BufferedReader(new FileReader(file));

				// Allow only aug or doc as the Term Frequency strategy.
				if (!(args[1].equals("aug") || args[1].equals("doc"))) {
					System.out
							.println("Invalid Term Frequency strategy! [doc|aug]");
					System.exit(0);
				}

				// Init new search engine
				SearchEngine searchEngine = new SearchEngine(file, args[1]);

				// To store a line read by the reader
				String line;
				// Counter to store the number of documents.
				Integer documentCounter = 0;

				// Loop over each line (document) in the file.
				while ((line = reader.readLine()) != null) {
					if (line != "" && line.length() > 0) {
						// Add the document to the document map
						searchEngine.addDocumentToMap(documentCounter, line);
						// Increment document counter
						documentCounter++;
					}
				}

				// iterate through all documents to create an array of TF-IDF
				// between each term and each document.
				searchEngine.iterateDocuments();

				// Input scanner
				scanner = new Scanner(System.in);
				String input;

				// Infinite loop to get query, return results, wait for next
				// query and so on.
				while (true) {
					System.out
							.println("Write a query [Enter empty input to exit]");
					input = scanner.nextLine();

					// on empty input the application exits
					if (input.length() > 0) {
						// Search the index for the query.
						searchEngine.findQueryInIndex(input);
					} else {
						System.out.println("Application exiting!");
						break;
					}
				}
			} catch (FileNotFoundException e) {
				System.err.println("Error reading file : " + file);
			} catch (IOException e) {
				System.err.println("Error reading input in file : " + file);
			} finally {
				// Cleanup
				try {
					if (reader != null) {
						reader.close();
					}
					if (scanner != null) {
						scanner.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err
					.println("Invalid Arguments! E.g. java SearchEngine <file> [doc|aug]");
		}
	}

	private void iterateDocuments() {
		termDocumentMapping = new double[term_identifier.size()][documents
				.size()];
		Integer termId;

		for (Map.Entry<String, List<Posting>> entry : index.entrySet()) {
			termId = term_identifier.get(entry.getKey());
			for (Posting posting : entry.getValue()) {
				if (entry.getValue() != null) {
					posting.setInverseDocFreq(documents.size(), entry
							.getValue().size());
					termDocumentMapping[termId][posting.getDocument().getId()] = posting
							.getTf_idf();
				}
			}
		}
		
		
//		for (String term : term_identifier.keySet()) {
//			System.out.println(term + " " + term_identifier.get(term));
//		}
//		System.out.println("\n");

//		for (int i = 0; i < term_identifier.size(); i++) {
//			System.out.println();
//			for (int j = 0; j < documents.size(); j++) {
//				System.out.print(termDocumentMapping[i][j] + "\t\t\t");
//			}
//		}
//		System.out.println();
		
		termDocumentMapping_T = MatrixUtils.createRealMatrix(termDocumentMapping).transpose().getData();
		
//		System.out.println("-------------------------------------\n");
//
//		for (int i = 0; i < documents.size(); i++) {
//			System.out.println();
//			for (int j = 0; j < term_identifier.size(); j++) {
//				System.out.print(termDocumentMapping_T[i][j] + "\t\t\t");
//			}
//		}
	}
}
