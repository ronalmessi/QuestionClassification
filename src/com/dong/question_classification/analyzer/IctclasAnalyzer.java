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
				"C:\\iclass-workspace\\QuestionClassification\\source\\NLPIR",
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
	public Map<String, Term> analyze(File file) {
		String doc = file.getAbsolutePath();
		LOG.info("Process document: file=" + doc);
		Map<String, Term> terms = new HashMap<String, Term>(0);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), charSet));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					String content = analyzer.NLPIR_ParagraphProcess(line, 1);
					// String content = new String(nativeBytes, 0,
					// nativeBytes.length, charSet);
					System.out.println(content);
					String[] rawWords = content.split("\\s+");
					for (String rawWord : rawWords) {
						String[] words = rawWord.split("/");
						if (words.length == 2) {
							String word = words[0];
							String lexicalCategory = words[1];
							Term term = terms.get(word);
							if (term == null) {
								term = new Term(word);
								// TODO set lexical category
								term.setLexicalCategory(lexicalCategory);
								terms.put(word, term);
							}
							term.incrFreq();
							LOG.debug("Got word: word=" + rawWord);
						}
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
		return terms;
	}

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		IctclasAnalyzer a = new IctclasAnalyzer(configuration);
		String f = "C:\\Users\\iclass\\Desktop\\22.txt";
		Map<String, Term> terms = a.analyze(new File(f));
		for (Entry<String, Term> entry : terms.entrySet()) {
			System.out.println(entry.getValue());
		}

	}

}
