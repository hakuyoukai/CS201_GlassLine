package engine.conveyorfamily.last;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyLast implements ConveyorFamilyInterface
{
	private Integer number = 14;
	private ConveyorAgent conveyor;
	
	public ConveyorFamilyLast(Transducer t, ConveyorFamilyInterface cF)
	{
		conveyor = new ConveyorAgent(number,t);
		conveyor.setPrevConveyor(cF);
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
		//Not necessary
	}

	@Override
	public void msgDeleteGlass(Glass g)
	{
		// TODO Auto-generated method stub
		
	}
}