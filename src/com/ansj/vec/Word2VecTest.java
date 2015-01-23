package com.ansj.vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.dong.question_classification.common.CLibrary;
import com.dong.question_classification.common.Term;
import com.sun.jna.Native;

import love.cq.util.IOUtil;

public class Word2VecTest {
	private static final File sportCorpusFile = new File(
			"D:\\play\\workspace11\\QuestionClassification\\source\\result.txt");
	private static CLibrary analyzer;

	public static void main(String[] args) throws IOException {
		// 初始化
//		analyzer = (CLibrary) Native.loadLibrary(
//				"D:\\play\\workspace11\\QuestionClassification\\source\\NLPIR",
//				CLibrary.class);
//		int init_flag = analyzer.NLPIR_Init("", 1, "0");
//		analyzer.NLPIR_AddUserWord("为啥");
//		String resultString = null;
//		if (0 == init_flag) {
//			resultString = analyzer.NLPIR_GetLastErrorMsg();
//			System.err.println("初始化失败！\n" + resultString);
//			return;
//		}
//
//		File[] files = new File(
//				"D:\\play\\workspace11\\QuestionClassification\\word2vec")
//				.listFiles();
//		// 构建语料
//		try (FileOutputStream fos = new FileOutputStream(sportCorpusFile)) {
//			System.out.println(files.length);
//			for (File file : files) {
//				if (file.canRead() && file.getName().endsWith(".txt")) {
//					parserFile(fos, file);
//				}
//			}
//		}
//
//		// 进行分词训练
//
//		Learn lean = new Learn();
//
//		lean.learnFile(sportCorpusFile);
//
//		lean.saveModel(new File(
//				"D:\\play\\workspace11\\QuestionClassification\\word2vec\\vector.mod"));

		// 加载测试
		Word2VEC w2v = new Word2VEC();
		w2v.loadJavaModel("D:\\play\\workspace11\\QuestionClassification\\word2vec\\vector.mod");
		System.out.println(w2v.distance("区别"));

	}

	private static void parserFile(FileOutputStream fos, File file)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		try (BufferedReader br = IOUtil.getReader(file.getAbsolutePath(),
				IOUtil.UTF8)) {
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					// String[] question_label = line.split(",");
					// if (question_label.length == 2
					// && question_label[1].startsWith("L")) {
					// String question = question_label[0];
					// String lable = question_label[1];
					// System.out.println(question + "--" + lable);
					String question = line;
					System.out.println(question);
					Map<String, Term> terms = new HashMap<String, Term>(0);
					String content = analyzer.NLPIR_ParagraphProcess(question,
							1);
					String[] rawWords = content.split("\\s+");
					for (String rawWord : rawWords) {
						String[] words = rawWord.split("/");
						StringBuilder sb = new StringBuilder();
						if (words.length == 2) {
							String word = words[0];
							sb.append(word).append(" ");
						}
						fos.write(sb.toString().getBytes());
					}
					// }
				}
			}
		}
	}
	//
	// private static void paserStr(FileOutputStream fos, String title)
	// throws IOException {
	// List<Term> parse2 = ToAnalysis.parse(title);
	// StringBuilder sb = new StringBuilder();
	// for (Term term : parse2) {
	// sb.append(term.getName());
	// sb.append(" ");
	// }
	// fos.write(sb.toString().getBytes());
	// fos.write("\n".getBytes());
	// }
}
