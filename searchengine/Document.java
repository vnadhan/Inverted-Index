package searchengine;

/**
 * Entity class for Document to store information about a document.
 */
public class Document {
	// Number of words in the document
	private Integer num_of_words;
	// The actual text in the document
	private String text;
	// Identifier for the document
	private Integer id;
	// Document Identifier as String
	private String documentIdAsString;
	// Frequency of the maximum occurring word in the document
	private Integer freq_of_max_occuring_word;

	// Constructor
	public Document(Integer num_of_words, String text, Integer id) {
		super();
		this.num_of_words = num_of_words;
		this.id = id;
		this.documentIdAsString = "document" + this.id;
		this.text = text;
	}

	// getter
	public Integer getFreq_of_max_occuring_word() {
		return freq_of_max_occuring_word;
	}

	// setter
	public void setFreq_of_max_occuring_word(Integer freq_of_max_occuring_word) {
		this.freq_of_max_occuring_word = freq_of_max_occuring_word;
	}
	
	// getter
	public String getDocumentIdAsString() {
		return documentIdAsString;
	}
	
	// getter
	public Integer getNum_of_words() {
		return num_of_words;
	}

	// getter
	public String getText() {
		return text;
	}

	// getter
	public Integer getId() {
		return id;
	}
}
