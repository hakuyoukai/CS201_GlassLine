package engine.conveyorfamily.zero;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyZero implements ConveyorFamilyInterface
{
	private Integer number = 0;
	private ConveyorAgent conveyor;
	private Transducer transducer;
	
	public ConveyorFamilyZero(Transducer t)
	{
		transducer = t;
		conveyor = new ConveyorAgent(number, transducer);
		conveyor.startThread();
	}
	@Override
	public void msgHereIsGlass(Glass g)
	{
		conveyor.msgHereIsGlass(g);
	}

	@Override
	public void msgIAmReady() 
	{
		conveyor.msgIAmReady();
	}
	
	public void setNextConveyor(ConveyorFamilyInterface cF)
	{
		conveyor.setNextConveyor(cF);
	}
	@Override
	public void msgDeleteGlass(Glass g)
	{
		// TODO Auto-generated method stub
		
	}
	
}