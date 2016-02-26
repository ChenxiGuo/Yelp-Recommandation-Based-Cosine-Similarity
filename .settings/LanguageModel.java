/**
 * 
 */
package analyzer;

//import java.awt.List;
import java.math.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.*;
import java.util.Map.Entry;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.Post;
import structures.Token;




/**
 * @author 
 * Sample codes for demonstrating OpenNLP package usage 
 * NOTE: the code here is only for demonstration purpose, 
 * please revise it accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer {
	
	private static final float Integer = 0;
	//a list of 
	HashSet<String> m_stopwords;
	HashSet<String> m_ctrlVocabulary;
	PrintWriter dfExl, ttfExl;
	//you can store the loaded reviews in this  for further processing
	ArrayList<Post> m_reviews;
	ArrayList<Post> m_sample;
	ArrayList<TreeMap<Double, Integer>> Sml_Result = new ArrayList<TreeMap<Double, Integer>>();
	ArrayList<HashMap<String, Integer>> Q_tfMap = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Double>> Q_tfMap_tfidf = new ArrayList<HashMap<String, Double>>();
	ArrayList<Double> R_module = new ArrayList<Double>();
	ArrayList<Double> Q_module = new ArrayList<Double>();
	ArrayList<HashMap<String,Integer>> R_tfMap = new ArrayList<HashMap<String, Integer>>();
	ArrayList<HashMap<String, Double>> R_tfMap_tfidf = new ArrayList<HashMap<String, Double>>();
	ArrayList<Post> m_query;
	public static Tokenizer tokenizer;
	Map<String, Integer> ttfMap = new HashMap<>();
	Map<String, Token> tokenMap = new HashMap<>();
	Map<String, Token> tokenSampleMap = new HashMap<>();	
	//you might need something like this to store the counting statistics for validating Zipf's and computing IDF
	//HashMap<String, Token> m_stats;	
	
	//we have also provided sample implementation of language model in src.structures.LanguageModel
	
	public DocAnalyzer() {
		m_reviews = new ArrayList<Post>();
		m_query = new ArrayList<Post>();
		m_sample = new ArrayList<Post>();
		
	}
	
	//sample code for loading a list of stopwords from file
	//you can manually modify the stopword file to include your newly selected words
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				//it is very important that you perform the same processing operation to the loaded stopwords
				//otherwise it won't be matched in the text content
				line = SnowballStemmingDemo(NormalizationDemo(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
					//System.out.format("%s\n", line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", m_stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
	
	public void analyzeDocumentDemo(JSONObject json, Tokenizer tokenizer, int id) {		
		try {
			
			JSONArray jarray = json.getJSONArray("Reviews");
			for(int i=0; i<jarray.length(); i++) {
			//for(int i=0; i<1; i++) {
				////System.out.format("%s\n", jarray.getJSONObject(i));
				Post review = new Post(jarray.getJSONObject(i));
				
					//System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
					StringBuffer strBanalyzedContent = new StringBuffer();
					for(String token:tokenizer.tokenize(review.getContent())){
						//if(!m_stopwords.contains(token)){
							token = NormalizationDemo(token);
							token = SnowballStemmingDemo(token);
							//token = PorterStemmingDemo(token);
							strBanalyzedContent.append(token);
							strBanalyzedContent.append(" ");
						//}
					}
					String strAnalyzedContent = strBanalyzedContent.toString();
					review.setContent(strAnalyzedContent);
					//System.out.format("%s\n", review.getContent());
				
				if(id == 0)
				m_reviews.add(review);
				else if(id ==1){
				m_sample.add(review);
				
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void analyzeQuery(JSONObject json, Tokenizer tokenizer) {		
		try {
			
			JSONArray jarray = json.getJSONArray("Reviews");
			for(int i=0; i<jarray.length(); i++) {
			//for(int i=0; i<1; i++) {
				////System.out.format("%s\n", jarray.getJSONObject(i));
				Post review = new Post(jarray.getJSONObject(i));
				
					//System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
					StringBuffer strBanalyzedContent = new StringBuffer();
					for(String token:tokenizer.tokenize(review.getContent())){
						//if(!m_stopwords.contains(token)){
							token = NormalizationDemo(token);
							token = SnowballStemmingDemo(token);
							//token = PorterStemmingDemo(token);
							strBanalyzedContent.append(token);
							strBanalyzedContent.append(" ");
						//}
					}
					String strAnalyzedContent = strBanalyzedContent.toString();
					review.setContent(strAnalyzedContent);
					//System.out.format("%s\n", review.getContent());
				
				
				m_query.add(review);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
	// calculating TTF and DF
		public Map<String, Integer> calTTFDF(){
			
			
			//ArrayList<String> tokens = new ArrayList<String>();
			Token tokentype;
			int reviewSize = m_reviews.size();
			String bigram;
			for(int i=0; i<reviewSize; i++){
				
				ArrayList<String> tokensinDoc = new ArrayList<String>();
				//System.out.format("review %d is used to calculate df\n", i);
				String pre="";
				for(String token:tokenizer.tokenize(m_reviews.get(i).getContent())){
					
					if(m_ctrlVocabulary.contains(token) && !m_stopwords.contains(token)) {
						
						if (!tokenMap.containsKey(token)){
							//ttfMap.put(token, 1);
							tokentype = new Token(token);
							tokenMap.put(token, tokentype);
							//tokenMap.get(token).setTTFValue(1);
							
							//tokens.add(token);
							
						}	
						//else{
							//int newValue = Map.get(token).getTFFValue() + 1;
							//tokenMap.get(token).setTTFValue(newValue);
							//ttfMap.remove(token);
							//ttfMap.put(token, newValue);
						//}
						
						if(!tokensinDoc.contains(token))
							tokensinDoc.add(token);
					}	
						//bigram
						bigram=pre+" "+token;
						if(m_ctrlVocabulary.contains(bigram) && !m_stopwords.contains(bigram)){
							if (!tokenMap.containsKey(bigram)){
							      //ttfMap.put(bigram, 1);
							      tokentype = new Token(bigram);
							      tokenMap.put(bigram, tokentype);
							      //tokenMap.get(bigram).setTTFValue(1);
							      //tokens.add(bigram);
							}
						if(!tokensinDoc.contains(bigram))
							tokensinDoc.add(bigram);
						}
						pre=token;
					
				}
				for(String str: tokensinDoc){
					int newDF = tokenMap.get(str).getDFValue() + 1;
					tokenMap.get(str).setDFValue(newDF);
				}	
			}
			return ttfMap;
			
		}

public void calSimilarity(){
	int reviewSize = m_reviews.size();
	int querySize = m_query.size();
	for(int i=0;i<querySize;i++){
		double sqrt_q = 0;
		HashMap<String, Double> cquery_tfidf = new HashMap<String, Double>();
		Set<Entry<String, Integer>> entries_query = Q_tfMap.get(i).entrySet();
		Iterator<Entry<String, Integer>> iterator = entries_query.iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry =iterator.next();
			String token = entry.getKey();
			System.out.format("%s!!!!!!!!!!\n",token);
			if(m_ctrlVocabulary.contains(token) ){
				double tfidf = Q_tfMap.get(i).get(token) * (1 + ( Math.log ( querySize / tokenSampleMap.get(token).getDFValue() ) / Math.log(10)));
				cquery_tfidf.put(token,tfidf);
				sqrt_q += Math.pow(tfidf, 2);
			}
		}
		Q_module.add(i,sqrt_q);
		Q_tfMap_tfidf.add(i,cquery_tfidf);
		
	}
	
	double sqrt_r;
	for(int i=0;i<reviewSize;i++){
		sqrt_r = 0;
		HashMap<String, Double> creview_tfidf = new HashMap<String, Double>();
		Set<Entry<String, Integer>> entries_review = R_tfMap.get(i).entrySet();
		Iterator<Entry<String, Integer>> iterator = entries_review.iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry =iterator.next();
			String token = entry.getKey();
			if(m_ctrlVocabulary.contains(token) ){
				double tfidf = R_tfMap.get(i).get(token) * (1 + ( Math.log ( reviewSize / tokenMap.get(token).getDFValue() ) / Math.log(10)));
				creview_tfidf.put(token,tfidf);
				sqrt_r += Math.pow(tfidf, 2);
			}
		}
		R_module.add(i,sqrt_r);
		R_tfMap_tfidf.add(i,creview_tfidf);
	}
	
	double tfidf_q = 0;
	double tfidf_r = 0;
	for(int i=0;i<querySize;i++){
		
		Set<Entry<String, Double>> entries = Q_tfMap_tfidf.get(i).entrySet();
		TreeMap<Double,Integer> similarity = new TreeMap<Double,Integer>();
		
		for(int j=0; j<reviewSize; j++){
			Iterator<Entry<String, Double>> iterator = entries.iterator();
			double sum = 0;
			while(iterator.hasNext()) {
				Entry<String, Double> entry =iterator.next();
				
				if( m_ctrlVocabulary.contains(entry.getKey()) && R_tfMap_tfidf.get(j).containsKey(entry.getKey())){	
					
					tfidf_q = entry.getValue();
					tfidf_r = R_tfMap_tfidf.get(j).get(entry.getKey());
					sum+=tfidf_r*tfidf_q;
				}
			}
			similarity.put(sum/(Math.sqrt(R_module.get(j)) * Math.sqrt(Q_module.get(i))),j);	
		}
		
		Sml_Result.add(similarity);
		
		int three = 0;
		NavigableMap<Double, java.lang.Integer> similarity_re = similarity.descendingMap();
		Set<Double> keySet = similarity_re.keySet(); 
		for (Double key : keySet ) {
		   if (three++ == 4) {
		      break;
		    }
		  System.out.format("Document %d, No. %d %s\n",i+1, three-1, m_reviews.get(similarity_re.get(key)).getContent());
		}
	}
	
}
			
		public void calSampleDF(){
				
				
			//ArrayList<String> tokens = new ArrayList<String>();
			Token tokentype;
			int sampleSize = m_sample.size();
		/*	for(int i=0; i<5; i++){
				System.out.format("%s\n",m_sample.get(i).getContent());
				System.out.format("============checking m_query and m_sample===========\n");
			}
			*/
			System.out.format("%d", sampleSize);
			String bigram;
			for(int i=0; i<sampleSize; i++){
				
				ArrayList<String> tokensinDoc = new ArrayList<String>();
				//System.out.format("review %d is used to calculate df\n", i);
				String pre="";
				for(String token:tokenizer.tokenize(m_sample.get(i).getContent())){
					
					if(m_ctrlVocabulary.contains(token) && !m_stopwords.contains(token)) {
						
						if (!tokenSampleMap.containsKey(token)){
							tokentype = new Token(token);
							tokenSampleMap.put(token, tokentype);
						}	
						
						if(!tokensinDoc.contains(token))
							tokensinDoc.add(token);
					}
						//bigram
						bigram=pre+" "+token;
						
						if (m_ctrlVocabulary.contains(bigram) && !m_stopwords.contains(bigram)){
							if (!tokenSampleMap.containsKey(bigram)){
						      tokentype = new Token(bigram);
						      tokenSampleMap.put(bigram, tokentype);
							}
							
							if(!tokensinDoc.contains(bigram))
								tokensinDoc.add(bigram);
						}
						pre=token;
				
				}
				
				for(String str: tokensinDoc){
					int newDF = tokenSampleMap.get(str).getDFValue() + 1;
					tokenSampleMap.get(str).setDFValue(newDF);
					//System.out.format("token is %s\n",str);
					//System.out.format("\t @@@@@@@This bigram is in %d document\n", tokenSampleMap.get(str).getDFValue());
				}	
			}
			
		/*	for(int i=0; i<sampleSize; i++){
				int j =0;
				Set<Entry<String, Token>> entries_sdf = tokenSampleMap.entrySet();
				if(entries_sdf != null) {
					Iterator<Entry<String, Token>> iterator = entries_sdf.iterator();
					while(iterator.hasNext()) {
						Entry<String, Token> entry_sdf =iterator.next();
						System.out.format("DF: %s\t%d\n",entry_sdf.getKey(), entry_sdf.getValue().getDFValue());
						j++;
					}	
				}
			}
			*/
		}
		
		public void ctrlVocabulary(String filename_v){
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename_v), "UTF-8"));
				String line;

				while ((line = reader.readLine()) != null) {
					//it is very important that you perform the same processing operation to the loaded stopwords
					//otherwise it won't be matched in the text content
					//line = SnowballStemmingDemo(NormalizationDemo(line));
					if (!line.isEmpty())
						m_ctrlVocabulary.add(line);
						//System.out.format("%s\n", line);
				}
				reader.close();
				//System.out.format("Loading %d Control Vocabulary from %s\n", m_stopwords.size(), filename_v);
			} catch(IOException e){
				System.err.format("[Error]Failed to open file %s!!", filename_v);
			}
			
		}
	
		public void exportExl(){
			File file_ub_df = new File("/Users/guochenxi/Documents/workspace/ubdf.xls");
			try {
				dfExl = new PrintWriter(new FileOutputStream(file_ub_df));
				
				Set<Entry<String, Token>> entries_ub_df = tokenMap.entrySet();
				
				if(entries_ub_df != null) {
					Iterator<Entry<String, Token>> iterator = entries_ub_df.iterator();
					while(iterator.hasNext()) {
						
						Entry<String, Token> entry_ub_df =iterator.next();
						if(entry_ub_df.getValue().getDFValue()>50)
							dfExl.printf(entry_ub_df.getKey()+ "\t"+entry_ub_df.getValue().getDFValue()+"\n");
						
					}
					//dfExl.close;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	

			/*File file_ttf = new File("/Users/guochenxi/Documents/workspace/ttf.xls");
			try {
				ttfExl = new PrintWriter(new FileOutputStream(file_ttf));
				
				Set<Entry<String, Integer>> entries_ttf = ttfMap.entrySet();
				
				if(entries_ttf != null) {
					Iterator<Entry<String, Integer>> iterator = entries_ttf.iterator();
					while(iterator.hasNext()) {
						
						Entry<String, Integer> entry_ttf =iterator.next();
						ttfExl.printf(entry_ttf.getKey()+ "\t"+entry_ttf.getValue()+"\n");
						
					}
					//dfExl.close;
				
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
		}
	
		public void calQueryTF(){
			
			
			//int reviewSize = m_reviews.size();
			int querySize = m_query.size();
			/*
			for(int i=0; i<5; i++){
				System.out.format("%s\n",m_query.get(i).getContent());
				System.out.format("============checking m_query and m_sample===========\n");
			}
			*/
			for(int i=0; i<querySize; i++){
				String bigram;
				String pre="";
				HashMap<String, Integer> cquery = new HashMap<String, Integer>();
				for(String token:tokenizer.tokenize(m_query.get(i).getContent())){
					if (m_ctrlVocabulary.contains(token) && !m_stopwords.contains(token)){
						if(!cquery.containsKey(token)){
							cquery.put(token, 1);
						}
						else{
							int newValue = cquery.get(token) + 1;
							cquery.remove(token);
							cquery.put(token, newValue);
						}
					}
					bigram=pre+" "+token;
					if (m_ctrlVocabulary.contains(bigram) && !m_stopwords.contains(bigram)){
						
						if(!cquery.containsKey(bigram)){
							cquery.put(bigram, 1);
						}
						else{
							int newValue = cquery.get(bigram) + 1;
							cquery.remove(bigram);
							cquery.put(bigram, newValue);
						}
					}
					pre=token;
					
				}
				Q_tfMap.add(cquery);
				
			}
			
		/*	for(int i=0; i<querySize; i++){
				Set<Entry<String, Integer>> entries_qv = Q_tfMap.get(i).entrySet();
				if(entries_qv != null) {
					Iterator<Entry<String, Integer>> iterator = entries_qv.iterator();
					while(iterator.hasNext()) {
						Entry<String, Integer> entry_qv =iterator.next();
						System.out.format("TF^^^^^^");
						System.out.format("TF: %s\t%s\n",entry_qv.getKey(), entry_qv.getValue());
					}	
				}
			}
			*/
		}
				
public void calReviewTF(){
			
			//int reviewSize = m_reviews.size();
			System.out.format("*******Calculating ReviewTF******\n");
			int reviewSize = m_reviews.size();
			//for(int i=0; i<5; i++){
			//	System.out.format("%s\n",m_query.get().getContent());
			//	System.out.format("============checking m_query and m_sample===========\n");
			//}
			for(int i=0; i<reviewSize; i++){
				String bigram;
				String pre="";
				HashMap<String, Integer> creview = new HashMap<String, Integer>();
				for(String token: tokenizer.tokenize(m_reviews.get(i).getContent())){
					if (m_ctrlVocabulary.contains(token) && !m_stopwords.contains(token)){
						if(!creview.containsKey(token)){
							creview.put(token, 1);
						}
						else{
							int newValue = creview.get(token) + 1;
							creview.remove(token);
							creview.put(token, newValue);
						}
					}
					
						bigram=pre+" "+token;
						if (m_ctrlVocabulary.contains(bigram) && !m_stopwords.contains(bigram)){
							
							if(!creview.containsKey(bigram)){
								creview.put(bigram, 1);
							}
						 
							else{
								int newValue = creview.get(bigram) + 1;
								creview.remove(bigram);
								creview.put(bigram, newValue);
							}
						}
						pre=token;
					
				}
				R_tfMap.add(creview);
				
				//System.out.format("######Add a vector into R_tfMap#####\n");
			}
			
			/*for(int i=0; i<reviewSize; i++){
				
				Set<Entry<String, Integer>> entries_qv = R_tfMap.get(i).entrySet();
				if(entries_qv != null) {
					int j = 0;
					Iterator<Entry<String, Integer>> iterator = entries_qv.iterator();
					while(iterator.hasNext() && j<20) {
						j++;
						Entry<String, Integer> entry_qv =iterator.next();
						System.out.format("%s\t%s\n",entry_qv.getKey(), entry_qv.getValue());
					}	
				}
				else{
					System.out.format("_______IS Null______\n");
				}
			}*/
		}
			
		/*	public void calReviewVector(){
				
				
				int reviewSize = m_reviews.size();
				int querySize = m_query.size();
				for(int i=0; i<reviewSize; i++){
					HashMap<String, Integer> tfMap = new HashMap<String, Integer>();
					String bigram;
					String pre="";
					for(String token:tokenizer.tokenize(m_query.get(i).getContent())){
						if (m_ctrlVocabulary.contains(token)){
							if(!tfMap.containsKey(token)){
								tfMap[i].put(token, 1);
							}
							else{
								int newValue = tfMap[i].get(token) + 1;
								tfMap[i].remove(token);
								tfMap[i].put(token, newValue);
							}
						}
						
						bigram=pre+" "+token;
						if (!m_ctrlVocabulary.contains(bigram)){
							if(!tfMap[i].containsKey(bigram)){
								tfMap[i].put(bigram, 1);
							}
						 
							else{
								int newValue = tfMap[i].get(bigram) + 1;
								tfMap[i].remove(bigram);
								tfMap[i].put(bigram, newValue);
							}
						
							pre=token;
						}
						
					}
				}
			
		} */
		
		
		
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	// sample code for demonstrating how to recursively load files in a directory 
	public void LoadDirectory(String folder, String suffix, int id) {
		File dir = new File(folder);
		int size = m_reviews.size();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocumentDemo(LoadJson(f.getAbsolutePath()), tokenizer, id);
				System.out.format("File %s is analyzed\n", f.getName());
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix, id);
		}
		size = m_reviews.size() - size;
		//System.out.println("Loading " + size + " review documents from " + folder);
	}

	//sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to perform text normalization
	public String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\pP|\\pS", ""); 
		
		// convert to lower case
		token = token.toLowerCase(); 
		token = token.replaceAll("\\pN+", "NUM"); 
		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol "NUM"
		
		return token;
	}
	
	public void TokenizerDemon(String text, Tokenizer tokenizer) {
		
			/**
			 * HINT: instead of constructing the Tokenizer instance every time when you perform tokenization,
			 * construct a global Tokenizer instance once and evoke it everytime when you perform tokenization.
			*/
			System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
			for(String token:tokenizer.tokenize(text)){
				System.out.format("%s\t%s\t%s\t%s\n", token, NormalizationDemo(token), SnowballStemmingDemo(token), PorterStemmingDemo(token));
			}
		
		
	}
	
	public static void main(String[] args) {
		
		
		try {
			
			/**
			 * HINT: instead of constructing the Tokenizer instance every time when you perform tokenization,
			 * construct a global Tokenizer instance once and evoke it everytime when you perform tokenization.
			 */
			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("/Users/guochenxi/Documents/java/Model/en-token.bin")));
			
			//System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
			
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	
		DocAnalyzer analyzer = new DocAnalyzer();
		
		//codes for demonstrating tokenization and stemming
		
		//codes for loading json file
		
		//analyzer.calculateDF();
		//when we want to execute it in command line
		analyzer.m_stopwords = new HashSet<String>();
		analyzer.m_ctrlVocabulary = new HashSet<String>();
		analyzer.LoadStopwords("/Users/guochenxi/Documents/workspace/stopwords.txt");
		analyzer.ctrlVocabulary("/Users/guochenxi/Documents/workspace/ctrl_vocab.txt");
		analyzer.LoadDirectory("/Users/guochenxi/Documents/java/yelp", ".json", 0);
		analyzer.LoadDirectory("/Users/guochenxi/Documents/workspace/MP1/data/samples1", ".json", 1);
		
		analyzer.calTTFDF();
		analyzer.calReviewTF();
		//analyzer.exportExl();
		
		analyzer.analyzeQuery(analyzer.LoadJson("/Users/guochenxi/Documents/workspace/MP1/data/samples/query.json"), tokenizer);
		analyzer.calQueryTF();
		analyzer.calSampleDF();
		
		analyzer.calSimilarity();
	}

}
