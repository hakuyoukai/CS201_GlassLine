package engine.conveyorfamily.online;

import engine.util.ConveyorFamilyInterface;
import transducer.TChannel;
import transducer.Transducer;

public class ConveyorFamily implements ConveyorFamilyInterface
{	
	SensorBeforeAgent sensorBefore;
	SensorAfterAgent sensorAfter;
	ConveyorAgent conveyor;
	ConveyorFamilyInterface conveyorBefore;
	ConveyorFamilyInterface conveyorAfter;
	public int familyIndex;

	
	public ConveyorFamily(ConveyorFamilyInterface conveyorBefore, ConveyorFamilyInterface conveyorAfter,Transducer t, int familyIndex, TChannel machineType)
	{
		this.conveyorBefore=conveyorBefore;
		this.familyIndex=familyIndex;
		sensorBefore=new SensorBeforeAgent(conveyorBefore,t,2*familyIndex);
		conveyor=new ConveyorAgent(conveyorAfter,t,familyIndex,machineType);
		sensorAfter=new SensorAfterAgent(conveyor,t,2*familyIndex+1);
		startAllAgentThreads();
	}
	
	public void setNextCF(ConveyorFamilyInterface conveyorAfter)
	{
		this.conveyorAfter=conveyorAfter;
		conveyor.setNextCF(this.conveyorAfter);
	}
	
	public void setPreviousCF(ConveyorFamilyInterface conveyorBefore)
	{
		this.conveyorBefore=conveyorBefore;
		sensorBefore.setPreviousCF(this.conveyorBefore);
	}


	
	private void startAllAgentThreads()
	{
		conveyor.startThread();
		sensorAfter.startThread();
		sensorBefore.startThread();
	}
	
	/* (non-Javadoc)
	 * @see engine.agent.IConveyorFamily#msgIAmReady()
	 */
	public void msgIAmReady()
	{
		conveyor.msgNextCFRready();
	}

	@Override
	public void msgHereIsGlass(engine.util.Glass g)
	{
		conveyor.msgPreviousCFGaveGlass(g);
		System.out.println(familyIndex+ " is given a glass");
	}
}
