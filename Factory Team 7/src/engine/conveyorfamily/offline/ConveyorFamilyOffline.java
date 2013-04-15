package engine.conveyorfamily.offline;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyOffline implements ConveyorFamilyInterface
{
	private Integer name;
	private ConveyorAgent conveyor;
	private PopupAgent popup;
	private Transducer transducer;
	
	public ConveyorFamilyOffline(ConveyorFamilyInterface previous, Transducer t, Integer n)
	{
		name = n;
		transducer = t;
		popup = new PopupAgent(transducer, name);
		conveyor = new ConveyorAgent(transducer, name);
		popup.setPrevious(conveyor);
		conveyor.setNext(popup);
		conveyor.setPrev(previous);
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
	
	public void setNextConveyor(ConveyorFamilyInterface cF)
	{
		popup.setNextConveyor(cF);
	}
	
	public void startThread()
	{
		conveyor.startThread();
		popup.startThread();
	}
}