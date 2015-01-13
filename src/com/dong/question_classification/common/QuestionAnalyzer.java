package com.dong.question_classification.common;

import java.io.File;
import java.util.Map;

public interface QuestionAnalyzer {
	Map<String, Term> analyze(File file);
}
