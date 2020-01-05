package searchengine;

public class TermMetrics {

	public TermMetrics(Double idf) {
		super();
		
		count = 1;
		this.idf = idf;
	}

	// Term Frequency
	private Double tf;
	// Inverse Document Frequency
	private Double idf;
	// TF-IDF
	private Double tf_idf;
	// Count
	private Integer count;
	
	public Double getTf_idf() {
		return tf_idf;
	}

	public void calculateTf_idf() {
		this.tf_idf = this.tf * this.idf;
	}

	public Double getIdf() {
		return idf;
	}

	public void setIdf(Double idf) {
		this.idf = idf;
	}

	public Double getTf() {
		return tf;
	}

	public void setTf(Double tf) {
		this.tf = tf;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	// Calculate and set Term Frequency
	public void setTermFreq(Integer total_words) {
		// Strategy: term frequency adjusted for document length
		this.tf = ((double) this.count / (double) total_words);
	}
}
