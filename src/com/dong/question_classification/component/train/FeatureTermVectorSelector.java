package com.dong.question_classification.component.train;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;
import com.dong.question_classification.common.Term;
import com.dong.question_classification.component.BasicInformationCollector;
import com.dong.question_classification.component.DocumentWordsCollector;
import com.dong.question_classification.utils.SortUtils;
import com.dong.question_classification.utils.SortUtils.Result;

public class FeatureTermVectorSelector extends AbstractComponent {

	private static final Log LOG = LogFactory
			.getLog(FeatureTermVectorSelector.class);
	private final int keptTermCountEachLabel;

	public FeatureTermVectorSelector(Context context) {
		super(context);
		keptTermCountEachLabel = context.getConfiguration().getInt(
				"processor.each.label.kept.term.count", 3000);
	}

	@Override
	public void fire() {
		// compute CHI value for selecting feature terms
		// after sorting by CHI value
		for (String label : context.getVectorMetadata().getLabels()) {
			// for each label, compute CHI vector
			LOG.info("Compute CHI for: label=" + label);
			processOneLabel(label);
		}

		// sort and select CHI vectors
		Iterator<Entry<String, Map<String, Term>>> chiIter = context
				.getVectorMetadata().chiLabelToWordsVectorsIterator();
		while (chiIter.hasNext()) {
			Entry<String, Map<String, Term>> entry = chiIter.next();
			String label = entry.getKey();
			LOG.info("Sort CHI terms for: label=" + label + ", termCount="
					+ entry.getValue().size());
			Result result = sort(entry.getValue());
			for (int i = result.getStartIndex(); i <= result.getEndIndex(); i++) {
				Entry<String, Term> termEntry = result.get(i);
				// merge CHI terms for all labels
				context.getVectorMetadata().addChiMergedTerm(
						termEntry.getKey(), termEntry.getValue());
			}
		}
	}

	private Result sort(Map<String, Term> terms) {
		SortUtils sorter = new SortUtils(terms, true, keptTermCountEachLabel);
		Result result = sorter.heapSort();
		return result;
	}

	private void processOneLabel(String label) {
		Iterator<Entry<String, Map<String, Set<String>>>> iter = context
				.getVectorMetadata().invertedTableIterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Set<String>>> entry = iter.next();
			String word = entry.getKey();
			Map<String, Set<String>> labelledQuestions = entry.getValue();

			// A: question count containing the word in this label
			int questionCountContainingWordInLabel = 0;
			if (labelledQuestions.get(label) != null) {
				questionCountContainingWordInLabel = labelledQuestions.get(
						label).size();
			}
			System.out.println(label + "--A---"
					+ questionCountContainingWordInLabel);
			// B: doc count containing the word not in this label
			int docCountContainingWordNotInLabel = 0;
			Iterator<Entry<String, Set<String>>> labelledIter = labelledQuestions
					.entrySet().iterator();
			while (labelledIter.hasNext()) {
				Entry<String, Set<String>> labelledEntry = labelledIter.next();
				String tmpLabel = labelledEntry.getKey();
				if (!label.equals(tmpLabel)) {
					docCountContainingWordNotInLabel += entry.getValue().size();
				}
			}
			System.out.println(label + "--B---"
					+ docCountContainingWordNotInLabel);
			// C: doc count not containing the word in this label
			int docCountNotContainingWordInLabel = getDocCountNotContainingWordInLabel(
					word, label);
			System.out.println(label + "--C---"
					+ docCountNotContainingWordInLabel);
			// D: doc count not containing the word not in this label
			int docCountNotContainingWordNotInLabel = getDocCountNotContainingWordNotInLabel(
					word, label);
			System.out.println(label + "--D---"
					+ docCountNotContainingWordNotInLabel);
			// compute CHI value
			int N = context.getVectorMetadata().getTotalDocCount();
			int A = questionCountContainingWordInLabel;
			int B = docCountContainingWordNotInLabel;
			int C = docCountNotContainingWordInLabel;
			int D = docCountNotContainingWordNotInLabel;
			int temp = (A * D - B * C);
			// double chi = (double) N*temp*temp / (A+C)*(A+B)*(B+D)*(C+D); //
			// incorrect!!!
			double chi = (double) N * temp * temp
					/ ((A + C) * (A + B) * (B + D) * (C + D)); // correct
			System.out.println(word+"--CHI:"+chi); // formula
			// computation
			Term term = new Term(word);
			term.setChi(chi);
			context.getVectorMetadata().addChiTerm(label, word, term);
		}
	}

	private int getDocCountNotContainingWordInLabel(String word, String label) {
		int count = 0;
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context
				.getVectorMetadata().termTableIterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			String tmpLabel = entry.getKey();
			// in this label
			if (tmpLabel.equals(label)) {
				Map<String, Map<String, Term>> labelledDocs = entry.getValue();
				for (Entry<String, Map<String, Term>> docEntry : labelledDocs
						.entrySet()) {
					// not containing this word
					if (!docEntry.getValue().containsKey(word)) {
						++count;
					}
				}
				break;
			}
		}
		return count;
	}

	private int getDocCountNotContainingWordNotInLabel(String word, String label) {
		int count = 0;
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context
				.getVectorMetadata().termTableIterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			String tmpLabel = entry.getKey();
			// not in this label
			if (!tmpLabel.equals(label)) {
				Map<String, Map<String, Term>> labelledDocs = entry.getValue();
				for (Entry<String, Map<String, Term>> docEntry : labelledDocs
						.entrySet()) {
					// not containing this word
					if (!docEntry.getValue().containsKey(word)) {
						++count;
					}
				}
			}
		}
		return count;
	}

	public static void main(String[] args) {
		Context context = new Context(ProcessorType.TRAIN,
				"config-train.properties");
		new BasicInformationCollector(context).fire();
		new DocumentWordsCollector(context).fire();
		new FeatureTermVectorSelector(context).fire();
	}
}
