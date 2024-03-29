package engine.conveyorfamily.last;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.*;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorAgent extends Agent implements TReceiver
{

	private Integer number;
	private ConveyorFamilyInterface prevConveyor;
	private Transducer transducer;
	private ArrayList<Glass> glass;
	private Glass incomingGlass;
	private Integer capacity = 3;
	private Integer[] newArgs;
	private boolean quieted = false;
	private Boolean jammed = false;
	private boolean initialJam = false;
	private boolean truckBroken = false;
	
	private enum SensorState {ON,OFF};
	private enum SendState {APPROVED,DEFAULT,WAITING,DENIED};
	private enum AnimState {WAITING, LOADING, WORKING, RELEASING, DEFAULT, BROKEN};
	
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState prevState;
	private SendState nextState;
	private AnimState truckState;
	
	private int i=0;
	
	public ConveyorAgent(Integer n, Transducer t)
	{
		transducer = t;
		transducer.register(this, TChannel.TRUCK);
		transducer.register(this, TChannel.SENSOR);
		transducer.register(this, TChannel.CONTROL_PANEL);
		number = n;
		
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		nextState = SendState.APPROVED;
		prevState = SendState.DEFAULT;
		truckState = AnimState.DEFAULT;
		
		glass = new ArrayList<Glass>();
		newArgs = new Integer[1];
		newArgs[0]=14;
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
	
	public void msgDeleteGlass(Glass g)
	{
		incomingGlass = null;
		prevState = SendState.APPROVED;
		stateChanged();
	}
	
	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction() 
	{
		if(!jammed)
		{
			if(truckState == AnimState.WAITING)
			{
					if(sensorTwo == SensorState.ON)
					{
						sendTruck();
						return true;
					}
			}
			if(!glass.isEmpty())
			{
				if(nextState==SendState.APPROVED && truckState==AnimState.DEFAULT)
				{
					sendNext();
					return true;
				}
			}
			if(prevState!=SendState.APPROVED && prevState!=SendState.WAITING && prevState!=SendState.DENIED)
			{
				if(!glass.isEmpty())
				{
					if(sensorOne!=SensorState.ON && sensorTwo == SensorState.ON)
					{
						askForGlass();
						return true;
					}
				}
				else
				{
					askForGlass();
					return true;
				}
			}
			if(glass.isEmpty() && truckState == AnimState.DEFAULT && prevState == SendState.APPROVED && !quieted)
			{
				quietConveyor();
				return true;
			}
		}
		else if(!initialJam)
		{
			if(truckState != AnimState.LOADING)
			{
				initialJam = true;
				transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
				return true;
			}
		}
		return false;
	}
	
	//!!ACTIONS!!
	public void quietConveyor()
	{
		quieted = true;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		stateChanged();
	}
	public void sendTruck()
	{
		quieted = false;
		transducer.fireEvent(TChannel.TRUCK, TEvent.CONVEYOR_DO_START, newArgs);
		truckState = AnimState.LOADING;
		stateChanged();
	}
	
	public void sendNext()
	{
		glass.remove(0);
		truckState = AnimState.WAITING;
		quieted = false;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		nextState = SendState.DEFAULT;
		stateChanged();
	}
	
	public void askForGlass()
	{
		prevConveyor.msgIAmReady();
		prevState = SendState.APPROVED;
		stateChanged();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == TChannel.TRUCK)
		{
			if(event == TEvent.TRUCK_GUI_LOAD_FINISHED)
			{
				truckState = AnimState.WORKING;
				transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
				stateChanged();
			}
			if(event == TEvent.TRUCK_GUI_EMPTY_FINISHED)
			{
				truckState = AnimState.DEFAULT;
				nextState = SendState.APPROVED;
				stateChanged();
			}
		}
		if(channel == TChannel.SENSOR)
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				if((Integer)args[0] == 28)
				{
					sensorOne = SensorState.ON;
					if(prevState == SendState.WAITING)
					{
						glass.add(incomingGlass);
						incomingGlass = null;
						prevState = SendState.DEFAULT;
					}
					stateChanged();
				}
				if((Integer)args[0] == 29)
				{
					sensorTwo = SensorState.ON;
					if(truckState == AnimState.WAITING)
					{
						quieted = false;
						transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,newArgs);
					}
					else
					{
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
					}
					stateChanged();
				}
			}
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				if((Integer)args[0] == 28)
				{
					sensorOne = SensorState.OFF;
					stateChanged();
				}
				if((Integer)args[0] == 29)
				{
					sensorTwo = SensorState.OFF;
					stateChanged();
				}
			}
		}
		if(channel == TChannel.CONTROL_PANEL)
		{
			if(event == TEvent.CONVEYOR_JAM)
			{
				if((Integer)args[0] == 14)
				{
					synchronized(jammed)
					{
						jammed = true;
						stateChanged();
					}
				}
			}
			if(event == TEvent.CONVEYOR_UNJAM)
			{
				if((Integer)args[0] == 14)
				{
					synchronized(jammed)
					{
						initialJam = false;
						jammed = false;
						if(truckState == AnimState.WAITING)
						{
							transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
						}
						stateChanged();
					}
				}
			}
			if(event == TEvent.TRUCK_BREAK)
			{
				truckState = AnimState.BROKEN;
				stateChanged();
			}
			if(event == TEvent.TRUCK_REPAIR)
			{
				truckState = AnimState.BROKEN;
				stateChanged();
			}
		}
	}
	
	public void setPrevConveyor(ConveyorFamilyInterface cF)
	{
		prevConveyor = cF;
		stateChanged();
	}
	
	public String getName()
	{
		return "Conveyor Last";
	}
	
}