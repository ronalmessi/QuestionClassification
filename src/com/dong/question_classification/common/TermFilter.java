package com.dong.question_classification.common;

import java.util.Map;

public interface TermFilter {

	void filter(Map<String, Term> terms);
}
