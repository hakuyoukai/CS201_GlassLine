package engine.conveyorfamily.offline;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.Agent;
import engine.util.*;

public class ConveyorAgent extends Agent implements TReceiver
{
	private Transducer transducer;
	private Integer number;
	private PopupAgent next;
	private ConveyorFamilyInterface prev;
	private Glass incomingGlass;
	private ArrayList<Glass> glass;
	private Integer capacity = 3;
	private Integer[] conArgs;
	
	private enum SendState {DEFAULT, APPROVED, WAITING, ASKED}
	private enum SensorState {ON,OFF}
	
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState nextState;
	private SendState prevState;
	
	
	public ConveyorAgent(Transducer t, Integer num)
	{
		transducer = t;
		transducer.register(this, TChannel.SENSOR);
		number = num;
		
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		nextState = SendState.APPROVED;
		prevState = SendState.DEFAULT;
		
		glass = new ArrayList<Glass>();
		conArgs = new Integer[1];
		conArgs[0] = number;
	}

	//!!MESSAGES!!
	public void msgHereIsGlass(Glass g)
	{
		incomingGlass = g;
		prevState = SendState.WAITING;
		stateChanged();
	}
	
	public void msgGiveMeGlass()
	{
		nextState = SendState.APPROVED;
		stateChanged();
	}

	@Override
	public boolean pickAndExecuteAnAction()
	{
		if(!glass.isEmpty())
		{
			if(nextState == SendState.APPROVED)
			{
				sendNext();
				return true;
			}
			if(nextState == SendState.DEFAULT)
			{
				askNext();
				return true;
			}
		}
		if(prevState == SendState.DEFAULT)
		{
			if(glass.size()<capacity)
			{
				if(sensorOne == SensorState.OFF)
				{
					askForGlass();
					return true;
				}
			}
		}
		return false;
	}
	
	public void sendNext()
	{
		next.msgHereIsGlass(this,glass.remove(0));
		if(sensorTwo == SensorState.ON)
			nextState = SendState.DEFAULT;
		else
			nextState = SendState.WAITING;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
		stateChanged();
	}
	
	public void askNext()
	{
		nextState = SendState.ASKED;
		next.msgCanIGive();
		stateChanged();
	}
	
	public void askForGlass()
	{
		prevState = SendState.APPROVED;
		prev.msgIAmReady();
		stateChanged();
	}

	//!!SCHEDULER!!
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == TChannel.SENSOR)
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				if((Integer)args[0] == (2*number))
				{
					sensorOne = SensorState.ON;
					if(prevState == SendState.WAITING)
					{
						System.err.println("ADDIN DAT GLASS");
						glass.add(incomingGlass);
						incomingGlass = null;
						prevState = SendState.DEFAULT;
					}
					stateChanged();
				}
				if((Integer)args[0] == ((2*number)+1))
				{
					System.err.println("REACHED SECOND SENSOR");
					System.err.println(nextState);
					sensorTwo = SensorState.ON;
					if(nextState == SendState.WAITING)
					{
						nextState = SendState.DEFAULT;
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
					}
					else
					{
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conArgs);
					}
					stateChanged();
				
				}
			}
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				if((Integer)args[0] == (2*number))
				{
					sensorOne = SensorState.OFF;
					stateChanged();
				}
				if((Integer)args[0] == ((2*number)+1))
				{
					sensorTwo = SensorState.OFF;
					stateChanged();
				}
			}
		}
	}
	
	//!!UTILITIES!!
	public void setNext(PopupAgent p)
	{
		next = p;
	}
	
	public void setPrevious(ConveyorFamilyInterface cF)
	{
		prev = cF;
	}
	
	public String getName()
	{
		return "Conveyor " + number;
	}
	
}