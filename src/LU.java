import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;


public class LU {

	/*
	 * Kelas ini bertugas untuk menlakukan konversi
	 * dari kalimat yang didapat dari ASR ke bentuk
	 * frame
	 */
	
	private ArrayList<String> places;
	private ArrayList<String> time_modifiers;
	private ArrayList<String> ada_identifiers;
	private ArrayList<String> list_jadwal_identifiers;
	private ArrayList<String> place_identifiers;
	private ArrayList<String> place_identifiers_relations;
	
	private final String TIME_MODIFIER = "time_modifier";
	private final String PLACE_IDENTIFIER = "place_identifier";
	private final String LIST_JADWAL_IDENTIFIER = "list_jadwal_identifier";
	private final String ADA_IDENTIFIER = "ada_identifier";
	
	private ArrayList<String> keywords;
	
	public LU()
	{
		loadFiles();
		constructKnownKeywords();
	}
	
	public Frame getFrame(String sentence)
	{
		AnalysisResult analysis = getAnalysisResult(sentence);
		return constructFrameFromAnalysisResult(analysis);
	}
	
	private Frame constructFrameFromAnalysisResult(AnalysisResult analysis) {
		
		ArrayList<String[]> mapping = analysis.getMapping();
		ArrayList<Integer> consecutive_mapping = analysis.getConsecutiveMapping();
		String[] tokens = analysis.getTokens();
		Frame ret = new Frame();
		
		// construct from mapping
		for(String[] row: mapping)
		{
			String keyword_category = getCategoryOfKeyword(row[0]);
			
			if(keyword_category.equals(PLACE_IDENTIFIER))
			{
				int index = Integer.valueOf(row[1]);
				String place = getPlace(tokens, index);
				Slot slot = new Slot(row[0], place);
				ret.add(slot);
				
				if(index - 1 >= 0)
				{
					if(isWordPlace(tokens[index - 1]))
					{
						Slot guessed_slot = new Slot(getProperKeywordPlaceIdentifierForKeyword(row[0]), tokens[index-1]);
						if(!isSlotAlreadyExistOnFrame(ret, guessed_slot))
						{
							ret.add(guessed_slot);
						}
					}
				}
			}
			else if(keyword_category.equals(TIME_MODIFIER))
			{
				Slot slot = new Slot(TIME_MODIFIER, row[0]);
				ret.add(slot);
			}
			else if(keyword_category.equals(LIST_JADWAL_IDENTIFIER))
			{
				Slot slot = new Slot(LIST_JADWAL_IDENTIFIER, row[0]);
				ret.add(slot);
			}
			else if(keyword_category.equals(ADA_IDENTIFIER))
			{
				Slot slot = new Slot(ADA_IDENTIFIER, row[0]);
				ret.add(slot);
			}
		}
		
		//construct from consecutive occurence
		if(consecutive_mapping.size() > 0)
		{
			for(Integer start_index: consecutive_mapping)
			{
				Slot slot_dari = new Slot("dari", tokens[start_index]);
				Slot slot_ke = new Slot("ke", tokens[start_index + 1]);
				ret.add(slot_dari);
				ret.add(slot_ke);
			}
		}
		
		return ret;
	}
	
	private boolean isSlotAlreadyExistOnFrame(Frame ret, Slot guessed_slot) {
		
		boolean found = false;
		
		for(Slot row: ret.getContent())
		{
			if(row.getValue().equals(guessed_slot.getValue()))
			{
				found = true;
				break;
			}
		}
		
		return found;
	}

	private String getProperKeywordPlaceIdentifierForKeyword(String keyword)
	{
		String ret = "";
		
		for(String row: place_identifiers_relations)
		{
			String[] arr = row.split("\\s");
			if(arr[0].equals(keyword))
			{
				ret = arr[1];
				break;
			}
		}
		
		return ret;
	}
	
	private String getPlace(String[] tokens, int start_index) {
		
		/*
		 * fungsi ini akan ngambil value place
		 * dari yang terdekat
		 */
		
		int i = start_index + 1;
		boolean found = false;
		String ret = "";
		
		while(!found && i < tokens.length)
		{
			if(isWordPlace(tokens[i]))
			{
				found = true;
			}
			else
			{
				i++;
			}
		}
		
		if(found)
		{
			ret = tokens[i];
		}
		
		return ret;
	}
	
	private boolean isWordPlace(String word)
	{
		return places.contains(word);
	}

	private String getCategoryOfKeyword(String keyword) {
		
		String ret = "";
		
		if(place_identifiers.contains(keyword))
		{
			ret = PLACE_IDENTIFIER;
		}
		else if(time_modifiers.contains(keyword))
		{
			ret = TIME_MODIFIER;
		}
		else if(list_jadwal_identifiers.contains(keyword))
		{
			ret = LIST_JADWAL_IDENTIFIER;
		}
		else if(ada_identifiers.contains(keyword))
		{
			ret = ADA_IDENTIFIER;
		}
		
		return ret;
	}

	private AnalysisResult getAnalysisResult(String sentence) {
		
		String[] tokens = sentence.split("\\s");
		
		ArrayList<String[]> mapping = new ArrayList<>(); //contains mapping for keyword and its index in token
		ArrayList<Integer> consecutive_mapping = new ArrayList<>(); //contains starting index of consecutive occurence
		
		for(int i = 0; i < tokens.length; i++)
		{
			// mapping keywords
			if(keywords.contains(tokens[i]))
			{
				String[] map = new String[2];
				map[0] = tokens[i];
				map[1] = String.valueOf(i);
				mapping.add(map);
			}
			
			//mapping consecutive places occuring
			if(i + 1 < tokens.length)
			{
				if(isWordPlace(tokens[i]) && isWordPlace(tokens[i+1]))
				{
					consecutive_mapping.add(i);
				}
			}
		}
		
		AnalysisResult ret = new AnalysisResult(tokens, mapping, consecutive_mapping);
		return ret;
	}
	
	private void constructKnownKeywords()
	{
		keywords = new ArrayList<>();
		keywords.addAll(place_identifiers);
		keywords.addAll(ada_identifiers);
		keywords.addAll(list_jadwal_identifiers);
		keywords.addAll(time_modifiers);
	}
	
	private void loadFiles()
	{
		places = loadTextFile("places");
		time_modifiers = loadTextFile("time_modifiers");
		ada_identifiers = loadTextFile("ada_identifiers");
		list_jadwal_identifiers = loadTextFile("list_jadwal_identifiers");
		place_identifiers = loadTextFile("place_identifiers");
		place_identifiers_relations = loadTextFile("place_identifiers_relations");
	}
	
	private ArrayList<String> loadTextFile(String filename)
	{
		ArrayList<String> ret = new ArrayList<>();
		
		URL file = getClass().getResource(filename);
        BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(file.openStream()));
			
			String inputLine;
	        while ((inputLine = in.readLine()) != null)
	        {
	        	if(inputLine.charAt(0) != '#')
	        	{
	        		ret.add(inputLine);
	        	}
	        }
	            
	        in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
	
	public static void printArrayList(ArrayList<String> ar)
	{
		for(String row: ar)
		{
			System.out.println(row);
		}
	}
	
	public void testDrive(String filename)
	{
		ArrayList<String> tests = loadTextFile(filename);
		
		for(String test: tests)
		{
			System.out.println("Sentence: " + test);
			Frame.printFrame(getFrame(test));
			System.out.println();
		}
	}
	
	public static void main(String args[])
	{
		LU lu = new LU();
		lu.testDrive("default_pertanyaan.txt");
	}
	
}
