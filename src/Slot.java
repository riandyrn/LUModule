
public class Slot {

	String[] container;
	
	public Slot(String key, String value) {
		container = new String[2];
		container[0] = key;
		container[1] = value;
	}
	
	public String getKey()
	{
		return container[0];
	}
	
	public String getValue()
	{
		return container[1];
	}

}
