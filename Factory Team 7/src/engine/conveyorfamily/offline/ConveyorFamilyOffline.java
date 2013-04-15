package engine.conveyorfamily.offline;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyOffline implements ConveyorFamilyInterface
{

	private Integer number;
	private ConveyorAgent conveyor;
	private PopupAgent popup;
	private Transducer transducer;
	
	public ConveyorFamilyOffline(Transducer t, ConveyorFamilyInterface prev, Integer num)
	{
		number = num;
		conveyor = new ConveyorAgent(t,number);
		popup = new PopupAgent(t,number);
		conveyor.setNext(popup);
		conveyor.setPrevious(prev);
		popup.setPrevious(conveyor);
	}
	
	@Override
	public void msgHereIsGlass(Glass g) 
	{
		conveyor.msgHereIsGlass(g);
	}

	@Override
	public void msgIAmReady() 
	{
		popup.msgIAmReady();
	}
	
	public void setNext(ConveyorFamilyInterface cF)
	{
		popup.setNext(cF);
	}
		
	public void starThread()
	{
		popup.startThread();
		conveyor.startThread();
	}
}