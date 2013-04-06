package engine.conveyorfamily.m;


import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;
import transducer.*;

public class ConveyorFamilyM implements ConveyorFamilyInterface
{
	private Integer name;
	private ConveyorAgent conveyor;
	private PopupAgent popup;
	private Transducer tranny;
	
	public ConveyorFamilyM(ConveyorFamilyInterface next, ConveyorFamilyInterface previous)
	{
		popup = new PopupAgent(next);
		conveyor = new ConveyorAgent(previous);
	}
	
	public ConveyorFamilyM(Integer n, Transducer t)
	{
		popup = new PopupAgent(n);
		conveyor = new ConveyorAgent(n);
		popup.setPrevious(conveyor);
		conveyor.setPopup(popup);
		
		tranny = t;
		
		popup.setTransducer(tranny);
		conveyor.setTransducer(tranny);
		
		popup.startThread();
		conveyor.startThread();
	}
	
	public void msgHereIsGlass(Glass g)
	{
		conveyor.msgHereIsGlass(g);
	}
	
	public void msgIAmReady()
	{
		popup.msgIAmReady();
	}
	
	public void setNextConveyor(ConveyorFamilyInterface cf)
	{
		popup.setNext(cf);
	}
	
	public void setPrevConveyor(ConveyorFamilyInterface cf)
	{
		conveyor.setPrevConveyor(cf);
	}
}