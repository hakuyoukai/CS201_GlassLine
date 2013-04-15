package engine.conveyorfamily.offline;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorAgent extends Agent implements TReceiver
{
	private Integer number;
	private ConveyorFamilyInterface prevConveyor;
	private PopupAgent popup;
	private Transducer transducer;
	private ArrayList<Glass> glass;
	private Glass incomingGlass;
	private Integer[] conArgs = new Integer[1];
	private Integer capacity = 3;
	
	private enum SensorState {ON,OFF};
	private enum SendState {APPROVED, DEFAULT, WAITING, ASKED, LOADING};
	
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState nextState;
	private SendState prevState;
	
	
	public ConveyorAgent(Transducer t, Integer name) 
	{
		transducer = t;
		transducer.register(this, TChannel.CONVEYOR);
		transducer.register(this, TChannel.SENSOR);
		number = name;
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		nextState = SendState.DEFAULT;
		prevState = SendState.DEFAULT;
		
		glass = new ArrayList<Glass>();
		conArgs[0] = number;
	}

	//!!MESSAGES!!

	public void msgGiveMeGlass() 
	{
		System.err.println("POPUP NEED GLASS " + number);
		nextState = SendState.APPROVED;
		stateChanged();
	}
	
	public void msgHereIsGlass(Glass g) 
	{
		incomingGlass = g;
		prevState = SendState.WAITING;
		stateChanged();
	}
	
	@Override
	public boolean pickAndExecuteAnAction()
	{
		System.err.println(number);
		System.err.println(nextState);
		System.err.println(prevState);
		if(nextState == SendState.WAITING)
		{
			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
			if(sensorTwo == SensorState.ON)
			{
				sendPopup();
				return true;
			}
		}
		if(!glass.isEmpty())
		{
			if(nextState == SendState.APPROVED)
			{
				sendNext();
				return true;
			}
			if(nextState!=SendState.ASKED)
			{
				askNext();
				return true;
			}
		}
		if(prevState == SendState.DEFAULT)
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
	public void askNext()
	{
		popup.msgCanIGiveGlass(glass.get(0).recipe.get(number));
		nextState = SendState.ASKED;
		stateChanged();
	}
	
	public void sendNext()
	{
		popup.msgHereIsGlass(this, glass.remove(0));
		nextState = SendState.WAITING;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
		stateChanged();
	}
	
	public void sendPopup()
	{
		nextState = SendState.LOADING;
		System.err.println("SENDING TO POPUP " + number);
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
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
		if(channel == TChannel.SENSOR)
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				if((Integer)args[0] == (2*number))
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
				if((Integer)args[0] == ((2*number)+1))
				{
					sensorTwo = SensorState.ON;
					if(nextState == SendState.LOADING)
					{
						nextState = SendState.DEFAULT;
					}
					transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP, conArgs);
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


	public void setNext(PopupAgent p)
	{
		popup = p;
	}

	public String getName()
	{
		return "CONVEYOR " + number;
	}
	
	public void setPrev(ConveyorFamilyInterface cF)
	{
		prevConveyor = cF;
	}

	
}