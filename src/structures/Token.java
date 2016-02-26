/**
 * 
 */
package structures;

/**
 * @author hongning
 * Suggested structure for constructing N-gram language model and vector space representation
 */
public class Token {

	int m_id; // the numerical ID you assigned to this token/N-gram
	public int getID() {
		return m_id;
	}

	public void setID(int id) {
		this.m_id = id;
	}

	String m_token; // the actual text content of this token/N-gram
	public String getToken() {
		return m_token;
	}

	public void setToken(String token) {
		this.m_token = token;
	}

	int  df_value; // frequency or probability of this token/N-gram
	public int getDFValue() {
		return df_value;
	}

	public void setDFValue(int value) {
		this.df_value =value;
	}
	
	int tf_value;
	public int getTFValue() {
		return tf_value;
	}

	public void setTFValue(int value) {
		this.tf_value =value;
	}
	
	int idf_value;
	public int getIDFValue() {
		return idf_value;
	}

	public void setIDFValue(int value) {
		this.idf_value =value;
	}
	
	int tfidf_value;
	public int getTFIDFValue() {
		return tfidf_value;
	}

	public void setTFIDFValue(int value) {
		this.tfidf_value =value;
	}
	
	int ttf_value;
	public int getTFFValue() {
		return ttf_value;
	}

	public void setTTFValue(int value) {
		this.ttf_value =value;
	}	
	
	//default constructor
	public Token(String token) {
		m_token = token;
		m_id = -1;
		df_value = 0;
		ttf_value = 0;
	}
	

	//default constructor
	public Token(int id, String token) {
		m_token = token;
		m_id = id;
		df_value = 0;	
		ttf_value = 0;
	}
}
