package engine.conveyorfamily.online;

import engine.agent.Agent;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class SensorAfterAgent extends Agent
{
	ConveyorAgent conveyor;
	public enum SensorAfterState{RELEASED,PRESSED,JUST_RELEASED,JUST_PRESSED};
	public SensorAfterState state=SensorAfterState.RELEASED;
	int sensorAfterIndex;
	
	public SensorAfterAgent(ConveyorAgent conveyor,Transducer t,int sensorAfterIndex)
	{
		super("Sensor: " + sensorAfterIndex);
		this.conveyor=conveyor;
		this.transducer=t;
		this.transducer.register(this, TChannel.SENSOR);
		this.sensorAfterIndex=sensorAfterIndex;
	}

	//called when the sensor is released
	public void sensorAfterReleased()
	{
		this.conveyor.msgSensorAfterReleased();
		stateChanged();
	}

	//called when the glass moved onto this sensor
	public void tellConveyorGlassArrived()
	{
		this.conveyor.msgGlassArrived();
		stateChanged();
	}

	/* (non-Javadoc)
	 * @see engine.agent.SensorAfter#eventFired(transducer.TChannel, transducer.TEvent, java.lang.Object[])
	 */
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		
		if(args!=null)
		{
			Object[] arguments=args;
			if(arguments[0]!=null&&arguments[0] instanceof Integer)
			{
				if(channel==TChannel.SENSOR&&(Integer)arguments[0]==sensorAfterIndex)
				{
					if(event==TEvent.SENSOR_GUI_RELEASED)
					{
						state=SensorAfterState.JUST_RELEASED;
						stateChanged();
					}
					else if(event==TEvent.SENSOR_GUI_PRESSED)
					{
						state=SensorAfterState.JUST_PRESSED;
						stateChanged();
					}
				}
			}
		}
	}
	
	public boolean pickAndExecuteAnAction()
	{
		if(state==SensorAfterState.JUST_RELEASED)
		{
			state=SensorAfterState.RELEASED;
			sensorAfterReleased();
			return true;
		}
		else if(state==SensorAfterState.JUST_PRESSED)
		{
			state=SensorAfterState.PRESSED;
			tellConveyorGlassArrived();
			return true;
		}
		return false;
	}
}
