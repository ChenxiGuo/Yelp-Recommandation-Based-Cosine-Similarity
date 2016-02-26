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
import java.util.ArrayList;  
import java.util.Arrays;  
import java.util.HashSet;  
import java.util.List;  
import java.util.Set;  


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
/*class ValueComparator implements Comparator<Map.Entry<String, Double>>    
{    
    public int compare(Map.Entry<String, Double> mp1, Map.Entry<String, Double> mp2)     
    {    
        if((mp1.getValue() - mp2.getValue()) > (double)0) 
        	return 1;
        if((mp1.getValue() - mp2.getValue()) > (double)0)
        	return -1;
        else return 0;
        
    }    
}*/

/*class ValueComparator implements Comparator<Map.Entry<String, Double>>   
{
    HashMap<String, Double> base;

    public ValueComparator(HashMap base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
        if (a.getValue() <= b.getValue()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}*/

class ValueComparator implements Comparator<String> {
	 
    Map<String, Double> map;
 
    public ValueComparator(Map<String, Double> base) {
        this.map = base;
    }
 
    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }
}

public class DocAnalyzer {
		
	private static final double Integer = 0;
	static String testString = "good";
	int totalWords = 0;
	int UniVSize = 0;
	
	HashSet<String> m_stopwords;
	HashSet<String> m_ctrlVocabulary;
	PrintWriter dfExl, ttfExl;
	//you can store the loaded reviews in this  for further processing
	Map<String, Integer> testBigramMap = new HashMap<>();
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
	HashMap<String, HashSet<String>> UniBiMap = new HashMap<String, HashSet<String>>();
	HashMap<String, HashSet<String>> preNextMap = new HashMap<String, HashSet<String>>();
	Map<String, Integer> ttfUniMap = new HashMap<>();
	Map<String, Integer> ttfBiMap = new HashMap<>();
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
		public void calTTFDF(){
			
			int reviewSize = m_reviews.size();
			String bigram;
			for(int i=0; i<reviewSize; i++){
				//System.out.format("review %d is used to calculate df\n", i);
				String pre="";
				for(String token:tokenizer.tokenize(m_reviews.get(i).getContent())){
					totalWords++;
					if(!ttfUniMap.containsKey(token)){ 
						ttfUniMap.put(token,1);
						UniVSize++;
					}
					else ttfUniMap.put(token, ttfUniMap.get(token) + 1);
					//bigram
					bigram=pre+" "+token;
					if (!ttfBiMap.containsKey(bigram)) ttfBiMap.put(bigram, 1);
					else ttfBiMap.put(token, ttfBiMap.get(bigram) + 1);
					
					if(!UniBiMap.containsKey(pre)){
						HashSet<String> BiSet = new HashSet<String>();
						HashSet<String> NextSet = new HashSet<String>();
						UniBiMap.put(pre,BiSet);
						preNextMap.put(pre, NextSet);
						BiSet.add(bigram);
						NextSet.add(token);
					}
					else{
						if(!UniBiMap.get(pre).contains(bigram)){
							UniBiMap.get(pre).add(bigram);
							preNextMap.get(pre).add(token);
						}
					}
					pre=token;
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public void calBiString(String s){
			HashMap<String, Double> biPa = new HashMap<String,Double>();
			HashMap<String, Double> biPl = new HashMap<String,Double>();
			ValueComparator sv_pa = new ValueComparator(biPa);
			ValueComparator sv_pl = new ValueComparator(biPl);
			TreeMap sorted_map_pa = new TreeMap(sv_pa);
			TreeMap sorted_map_pl = new TreeMap(sv_pl);
			for(Map.Entry MapUnitEntry : ttfUniMap.entrySet()){
			//for(String bi: preNextMap.get(s)){
				String next = (String) MapUnitEntry.getKey();
				biPa.put(next, BiPa(s,next));
			}
			for(Map.Entry MapUnitEntry : ttfUniMap.entrySet()){
			//for(String bi: preNextMap.get(s)){
				String next = (String) MapUnitEntry.getKey();
				biPl.put(next, BiPl(s,next));
			}
			sorted_map_pa.putAll(biPa);
			sorted_map_pl.putAll(biPl);
			int i=0;
			Set keys = sorted_map_pa.keySet();
			for (Iterator it = keys.iterator(); it.hasNext() && i<10;) {
				String key = (String) it.next();
				Double value = biPa.get(key);
				System.out.println(i + ": " + "good" + key + " = " + value);
				i++;
			}
			Set keys_2 = sorted_map_pl.keySet();
			i = 0;
			for (Iterator it = keys_2.iterator(); it.hasNext() && i<10;) {
				String key = (String) it.next();
				Double value = biPl.get(key);
				System.out.println(i + ": " + "good" + key + " = " + value);
				i++;
			}
		}
		
		public void createDoc(){
			HashMap<Integer,String> samUniMap = new HashMap<Integer,String>();
			int uniIndex = 0;
			for(Map.Entry<String, Integer> uni: ttfUniMap.entrySet()){
				uniIndex += uni.getValue();
				samUniMap.put(uniIndex, uni.getKey());
				//System.out.format("Doc %d now is %s\n",uniIndex,samUniMap.get(uniIndex));
			}
			Random random = new Random();
			for(int i=0;i<10;i++){
				double product = (double) 1;
				StringBuffer doc = new StringBuffer();
				for(int j=0;j<20;j++){
					int max=uniIndex;
			        int ranNum = random.nextInt(max);
			        while(samUniMap.get(ranNum) == null)
			        {
			        	ranNum++;
			        }
			        //System.out.format("random number is %d\n", ranNum);
			        //System.out.format("selected word is %s\n",samUniMap.get(ranNum));
			        product = product*ttfUniMap.get(samUniMap.get(ranNum))/uniIndex;
			        doc.append(' ');
			        doc.append(samUniMap.get(ranNum));
			        //System.out.format("Doc %d now is %s\n",i,samUniMap.get(ranNum));
			        
				}
				doc.toString();
				System.out.println(product);
				System.out.println(doc);
			}
		}
		
		public void createDocPa(){
			HashMap<Integer,String> samUniMap = new HashMap<Integer,String>();
			int uniIndex = 0;
			for(Map.Entry<String, Integer> uni: ttfUniMap.entrySet()){
				uniIndex += uni.getValue();
				samUniMap.put(uniIndex, uni.getKey());
				//System.out.format("Doc %d now is %s\n",uniIndex,samUniMap.get(uniIndex));
			}
			Random random = new Random();
			
			for(int i=0;i<10;i++){
				
				//create begin word
				double product = 1;
				StringBuffer doc = new StringBuffer();
				int max=uniIndex;
		        int ranNum = random.nextInt(max);
		        while(samUniMap.get(ranNum) == null)
		        {
		        	ranNum++;
		        }
		        doc.append(samUniMap.get(ranNum));
		        product = product*ttfUniMap.get(samUniMap.get(ranNum))/uniIndex;
		        //System.out.format("uniIndex is %d, product is %s\n",uniIndex,product);
		        
		        //create the following word by Pa
		        double biIndex = 0;
		        String pre = samUniMap.get(ranNum);
				for(int j=0;j<19;j++){
					HashMap<Double, String> biSetMap = new HashMap<Double, String>();
					double ranDouble =(double) Math.random();
					//System.out.format("random number is %f\n", ranDouble);
					for(String next: preNextMap.get(pre)){//? //preNextMap.get(pre) is a bigram set of pre
						double BiPa = BiPa(pre,pre+" "+ next);
						biIndex += BiPa;
						biSetMap.put(biIndex, next);
						//System.out.format("Doc %d now is %s\n",uniIndex,samUniMap.get(uniIndex));
					}
					//selecting word from set of pre
					int setSize = biSetMap.size();
					int si = 0;
					for(Map.Entry<Double, String> paNext: biSetMap.entrySet()){
						if(si == setSize-1){
							doc.append(' ');
							doc.append(paNext.getValue()); 
							String bigram = pre+" "+ paNext.getValue();
							product*=BiPa(pre,bigram);
							pre = paNext.getValue();
							//System.out.println(product);
							break;
						}
						else if(paNext.getKey() < ranDouble) ;
						else{
							doc.append(' ');
							doc.append(paNext.getValue()); 
							String bigram = pre+" "+ paNext.getValue();
							product*=BiPa(pre,bigram);
							//System.out.println(product);
							pre = paNext.getValue();						
							break;
						}
						si++;
					}
					
				}
				doc.toString();
				System.out.println(product);
				System.out.println(doc);
			}
		}
		
		public void createDocPl(){
			HashMap<Integer,String> samUniMap = new HashMap<Integer,String>();
			int uniIndex = 0;
			for(Map.Entry<String, Integer> uni: ttfUniMap.entrySet()){
				uniIndex += uni.getValue();
				samUniMap.put(uniIndex, uni.getKey());
				//System.out.format("Doc %d now is %s\n",uniIndex,samUniMap.get(uniIndex));
			}
			Random random = new Random();
			
			for(int i=0;i<10;i++){
				
				//create begin word
				double product = 1;
				StringBuffer doc = new StringBuffer();
				int max=uniIndex;
		        int ranNum = random.nextInt(max);
		        while(samUniMap.get(ranNum) == null)
		        {
		        	ranNum++;
		        }
		        doc.append(samUniMap.get(ranNum));
		        product = product*ttfUniMap.get(samUniMap.get(ranNum))/uniIndex;
		        //System.out.format("uniIndex is %d, product is %s\n",uniIndex,product);
		        
		        //create the following word by Pl
		        double biIndex = 0;
		        String pre = samUniMap.get(ranNum);
				for(int j=0;j<19;j++){
					HashMap<Double, String> biSetMap = new HashMap<Double, String>();
					double ranDouble =(double) Math.random();
					//System.out.format("random number is %f\n", ranDouble);
					for(String next: preNextMap.get(pre)){//? //preNextMap.get(pre) is a bigram set of pre
						double BiPl = BiPl(pre,pre+" "+ next);
						biIndex += BiPl;
						biSetMap.put(biIndex, next);
						//System.out.format("Doc %d now is %s\n",uniIndex,samUniMap.get(uniIndex));
					}
					//selecting word from set of pre
					int setSize = biSetMap.size();
					int si = 0;
					for(Map.Entry<Double, String> plNext: biSetMap.entrySet()){
						if(si == setSize-1){
							doc.append(' ');
							doc.append(plNext.getValue()); 
							String bigram = pre+" "+ plNext.getValue();
							product*=BiPl(pre,bigram);
							pre = plNext.getValue();
							//System.out.println(product);
							break;
						}
						else if(plNext.getKey() < ranDouble) ;
						else{
							doc.append(' ');
							doc.append(plNext.getValue()); 
							String bigram = pre+" "+ plNext.getValue();
							product*=BiPl(pre,bigram);
							//System.out.println(product);
							pre = plNext.getValue();						
							break;
						}
						si++;
					}
					
				}
				doc.toString();
				System.out.println(product);
				System.out.println(doc);
			}
		}
		
		
		public double BiPl(String pre, String next){
			String bi = pre+' '+next;
			double bicount = 0;
			if(ttfBiMap.get(bi)!= null)
				bicount = ttfBiMap.get(bi);
			
			double nextcount = 0;
			if(ttfUniMap.get(next) != null)
				nextcount = ttfUniMap.get(next);
		
			double precount;
			if(ttfUniMap.get(pre)!=null)
				precount = ttfUniMap.get(pre);
			else return 0.9*nextcount/(totalWords);
			
			double Pl = (double)(0.1*bicount/precount + 0.9*nextcount/(totalWords));
			//System.out.format("Pl is %f\n",Pl);
			return Pl;
		}
		public double BiPa(String pre, String next){
			String bi = pre+' '+next;
			//System.out.format("Pa is %f\n",Pa);
			
			//System.out.println("ttfBiMap:");
			//System.out.println(ttfBiMap.get(bi));
			//System.out.println("ttfUniMap:");
			//System.out.println(ttfUniMap.get(next));
			//System.out.println("ttfUniMap:");
			//System.out.println(ttfUniMap.get(pre));
			double bicount = 0;
			if(ttfBiMap.get(bi)!= null)
				bicount = ttfBiMap.get(bi);
			
			double nextcount = 0;
			if(ttfUniMap.get(next) != null)
				nextcount = ttfUniMap.get(next);
			
			int du =0;
			if(UniBiMap.get(pre) != null)
				du = UniBiMap.get(pre).size();
			
			double precount;
			if(ttfUniMap.get(pre)!=null)
				precount = ttfUniMap.get(pre);
			else  return 0;            //////////////temporary
			
			double Pa = (double)( (Math.max(bicount-0.1, 0) + 0.1*du*nextcount/(totalWords) ) / precount );
			//System.out.format("Pa is %f\n",Pa);
			return Pa;
			
		}
		
		public void calPP(){
			int testSize = m_sample.size();
			System.out.println("m_sample.size");
			System.out.println(m_sample.size());
			double[] PPu= new double[testSize];
			double[] PPa= new double[testSize];
			double[] PPl= new double[testSize];
			double[] PPd= new double[testSize];
			for(int i=0; i<testSize; i++){
				PPu[i] = 0; PPa[i] = 0; PPl[i] = 0; PPd[i] = 0;
				String pre="is";
				//double precountpr = 1;
				int tokennum = 0;
				//HashMap<String, Integer> cquery = new HashMap<String, Integer>();
				for(String token:tokenizer.tokenize(m_sample.get(i).getContent())){
					tokennum++;
				}
				
				for(String token:tokenizer.tokenize(m_sample.get(i).getContent())){
					
					double tokencount = 0;
					if(ttfUniMap.get(token) != null)
						tokencount = ttfUniMap.get(token);
					
					double PwNew = (tokencount+0.1)/(totalWords +0.1*(ttfUniMap.size()+1));
					PPu[i] -= Math.log(PwNew)/tokennum;
					System.out.format("test file %d PPu[%d]:\n",i,i);
					System.out.println(PPu[i]);
					
					int bicount = 0;
					if(ttfBiMap.get(pre+" "+token)!=null)
						bicount =ttfBiMap.get(pre+" "+token);
					
					int precount = 1;
					if(ttfUniMap.get(pre)!=null)
						precount = ttfUniMap.get(pre);
					
					double du =10*precount;
					if(UniBiMap.get(pre) != null)
						du = UniBiMap.get(pre).size();
					
					//System.out.format("pre is %s:\n",pre);
					double PaNew =(double)( (Math.max(bicount-0.1, 0) + 0.1*du*PwNew) / precount);
					double PlNew = (double)(0.1*bicount/precount + 0.9*PwNew);
					double PdNew = (double)(bicount+0.1*PwNew)*(totalWords/totalWords+0.1);
					//System.out.format("test file %d token k: PaNew:\n",i);
					//System.out.println(PaNew);
					System.out.format("test file %d token k: PlNew:\n",i);
					System.out.println(PlNew);
					PPa[i] -= Math.log(PaNew)/tokennum;
					PPl[i] -= Math.log(PlNew)/tokennum;
					PPd[i] -= Math.log(PdNew)/tokennum;
					System.out.format("test file %d PPa[%d]:\n",i,i);
					System.out.println(PPa[i]);
					System.out.format("test file %d PPl[%d]:\n",i,i);
					System.out.println(PPl[i]);
					System.out.format("test file %d PPd[%d]:\n",i,i);
					System.out.println(PPd[i]);
					
					pre=token;
				}
				PPu[i] = Math.pow(Math.E,PPu[i]);
				PPa[i] = Math.pow(Math.E,PPa[i]);
				PPl[i] = Math.pow(Math.E,PPl[i]);
				PPd[i] = Math.pow(Math.E,PPl[i]);
			}
			double PPuMean = 0, PPaMean = 0, PPlMean = 0, PPdMean = 0;
			for(int j=0;j<testSize;j++){
				PPuMean += PPu[j];
				PPaMean += PPa[j];
				PPlMean += PPl[j];
				PPdMean += PPd[j];
			}
			PPuMean = PPuMean/testSize;
			PPaMean = PPaMean/testSize;
			PPlMean = PPlMean/testSize;
			PPdMean = PPdMean/testSize;
			
			double PPuVar = 0, PPaVar = 0, PPlVar = 0, PPdVar = 0;
			for(int j=0;j<testSize;j++){
				PPuVar += Math.pow((PPu[j]-PPuMean), 2);
				PPaVar += Math.pow((PPa[j]-PPaMean), 2);
				PPlVar += Math.pow((PPl[j]-PPlMean), 2);
				PPdVar += Math.pow((PPd[j]-PPdMean), 2);
			}
			PPuVar = PPuMean/testSize;
			PPaVar = PPaMean/testSize;
			PPlVar = PPlMean/testSize;
			PPdVar = PPdMean/testSize;
			System.out.println("PPuMean:");
			System.out.println(PPuMean);
			System.out.println("PPaMean:");
			System.out.println(PPaMean);
			System.out.println("PPlMean:");
			System.out.println(PPlMean);
			System.out.println("PPdMean:");
			System.out.println(PPdMean);
			System.out.println("PPuVar:");
			System.out.println(PPuVar);
			System.out.println("PPaVar:");
			System.out.println(PPaVar);
			System.out.println("PPlVar:");
			System.out.println(PPlVar);
			System.out.println("PPdVar:");
			System.out.println(PPdVar);
			
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
			int querySize = m_query.size();
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
		// add a line to recognize integers and  s via regular expression
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
	
	public double calListSimi(double[] user, double[] news){
		int length = user.length;
		int i;
		double productSum = 0, userSum = 0, newsSum = 0;
		
		for(i=0;i<length;i++){
			productSum += user[i]*news[i];
			userSum += Math.pow(user[i], 2);
			newsSum += Math.pow(news[i], 2);
		}
		
		if(userSum!=0 && newsSum!=0){
			double similarity = productSum/(Math.sqrt(userSum)*Math.sqrt(newsSum));
			System.out.println("similarity is "+ similarity+"\n");
			return similarity;
		}
		return 0;
	}

	public double recallAtM(int[] user, int[] news, int M){
		int UserN = user.length;
		int NewsN = news.length;
		int i, recall=0;
		HashSet<Integer> newsSet = new HashSet<Integer>();
		if(M>NewsN) M = NewsN;
		for(i=0;i<M;i++)
			newsSet.add(news[i]);
		for(int c: newsSet){
			System.out.println("c:"+ c);
		}
		System.out.println("M:"+ M);
		for(int ID: user){
			if(newsSet.contains(ID))
				{recall++;
				System.out.println("recall:"+ recall);
				}
		}
		System.out.println("final recall:"+ recall);
		System.out.println("userN:"+ UserN);
		double recallAtM = 0;
		
			recallAtM = recall/(double)UserN;
			System.out.format("recall is %f \n", recallAtM);
			return recall/UserN;

	}
	
	   
	   
	    
	        // you need treat n as an unsigned value
	    
	    	
	public static void main(String[] args) {
		try {
			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("/Users/guochenxi/Documents/java/Model/en-token.bin")));
			//System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	
		DocAnalyzer analyzer = new DocAnalyzer();
		
		//analyzer.calculateDF();
		//when we want to execute it in command line
		//analyzer.m_stopwords = new HashSet<String>();
		//analyzer.m_ctrlVocabulary = new HashSet<String>();
		//analyzer.LoadStopwords("/Users/guochenxi/Documents/workspace/stopwords.txt");
		//analyzer.ctrlVocabulary("/Users/guochenxi/Documents/workspace/ctrl_vocab.txt");
		//analyzer.LoadDirectory("/Users/guochenxi/Documents/workspace/MP1/data/samples1", ".json", 1);
		//analyzer.calReviewTF();
		//analyzer.exportExl();
		//analyzer.analyzeQuery(analyzer.LoadJson("/Users/guochenxi/Documents/workspace/MP1/data/samples/query.json"), tokenizer);
		//analyzer.calQueryTF();
		//analyzer.calSampleDF();
		
		//analyzer.calSimilarity();
		
		//analyzer.LoadDirectory("/Users/guochenxi/Documents/java/yelp", ".json", 0);
		//analyzer.calTTFDF();
		//analyzer.calBiString(testString);
		//analyzer.createDoc();
		//analyzer.createDocPa();
		//analyzer.createDocPl();
		//analyzer.calPL(testString,1);
		//analyzer.LoadDirectory("/Users/guochenxi/Documents/java/yelp_test", ".json", 1);
		//analyzer.calPP();
	}
}
