package com.dong.question_classification.libsvm;

import java.io.IOException;

import libsvm.*;

public class LibSVMTest {

	public static void main(String[] args) throws IOException {
		String[] trainArgs = {
				"-h",
				"0",
				"-t",
				"0",
				"D:\\play\\workspace11\\QuestionClassification\\source\\vector\\train\\train.txt" };
		String modelFile = svm_train.main(trainArgs);
		String[] testArgs = {
				"D:\\play\\workspace11\\QuestionClassification\\source\\vector\\test\\test.txt",
				modelFile, "D:\\play\\workspace11\\QuestionClassification\\source\\result.txt" };										
		Double accuracy = svm_predict.main(testArgs);
		System.out.println("SVM Classification is done! The accuracy is "
				+ accuracy);

		// Test for cross validation
		// String[] crossValidationTrainArgs = {"-v", "10",
		// "UCI-breast-cancer-tra"};// 10 fold cross validation
		// modelFile = svm_train.main(crossValidationTrainArgs);
		// System.out.print("Cross validation is done! The modelFile is " +
		// modelFile);
	}

}
