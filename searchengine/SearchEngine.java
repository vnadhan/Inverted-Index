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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SearchEngine {

	/*
	 * index : The Inverted Index It is a map between a word and a list of
	 * instances of Posting. When there is a document containing this word, a
	 * new posting is created and added to the list.
	 */
	private HashMap<String, List<Posting>> index;
	// Mapping between an index and a document.
	private HashMap<Integer, Document> documents;
	// Name of the input file
	private String fileName;

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

		// if the query has only ONE word, then get relevant documents and sort according to TF-IDF values.
		if (words.length == 1) {
			postings = index.get(words[0]);

			// Update IDF and TF-IDF for every posting
			if (postings != null) {
				// Calculate only if it is not already calculated.
				if (postings.get(0).getTf_idf() == null) {
					for (Posting posting : postings) {
						posting.setInverseDocFreq(documents.size(),
								postings.size());
					}
				}

				// Now sort the instances of Posting according to the respective
				// values of TF-IDF
				Collections.sort(postings);
			}
		} else {
			// This case is when the query is multi-word
			// Find all relevant documents that contain all the words in the query.
			// If the word does not exist in the Inverted Index, ignore it and continue 
			// with other words in the query.
			
			List<Posting> p_for_all_words_in_query = new ArrayList<Posting>();
			
			// For each word get the corresponding instances of Posting.
			// The purpose of the below loop is to find the list of Documents which contains 
			// all the words in the query.
			for (String w : words) {
				postings = index.get(w);
				
				if (postings != null) {
					if (p_for_all_words_in_query.size() == 0) {
						p_for_all_words_in_query.addAll(postings);
					} else {
						// retain an instance of Posting in p_for_all_words_in_query, 
						// if there exists a Posting in "postings" referring to the same document
						p_for_all_words_in_query.retainAll(postings);
					}
				}
			}
			// This has the list of relevant Documents 
			postings = p_for_all_words_in_query;
			// ToDo: Sort postings according to values of TF-IDF
		}

		// Print results
		if (postings != null && postings.size() > 0) {
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
						// Increment document counter
						documentCounter++;
						// Add the document to the document map
						searchEngine.addDocumentToMap(documentCounter, line);
					}
				}

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
}
