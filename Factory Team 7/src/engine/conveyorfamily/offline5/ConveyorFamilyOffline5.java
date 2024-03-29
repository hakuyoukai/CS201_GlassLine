package engine.conveyorfamily.offline5;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyOffline5 implements ConveyorFamilyInterface
{

	private Integer number;
	private ConveyorAgent conveyor;
	private PopupAgent popup;
	private Transducer transducer;
	
	public ConveyorFamilyOffline5(Transducer t, ConveyorFamilyInterface prev, Integer num)
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
	
	public void disableBot(){
		popup.msgOnlyTopWorkstation();
	}
	
	public void disableTop(){
		popup.msgOnlyBotWorkstation();
	}
	
	public void disableBoth(){
		popup.msgNoWorkstations();
	}
	
	public void jam(){
		conveyor.msgJamIt();
	}
	
	public void turnOff(){
		conveyor.msgTurnOff();
	}
	
	public void turnOn(){
		conveyor.msgTurnOn();
	}
	
	public void breakGlassTop(){
		popup.msgBreakGlass(0);
	}
	
	public void breakGlassBot(){
		popup.msgBreakGlass(1);
	}
	
	public void unBreakGlassTop(){
		popup.msgUnBreakGlass(0);
	}
	
	public void unBreakGlassBot(){
		popup.msgUnBreakGlass(1);
	}
	
	public void noProcessBot(){
		popup.msgNoProcessGlass(0);
	}
	
	public void noProcessTop(){
		popup.msgNoProcessGlass(1);
	}
	
	public void reProcessBot(){
		popup.msgReProcessGlass(0);
	}
	
	public void reProcessTop(){
		popup.msgReProcessGlass(1);
	}
		
	public void starThread()
	{
		popup.startThread();
		conveyor.startThread();
	}

	@Override
	public void msgDeleteGlass(Glass g)
	{
		// TODO Auto-generated method stub
		
	}
}