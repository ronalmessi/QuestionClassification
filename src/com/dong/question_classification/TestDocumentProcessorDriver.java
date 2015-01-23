package com.dong.question_classification;

import com.dong.question_classification.common.Component;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;
import com.dong.question_classification.component.BasicInformationCollector;
import com.dong.question_classification.component.DocumentTFIDFComputation;
import com.dong.question_classification.component.DocumentWordsCollector;
import com.dong.question_classification.component.test.LoadFeatureTermVector;
import com.dong.question_classification.component.test.OutputtingQuantizedTestData;

public class TestDocumentProcessorDriver extends AbstractDocumentProcessorDriver {

	@Override
	public void process() {
		Context context = new Context(ProcessorType.TEST, "config-test.properties");
		// for test data
		Component[]	chain = new Component[] {
				new BasicInformationCollector(context),
				new DocumentWordsCollector(context),
				new LoadFeatureTermVector(context),
				new DocumentTFIDFComputation(context),
				new OutputtingQuantizedTestData(context)
			};
		run(chain);
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(
				TestDocumentProcessorDriver.class);		
	}

}
