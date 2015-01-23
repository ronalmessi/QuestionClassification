package com.dong.question_classification.component.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dong.question_classification.common.AbstractComponent;
import com.dong.question_classification.common.Context;
import com.dong.question_classification.common.Term;

public class LoadFeatureTermVector extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(LoadFeatureTermVector.class);
	
	public LoadFeatureTermVector(Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		// load CHI term vector
		loadChiTermVector();
	}
	
	private void loadChiTermVector() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					context.getFDMetadata().getChiTermVectorFile()), charSet));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.isEmpty()) {
					String[] aWord = line.split("\\s+");
					if(aWord.length == 2) {
						String word = aWord[0];
						int wordId = Integer.parseInt(aWord[1]);
						Term term = new Term(word);
						term.setId(wordId);
						LOG.info("Load CHI term: word=" + word + ", wordId=" + wordId);
						context.getVectorMetadata().addChiMergedTerm(word, term);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}

}
