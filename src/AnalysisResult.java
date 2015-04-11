import java.util.ArrayList;


public class AnalysisResult {
	
	private ArrayList<String[]> mapping;
	private String[] tokens;
	
	public AnalysisResult(String[] tokens,
			ArrayList<String[]> mapping) {
		
		this.tokens = tokens;
		this.mapping = mapping;
	}

	public ArrayList<String[]> getMapping() 
	{
		return mapping;
	}

	public String[] getTokens() 
	{
		return tokens;
	}

}
