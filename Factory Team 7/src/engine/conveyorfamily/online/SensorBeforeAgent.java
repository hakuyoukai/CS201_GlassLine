package engine.conveyorfamily.online;

import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class SensorBeforeAgent extends Agent
{
	ConveyorFamilyInterface conveyorBefore;
	public enum SensorBeforeState{RELEASED,PRESSED,JUST_RELEASED,JUST_PRESSED};
	public SensorBeforeState state=SensorBeforeState.RELEASED;
	int sensorBeforeIndex;
	
	public SensorBeforeAgent(ConveyorFamilyInterface conveyorBefore,Transducer t,int sensorBeforeIndex)
	{
		super("Sensor: " + sensorBeforeIndex);
		this.transducer=t;
		this.transducer.register(this, TChannel.SENSOR);
		this.conveyorBefore=conveyorBefore;
		this.sensorBeforeIndex=sensorBeforeIndex;
	}
	
	public void setPreviousCF(ConveyorFamilyInterface conveyorBefore)
	{
		this.conveyorBefore=conveyorBefore;
	}
	
	//when this sensor is released
	public void tellPreviousCFReady()
	{
		if(sensorBeforeIndex==24)
		{
			System.out.println("Conveyor 12 told conveyor 11 it is ready");
		}
		this.conveyorBefore.msgIAmReady();
		stateChanged();
	}
	
	/* (non-Javadoc)
	 * @see engine.agent.SensorBefore#eventFired(transducer.TChannel, transducer.TEvent, java.lang.Object[])
	 */
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(args!=null)
		{
			Object[] arguments=args;
			if(arguments[0]!=null&&arguments[0] instanceof Integer)
			{
				if(channel==TChannel.SENSOR&&(Integer)arguments[0]==sensorBeforeIndex)
				{
					if(event==TEvent.SENSOR_GUI_RELEASED)
					{
						state=SensorBeforeState.JUST_RELEASED;
						stateChanged();
					}
				}
			}
		}
	}
	
	public boolean pickAndExecuteAnAction()
	{
		if(state==SensorBeforeState.JUST_RELEASED)
		{
			state=SensorBeforeState.RELEASED;
			tellPreviousCFReady();
			return true;
		}
		return false;
	}
}
