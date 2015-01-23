package com.dong.question_classification.component;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.ProcessorType;
import com.dong.question_classification.common.QuestionAnalyzer;
import com.dong.question_classification.common.Term;
import com.dong.question_classification.common.TermFilter;
import com.dong.question_classification.utils.ReflectionUtils;

public class DocumentWordsCollector extends AbstractComponent {

	private static final Log LOG = LogFactory
			.getLog(DocumentWordsCollector.class);
	private final QuestionAnalyzer analyzer;
	private final Set<TermFilter> filters = new HashSet<TermFilter>();
	private Context context;

	public DocumentWordsCollector(Context context) {
		super(context);
		this.context = context;
		String analyzerClass = context.getConfiguration().get(
				"processor.document.analyzer.class");
		LOG.info("Analyzer class name: class=" + analyzerClass);
		analyzer = ReflectionUtils.getInstance(analyzerClass,
				QuestionAnalyzer.class,
				new Object[] { context.getConfiguration() });
		// load term filter classes
		String filterClassNames = context.getConfiguration().get(
				"processor.document.filter.classes");
		if (filterClassNames != null) {
			LOG.info("Load filter classes: classNames=" + filterClassNames);
			String[] aClazz = filterClassNames.split("\\s*,\\s*");
			for (String clazz : aClazz) {
				TermFilter filter = ReflectionUtils.getInstance(clazz,
						TermFilter.class, new Object[] { context });
				if (filter == null) {
					throw new RuntimeException("Fail to reflect: class="
							+ clazz);
				}
				filters.add(filter);
				LOG.info("Added filter instance: filter=" + filter);
			}
		}
	}

	@Override
	public void fire() {
		analyzer.analyze(
				context);
		// output statistics
		start();
	}

	protected void filterTerms(Map<String, Term> terms) {
		for (TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

	private void start() {
		LOG.info("STAT: totalQuestionCount="
				+ context.getVectorMetadata().getTotalDocCount());
		LOG.info("STAT: labelCount="
				+ context.getVectorMetadata().getLabelCount());
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context
				.getVectorMetadata().termTableIterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			LOG.info("STAT: label=" + entry.getKey() + ", questionCount="
					+ entry.getValue().size());
		}
	}

	public static void main(String[] args) {
		Context context = new Context(ProcessorType.TRAIN,
				"config-train.properties");
		new BasicInformationCollector(context).fire();
		new DocumentWordsCollector(context).fire();
	}
}
