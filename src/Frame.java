import java.util.ArrayList;


public class Frame {
	
	private ArrayList<Slot> container;
	
	public Frame()
	{
		container = new ArrayList<>();
	}
	
	public void add(Slot slot) 
	{
		container.add(slot);
	}
	
	public ArrayList<Slot> getContent()
	{
		return container;
	}
	
	public static void printFrame(Frame frame)
	{
		ArrayList<Slot> container = frame.getContent();

		System.out.println("{");
		
		for(Slot slot: container)
		{
			System.out.println("\t" + slot.getKey() + ": " + slot.getValue());
		}
		
		System.out.println("}");

	}

}
