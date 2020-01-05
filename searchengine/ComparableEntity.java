package searchengine;

public class ComparableEntity implements Comparable<ComparableEntity> {

	// Cosine Similarity
	private Double cosine_similarity;

	// Cosine Similarity numerator
	private Double dot_prod;
	
	// Cosine Similarity denominator
	private Double squares;

	// Associated Document
	private Document document;
	
	
	public ComparableEntity(Document document, Double dot_prod, Double squares) {
		super();
		this.dot_prod = dot_prod;
		this.squares = squares;
		this.document = document;
		
		this.cosine_similarity = this.dot_prod / this.squares;
	}

	@Override
	public int compareTo(ComparableEntity o) {
		// In descending order
		return o.getCosine_similarity().compareTo(this.getCosine_similarity());
	}

	public Double getCosine_similarity() {
		return cosine_similarity;
	}

	public void setCosine_similarity(Double cosine_similarity) {
		this.cosine_similarity = cosine_similarity;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
}
