package com.dong.question_classification.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.Item;
import com.dong.question_classification.common.Term;
import com.dong.question_classification.component.train.OutputtingQuantizedTrainData;


public abstract class AbstractOutputtingQuantizedData extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(OutputtingQuantizedTrainData.class);
	protected BufferedWriter writer;
	
	public AbstractOutputtingQuantizedData(Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		// create term vectors for outputting/inputting
		quantizeTermVectors();
		// output train/test vectors
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(context.getFDMetadata().getOutputDir(), 
							context.getFDMetadata().getOutputVectorFile())), charSet));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> labelledDocsEntry = iter.next();
			String label = labelledDocsEntry.getKey();
			Integer labelId = getLabelId(label);
			if(labelId != null) {
				Map<String, Map<String, Term>>  docs = labelledDocsEntry.getValue();
				Iterator<Entry<String, Map<String, Term>>> docsIter = docs.entrySet().iterator();
				while(docsIter.hasNext()) {
					StringBuffer line = new StringBuffer();
					line.append(labelId).append(" ");
					Entry<String, Map<String, Term>> docsEntry = docsIter.next();
//					输出问题名称
//					line.append(docsEntry.getKey());
					Map<String, Term> terms = docsEntry.getValue();
					List<Item> itemList=new ArrayList<Item>();
					for(Entry<String, Term> termEntry : terms.entrySet()) {
						String word = termEntry.getKey();
						Integer wordId = getWordId(word);
						if(wordId != null) {
							Term term = termEntry.getValue();
							Item item=new Item(wordId, term.getTfidf());
							itemList.add(item);
//							line.append(wordId).append(":").append(term.getTfidf()).append(" ");
						}
					}
					Collections.sort(itemList, new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							int one = o1.getWordId();  
				            int two = o2.getWordId();
							return one-two;
						}
					});
					for(Item item:itemList){
//						TFIDF型权重
//						line.append(item.getWordId()).append(":").append(item.getTfIdf()).append(" ");
//						布尔型权重
						line.append(item.getWordId()).append(":").append("1").append(" ");
					}
					try {
						String element = line.toString().trim();
						LOG.debug("Write line: " + element);
						writer.write(element);
						writer.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				LOG.warn("Label ID can not be found: label=" + label + ", labelId=null");
			}
		}
		if(writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOG.info("Finished: outputVectorFile=" + context.getFDMetadata().getOutputVectorFile());
			
	}
	
	private Integer getWordId(String word) {
		return context.getVectorMetadata().getWordId(word);
	}

	private Integer getLabelId(String label) {
		return context.getVectorMetadata().getlabelId(label);
	}

	protected abstract void quantizeTermVectors();

}
