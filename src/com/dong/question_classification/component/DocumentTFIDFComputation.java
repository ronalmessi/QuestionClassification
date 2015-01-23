package com.dong.question_classification.component;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;
import com.dong.question_classification.common.Term;
import com.dong.question_classification.component.train.FeatureTermVectorSelector;
import com.dong.question_classification.utils.MetricUtils;

public class DocumentTFIDFComputation extends AbstractComponent {

	private static final Log LOG = LogFactory
			.getLog(DocumentTFIDFComputation.class);

	public DocumentTFIDFComputation(Context context) {
		super(context);
	}

	@Override
	public void fire() {
		System.out.println("11111111");
		// for each question, compute TF, IDF, TF-UDF
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context
				.getVectorMetadata().termTableIterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> labelledQuestionsEntry = iter
					.next();
			String label = labelledQuestionsEntry.getKey();
			Map<String, Map<String, Term>> questions = labelledQuestionsEntry
					.getValue();
			Iterator<Entry<String, Map<String, Term>>> questionsIter = questions
					.entrySet().iterator();
			while (questionsIter.hasNext()) {
				Entry<String, Map<String, Term>> questionsEntry = questionsIter
						.next();
				String question = questionsEntry.getKey();
				Map<String, Term> terms = questionsEntry.getValue();
				Iterator<Entry<String, Term>> termsIter = terms.entrySet()
						.iterator();
				while (termsIter.hasNext()) {
					Entry<String, Term> termEntry = termsIter.next();
					String word = termEntry.getKey();
					// check whether word is contained in CHI vector
					if (context.getVectorMetadata().containsChiWord(word)) {
						Term term = termEntry.getValue();
						int freq = term.getFreq();
						int termCount = context.getVectorMetadata()
								.getTermCount(label, question);

						double tf = MetricUtils.tf(freq, termCount);
						int totalDocCount = context.getVectorMetadata()
								.getTotalDocCount();
						int docCountContainingTerm = context
								.getVectorMetadata().getDocCount(term);

						double idf = MetricUtils.idf(totalDocCount,
								docCountContainingTerm);

						System.out.println(word + "--tf--" + tf + "--idf--"
								+ idf);
						termEntry.getValue().setIdf(idf);
						termEntry.getValue().setTf(tf);
						termEntry.getValue().setTfidf(
								MetricUtils.tfidf(tf, idf));
						System.out.println("Term detail: label=" + label
								+ ", doc=" + question + ", term=" + term);
					} else {
						// remove term not contained in CHI vector
						termsIter.remove();
						System.out.println("Not in CHI vector: word=" + word);

					}
				}
			}
		}
	}

	public static void main(String[] args) {
		Context context = new Context(ProcessorType.TRAIN,
				"config-train.properties");
		new BasicInformationCollector(context).fire();
		new DocumentWordsCollector(context).fire();
		new FeatureTermVectorSelector(context).fire();
		new DocumentTFIDFComputation(context).fire();
	}

}
