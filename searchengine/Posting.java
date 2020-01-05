package searchengine;

import java.util.Objects;

/**
 * Entity class for Posting. A Posting has a reference to a document, contains
 * information about the relevance between a word and the document.
 */
public class Posting implements Comparable<Posting> {

	// Term Frequency
	private Double tf;
	// Inverse Document Frequency
	private Double idf;
	// TF-IDF
	private Double tf_idf;

	/*
	 * tf_strategy: Term Frequency strategy. doc - term frequency adjusted for
	 * document length aug - augmented frequency
	 */
	private String tf_strategy;
	// Raw frequency of the word
	private Integer raw_frequency;
	// Document for this instance of Posting
	private Document document;

	// Constructor
	public Posting(Integer raw_frequency, Document document, String tf_strategy) {
		super();
		this.raw_frequency = raw_frequency;
		this.document = document;
		this.tf_idf = null;
		this.tf_strategy = tf_strategy;

		// Calculate and set Term Frequency
		setTermFreq();
	}

	// Return TF-IDF
	public Double getTf_idf() {
		return this.tf_idf;
	}

	// Calculate and set Term Frequency
	private void setTermFreq() {
		if (this.tf_strategy.equalsIgnoreCase("doc")) {
			// Strategy: term frequency adjusted for document length
			this.tf = ((double) this.raw_frequency / (double) this.document
					.getNum_of_words());
		} else {
			// Strategy: Augmented Frequency
			if (this.document.getFreq_of_max_occuring_word() != null) {
				this.tf = 0.5 + (0.5 * (this.raw_frequency / this.document
						.getFreq_of_max_occuring_word()));
			} else {
				System.err
						.println("Some error while calculalting Term Frequency using Augmented Frequency strategy");
			}
		}
	}

	// Calculate and set IDF
	public void setInverseDocFreq(Integer N, Integer d) {
		// System.out.println(N + " - " + d);
		// ToDo: Should we change the calculation to 1+log() to avoid divide by 0?
		this.idf = 1 + Math.log((double) N / (double) d);
		// System.out.println("idf " + this.idf);
		this.tf_idf = this.tf * this.idf;
//		System.out.println(N + " " +  d + " " + this.tf + " " + this.idf +  " " + this.tf_idf);	
	}

	public Double getTf() {
		return tf;
	}

	public Double getIdf() {
		return idf;
	}

	public Integer getRaw_frequency() {
		return raw_frequency;
	}

	public Document getDocument() {
		return document;
	}

	@Override
	// Used for comparing two instances of Posting according
	// to the value of TF-IDF
	public int compareTo(Posting o) {
		// In descending order
		return o.getTf_idf().compareTo(this.getTf_idf());
	}

	// Two instances of Posting is equal if they point to the same document.
	// This is implemented to support List.retainAll() method 
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Posting) {
			Posting other = (Posting) o;
			return Objects
					.equals(this.document.getId(), other.document.getId());
		} else {
			return false;
		}
	}

	@Override
	// This has to be override when equals() method is overridden
	public int hashCode() {
		return Objects.hash(this.document.getId());
	}
}
