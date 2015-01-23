package com.dong.question_classification.common;

public class Item {

	private Integer wordId;
	private double tfIdf;
	public Integer getWordId() {
		return wordId;
	}
	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}
	public double getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}
	public Item(Integer wordId, double tfIdf) {
		super();
		this.wordId = wordId;
		this.tfIdf = tfIdf;
	}
	
	
	
	
}
