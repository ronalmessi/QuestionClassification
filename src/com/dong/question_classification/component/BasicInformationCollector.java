package com.dong.question_classification.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;

public class BasicInformationCollector extends AbstractComponent {

	private static final Log LOG = LogFactory
			.getLog(BasicInformationCollector.class);


	public BasicInformationCollector(Context context) {
		super(context);
	}

	@Override
	public void fire() {
		int totalQuestionCount = 0;
		File inputFile=context.getFDMetadata().getInputRootDir();
//		File trainFile = new File(
//				"D:\\play\\workspace11\\QuestionClassification\\source\\data.txt");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFile), charSet));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					String[] question_label = line.split(",");
					if (question_label.length == 2) {
						String label = question_label[1];
						if (label.startsWith("L")) {
							context.getVectorMetadata().addLabel(label);
							LOG.info("Add label: label=" + label);
							int labelledTotalDocCount = context
									.getVectorMetadata()
									.getLabelledTotalDocCount(label);
							labelledTotalDocCount++;
							context.getVectorMetadata()
									.putLabelledTotalDocCount(label,
											labelledTotalDocCount);
							LOG.info("Put question count: label= " + label
									+ ", questionCount="
									+ labelledTotalDocCount);
							totalQuestionCount = totalQuestionCount + 1;
						}
					}

				}
			}
			LOG.info("Total question: totalCount= " + totalQuestionCount);
			context.getVectorMetadata().setTotalDocCount(totalQuestionCount);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				LOG.warn(e);
			}
		}
	}

	public static void main(String[] args) {
		Context context = new Context(ProcessorType.TRAIN,
				"config-train.properties");
		new BasicInformationCollector(context).fire();
	}
}
