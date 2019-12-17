ReadMe

- Example documents:
	the brown fox jumped over the brown dog
	the lazy brown dog sat in the corner
	the red fox bit the lazy dog

- The starting point of the application is SearchEngine.java. There are two other files (entities): Posting, Document.

- The program has two inputs:
	1. A file having the documents. (sample file: attached documents.txt)
	2. The strategy to calculate Term Frequency. The value can be either one of the below:
		doc - Term frequency adjusted for document length
		aug - Augmented Frequency

- The basic idea of the Inverted Index implemented in SearchEngine is:
	Word		Postings List 
	fox			1, 3
	dog			1, 2, 3
	....
	
	i.e. The Inverted Index is a map between a word and a list of instances of Posting
	
- Each Posting in the Postings list points to one document and it holds the documentID, and information about the relevance between a word and the document that is referenced by the Posting.

- Document refers to a document in the input file and it holds a few properties of the document.

- A query can be either one-word or multi-word. In the case of multi-word, the relevant documents are not sorted. This is not yet implemented. 

- When the query is one-word, the relevant documents will be sorted according to the values of TF-IDF.

- Steps to run the program:
	javac searchengine/*.java
	java searchengine.SearchEngine documents.txt doc