package com.dong.question_classification.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.CLibrary;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;
import com.dong.question_classification.common.QuestionAnalyzer;
import com.dong.question_classification.common.Term;
import com.dong.question_classification.config.Configuration;
import com.sun.jna.Native;

public class IctclasAnalyzer extends AbstractDocumentAnalyzer implements
		QuestionAnalyzer {

	private static final Log LOG = LogFactory.getLog(IctclasAnalyzer.class);
	private final CLibrary analyzer;

	public IctclasAnalyzer(Configuration configuration) {
		super(configuration);
		// 初始化
		analyzer = (CLibrary) Native.loadLibrary(
				"D:\\play\\workspace11\\QuestionClassification\\source\\NLPIR",
				CLibrary.class);
		int init_flag = analyzer.NLPIR_Init("", 1, "0");
		String resultString = null;
		if (0 == init_flag) {
			resultString = analyzer.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！\n" + resultString);
			return;
		}
	}

	@Override
	public Map<String, Term> analyze(Context context) {
		File file = context.getFDMetadata().getInputRootDir();
		LOG.info("Process document: file=" + file.getAbsolutePath());
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), charSet));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					String[] question_label = line.split(",");
					if (question_label.length == 2
							&& question_label[1].startsWith("L")) {
						String question = question_label[0];
						String lable = question_label[1];
						System.out.println(question + "--" + lable);
						Map<String, Term> terms = new HashMap<String, Term>(0);
						String content = analyzer.NLPIR_ParagraphProcess(
								question, 1);
						String[] rawWords = content.split("\\s+");
						for (String rawWord : rawWords) {
							String[] words = rawWord.split("/");
							if (words.length == 2
									&& !super.isStopword(words[0])) {
								String word = words[0];
								String lexicalCategory = words[1];
								Term term = terms.get(word);
								if (term == null) {
									term = new Term(word);
									term.setLexicalCategory(lexicalCategory);
									terms.put(word, term);
								}
								term.incrFreq();
							}
						}
						context.getVectorMetadata().addTerms(lable, question,
								terms);
						context.getVectorMetadata().addTermsToInvertedTable(lable, question, terms);
						
//						System.out.println("Done: question=" + question + ", termCount=" + terms.size());
//						System.out.println("Terms in a question: terms=" + terms);
					}
				}
			}
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
		return null;
	}

	public static void main(String[] args) {
		Context context = new Context(ProcessorType.TRAIN, "config-train.properties");
		IctclasAnalyzer a = new IctclasAnalyzer(context.getConfiguration());
		
		Map<String, Term> terms = a.analyze(context);
		for (Entry<String, Term> entry : terms.entrySet()) {
			System.out.println(entry.getValue());
		}

	}

	

}
