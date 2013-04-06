package engine.conveyorfamily.m;


import java.util.List;
import java.util.ArrayList;

import transducer.*;

import engine.agent.*;
import engine.util.Glass;
import engine.util.ConveyorFamilyInterface;


public class ConveyorAgent extends Agent implements Conveyor, TReceiver
{
	private Integer name;
	private ConveyorFamilyInterface previousConveyor;
	private List<Glass> glass;
	private Popup popup;
	private enum PopupState {ASKED, DEFAULT, APPROVED};
	private enum ConveyorState {ASKED, DEFAULT};
	private enum AnimState {BUSY,NONE};
	private PopupState popupState;
	private ConveyorState state;
	private Transducer myTransducer;
	private enum SensorState {ON,OFF};
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private AnimState animIn;
	private AnimState animOut;
	private Glass incomingGlass;
	private Boolean end = false; //Is a piece of glass on the end of the conveyor
	private Boolean start = false; //Is a piece of glass at the start of the conveyor
	private Boolean moving; //Determines if the conveyor is moving or not
	private int startIndex = 0;
	private int endIndex = 1;
	
	public ConveyorAgent(ConveyorFamilyInterface prev)
	{
		previousConveyor = prev;
	}
	
	public ConveyorAgent(Integer n)
	{
		name = n;
		state = ConveyorState.DEFAULT;
		popupState = PopupState.DEFAULT;
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.ON;
		glass = new ArrayList<Glass>();
	}

	//!!MESSAGES!!
	public void msgHereIsGlass(Glass g) 
	{
		incomingGlass = g;
		animIn = AnimState.BUSY;
		stateChanged();
	}
	
	public void msgGiveMeGlass()
	{
		popupState = PopupState.APPROVED;
		stateChanged();
	}

	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction()
	{
			
		if(!glass.isEmpty())
		{
			if(popupState==PopupState.DEFAULT)
			{
				askPopup();
				return true;
			}
			if(popupState == PopupState.APPROVED)
			{
				giveGlass();
				return true;
			}
		}
		
		if(state!=ConveyorState.ASKED)
		{
			if(glass.size()<3)
			{
				if(!start)
				{
					askForGlass();
					return true;
				}
				if(!end)
				{
					startConveyor();
					return true;
				}
			}
		}
		return false;
	}
	
	//!!ACTIONS!!
	private void askForGlass()
	{
		state = ConveyorState.ASKED;
		//previousConveyor.msgIAmReady();
		//myTransducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
		stateChanged();
	}

	private void askPopup()
	{
		popupState = PopupState.ASKED;
		popup.msgCanIGiveGlass(glass.get(0).recipe.get(name));
		stateChanged();
	}
	
	private void giveGlass()
	{	
		popup.msgHereIsGlass(this, glass.remove(0));
		popupState = PopupState.DEFAULT;
		startConveyor();
		stateChanged();
	}

	
	//!!EXTRA!!
	public void startConveyor()
	{
		myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, null);
		//TODO Add in conveyor number to args
	}
	
	public void stopConveyor()
	{
		myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, null);
		//TODO add in conveyor number to args
	}
	
	public void setPopup(Popup p)
	{
		popup = p;
	}
	
	public void setPrevConveyor(ConveyorFamilyInterface cf)
	{
		previousConveyor = cf;
	}
	
	public void setTransducer(Transducer transducer)
	{
		myTransducer = transducer;
		myTransducer.register(this, TChannel.CONVEYOR);
	}
	
	//!!TRANSDUCER METHODS!!
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) 
	{
		if(channel == TChannel.SENSOR)
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				if((int)args[0]==startIndex)
				{
					sensorOne = SensorState.ON;
					start = true;
					if(animIn == AnimState.BUSY)
					{
						glass.add(incomingGlass);
						incomingGlass = null;
						animIn = AnimState.NONE;
					}
				}
				else if((int)args[0]==endIndex)
				{
					sensorTwo = SensorState.ON;
					end = true;
					if(popupState!=PopupState.APPROVED)
						stopConveyor();
				}
			}
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				if((int)args[0]==0)
				{
					sensorOne = SensorState.OFF;
					start = false;
				}
				else if((int)args[0]==1)
				{
					sensorTwo = SensorState.OFF;
					end = false;
					moving = false;
					stopConveyor();
				}
			}
		}
	}
	
	public String getName()
	{
		return ("Conveyor " + name + ": ");
	}
}