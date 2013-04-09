package engine.conveyorfamily.zero;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

import java.util.ArrayList;

public class ConveyorAgent extends Agent implements TReceiver
{

	private Integer number;
	private ConveyorFamilyInterface nextConveyor;
	private Transducer transducer;
	private ArrayList<Glass> glass;
	private Glass incomingGlass;
	private Glass cutterGlass;
	private Integer capacity = 3;
	private Integer[] newArgs;
	
	private enum SensorState {ON,OFF};
	private enum SendState {APPROVED, DEFAULT, WAITING};
	private enum AnimState {WAITING,LOADING, WORKING, RELEASING, DEFAULT};
	
	
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState nextState;
	private SendState prevState;
	private AnimState cutterState;
	
	public ConveyorAgent(Integer n, Transducer t)
	{
		transducer = t;
		transducer.register(this, TChannel.CUTTER);
		transducer.register(this, TChannel.BIN);
		transducer.register(this, TChannel.SENSOR);
		number = n;
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		//TODO Change the following line if you are doing testing
		nextState = SendState.DEFAULT;
		nextState = SendState.APPROVED;
		prevState = SendState.DEFAULT;
		cutterState = AnimState.DEFAULT;
		glass = new ArrayList<Glass>();
		newArgs = new Integer[1];
		newArgs[0] = 0;
	}
	
	//!!MESSAGES!!
	public void msgHereIsGlass(Glass g)
	{
		incomingGlass = g;
		prevState = SendState.WAITING;
		stateChanged();
	}
	
	public void msgIAmReady()
	{
		nextState = SendState.APPROVED;
		stateChanged();
	}
	
	
	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction()
	{
		if(cutterState == AnimState.WAITING )
		{
			if(sensorTwo == SensorState.ON)
			{
				sendCutter();
				return true;
			}
		}
		if(!glass.isEmpty())
		{
			if(nextState==SendState.APPROVED)
			{
				sendNext();
				return true;
			}
		}
		
		if(prevState!=SendState.APPROVED && prevState!=SendState.WAITING)
		{
			if(glass.size()<capacity)
			{
				if(sensorOne!=SensorState.ON && sensorTwo!=SensorState.ON)
				{
					askForGlass();
					return true;
				}
			}
		}
		return false;
	}
	
	//!!ACTIONS!!
	public void sendNext()
	{
		//TODO REMOVE IF YOU ARE TESTING WITH YOUR CONVEYOR
		nextConveyor.msgHereIsGlass(glass.remove(0));
		cutterState = AnimState.WAITING;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		nextState = SendState.DEFAULT;
		stateChanged();
	}
	
	public void sendCutter()
	{
		transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START, newArgs);
		cutterState = AnimState.LOADING;
		stateChanged();
	}
	
	public void askForGlass()
	{
		//Temporary staging area until proper bin creation is done
		transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		prevState = SendState.APPROVED;
		stateChanged();
	}
	
	public void startConveyor(Integer num)
	{
		if(num == 1)
		{
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
			//Set conditions for conveyor only moving until sensor one is off
		}
		stateChanged();
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == TChannel.CUTTER)
		{
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				cutterState = AnimState.WORKING;
				transducer.fireEvent(TChannel.CUTTER,TEvent.WORKSTATION_DO_ACTION,null);
				stateChanged();
			}
			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				cutterState = AnimState.DEFAULT;
				//TODO If testing, change the below line to...
				nextState = SendState.DEFAULT;
				//nextState = SendState.APPROVED;
				transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
				stateChanged();
			}
			//For some reason release finished is continually sent by the GUI, with no regard for common sense, so it has been omitted here
		}
		if(channel == TChannel.SENSOR)
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				if((Integer)args[0] == 0)
				{
					sensorOne = SensorState.ON;
					if(prevState == SendState.WAITING)
					{
						glass.add(incomingGlass);
						incomingGlass = null;
						prevState = SendState.DEFAULT;
						stateChanged();

					}
				}
				if((Integer)args[0] == 1)
				{
					sensorTwo = SensorState.ON;
					/*
					if(cutterState == AnimState.WAITING)
					{
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
						stateChanged();

					}
					*/
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
						stateChanged();
				}
			}
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				if((Integer)args[0] == 0)
				{
					sensorOne = SensorState.OFF;
					stateChanged();

				}
				if((Integer)args[0] == 1)
				{
					sensorTwo = SensorState.OFF;
					stateChanged();

				}
			}
		}
		if(channel == TChannel.BIN)
		{
			if(event == TEvent.BIN_PART_CREATED)
			{
				incomingGlass = (Glass)args[0];
				prevState = SendState.WAITING;
				stateChanged();

			}
		}
	}

	public void setNextConveyor(ConveyorFamilyInterface cF)
	{
		nextConveyor = cF;
		stateChanged();
	}
	
	public String getName()
	{
		return "Conveyor Zero";
	}

}