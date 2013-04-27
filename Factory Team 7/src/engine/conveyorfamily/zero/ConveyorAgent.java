package engine.conveyorfamily.zero;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
	private enum SendState {APPROVED, DEFAULT, WAITING, DENIED};
	private enum AnimState {WAITING,LOADING, WORKING, RELEASING, DEFAULT};

	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState nextState;
	private SendState prevState;
	private AnimState cutterState;
	boolean quieted = false;
	Boolean jammed = false;
	boolean initialJam = false;
	
	private Timer timer = new Timer();
	
	private int i =0;
	
	public ConveyorAgent(Integer n, Transducer t)
	{
		transducer = t;
		transducer.register(this, TChannel.CUTTER);
		transducer.register(this, TChannel.BIN);
		transducer.register(this, TChannel.SENSOR);
		transducer.register(this, TChannel.CONVEYOR);
		transducer.register(this, TChannel.CONTROL_PANEL);
		number = n;
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		nextState = SendState.DEFAULT;
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
		synchronized(nextState){nextState = SendState.APPROVED;}
		stateChanged();
	}
	
	
	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction()
	{
		if(!jammed)
		{
			if(cutterState == AnimState.WAITING )
			{
				if(sensorTwo == SensorState.ON)
				{
					synchronized(nextState){sendCutter();}
					return true;
				}
			}
			if(!glass.isEmpty())
			{
				synchronized(nextState){
					if(nextState==SendState.APPROVED && cutterState == AnimState.DEFAULT)
					{
						sendNext();
						return true;
					}}
			}
		
			if(prevState!=SendState.APPROVED && prevState!=SendState.WAITING && prevState!=SendState.DENIED)
			{
					if(sensorOne!=SensorState.ON && sensorTwo!=SensorState.ON)
					{
						askForGlass();
						return true;
					}
			}
			if(glass.isEmpty() && nextState == SendState.DEFAULT && cutterState == AnimState.DEFAULT && !quieted)
			{
				quietConveyor();
				return true;
			}
		}
		else if(!initialJam)
		{
			if(cutterState != AnimState.LOADING)
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
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
	}
	public void sendNext()
	{
		nextConveyor.msgHereIsGlass(glass.remove(0));
		cutterState = AnimState.WAITING;
		quieted = false;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		nextState = SendState.DEFAULT;
		stateChanged();
	}
	
	public void sendCutter()
	{
		quieted = false;
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
				synchronized(nextState)
				{
					cutterState = AnimState.DEFAULT;
					transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
					stateChanged();
				}
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
					if(cutterState == AnimState.WAITING)
					{
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
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
			if(event == TEvent.BIN_CANNOT_CREATE)
			{
				incomingGlass = null;
				prevState = SendState.DENIED;
				timer.schedule(new TimerTask()
				{
					public void run()
					{
						prevState = SendState.DEFAULT;
						stateChanged();
					}
				},1000);
				stateChanged();
			}
		}
		if(channel == TChannel.CONTROL_PANEL)
		{
			if((Integer)args[0] == 0)
			{
				if(event == TEvent.CONVEYOR_JAM)
				{
					synchronized(jammed)
					{
						jammed = true;
						stateChanged();
					}
				}
				if(event == TEvent.CONVEYOR_UNJAM)
				{
					synchronized(jammed)
					{
						initialJam = false;
						jammed = false;
						if(cutterState == AnimState.WAITING)
						{
							transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
						}
						stateChanged();
					}
				}
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