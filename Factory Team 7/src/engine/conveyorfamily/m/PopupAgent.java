package engine.conveyorfamily.m;


import java.util.ArrayList;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

import engine.agent.*;
import engine.util.*;


public class PopupAgent extends Agent implements Popup, TReceiver
{
	Integer name;
	private Glass glass;
	private ArrayList<MyOperator> operators;
	private MyConveyor previousConveyor;
	private ConveyorFamilyInterface nextConveyor;
	
	enum PopupStatus {UP, DOWN, ANIM};
	enum PopupState {WAIT,READY};
	enum NextConveyorState {READY,DEFAULT};
	enum ConveyorState {ASKED,  APPROVED, DEFAULT};
	enum GlassState {PROCESSED, UNPROCESSED};
	enum OperatorState {FULL, EMPTY, DONE, APPROVED};
	enum AnimState {DONE, RUN, DEFAULT};
	
	GlassState gState;
	PopupState state;
	PopupStatus status;
	NextConveyorState nextState;
	AnimState animState;
	
	private Transducer myTransducer;
	
	private class MyOperator
	{
		Glass g;
		OperatorState state;
		
		public MyOperator()
		{
			g = null;
			state = OperatorState.EMPTY;
		}
	}
	
	private class MyConveyor
	{
		Conveyor conveyor;
		ConveyorState state;
		Integer action;
		
		public MyConveyor(Conveyor c)
		{
			conveyor = c;
			state = ConveyorState.DEFAULT;
		}
		
	}
	
	public PopupAgent(ConveyorFamilyInterface next)
	{
		nextConveyor = next;
		operators = new ArrayList<MyOperator>();
	}
	
	public PopupAgent(Integer n)
	{
		name = n;
		state = PopupState.READY;
		status = PopupStatus.UP;
		operators = new ArrayList<MyOperator>();
		animState = AnimState.DEFAULT;
	}
	
	//!!MESSAGES!!
	public void msgIAmReady() 
	{
		nextState = NextConveyorState.READY;
		stateChanged();
	}
	
	public void msgCanIGiveGlass(Integer action)
	{
		previousConveyor.action=action;
		previousConveyor.state = ConveyorState.ASKED;
		stateChanged();
	}
	
	public void msgHereIsGlass(Conveyor c, Glass g)
	{
		glass = g;
		if(g.recipe.get(name) == 0)
			gState = GlassState.PROCESSED;
		else
			gState = GlassState.UNPROCESSED;
		state = PopupState.WAIT;
		stateChanged();
	}
	
	/*//Remnant from when I thought Operators were seperate agents
	public void msgImDone(Operator op)
	{
		for(MyOperator o:operators)
			if(o.operator.equals(op))
				o.state = OperatorState.DONE;
		stateChanged();
	}
	*/
	
	/*//Remnant from when I thought Operators were seperate agents
	public void msgHereIsGlass(Operator op, Glass g)
	{
		for(MyOperator o:operators)
			if(o.operator.equals(op))
				o.state = OperatorState.EMPTY;
		glass = g;
		gState = GlassState.PROCESSED;
		state = PopupState.WAIT;
		stateChanged();
	}
	*/

	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction() 
	{
		if(state!=PopupState.WAIT && status!=PopupStatus.ANIM)
		{
			if(glass!=null)
			{
				if(gState == GlassState.PROCESSED)
				{
					if(nextState == NextConveyorState.READY)
					{
						if(status == PopupStatus.DOWN)
						{
							giveConveyorGlass();
							return true;
						}
						else
						{
							lowerPopup();
							return true;
						}
						
					}
				}
				if(gState == GlassState.UNPROCESSED)
				{
					for(MyOperator o:operators)
					{
						if(o.state == OperatorState.EMPTY)
						{
							if(status == PopupStatus.UP)
							{
								giveOperatorGlass(o);
								return true;
							}
							else
							{
								raisePopup();
								return true;
							}
						}
					}
				}
			}
			else
			{
				for(MyOperator o:operators)
				{
					if(o.state == OperatorState.DONE)
					{
						if(status == PopupStatus.UP)
						{
							sendPart(o);
							return true;
						}
						else
						{
							raisePopup();
							return true;
						}
					}
				}
				
				if(previousConveyor.state == ConveyorState.ASKED)
				{
					if(status == PopupStatus.DOWN)
					{
						givePrevAnswer();
						return true;
					}
					else
					{
						lowerPopup();
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//!!ACTIONS!!
	public void giveConveyorGlass()
	{
		nextConveyor.msgHereIsGlass(glass);
		nextState = NextConveyorState.DEFAULT;
		glass = null;
		state = PopupState.WAIT;
		//myTransducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_RELEASE_GLASS,null);
		//myTransducer.POPUP_RELEASE_GLASS();
		stateChanged();
	}
	
	public void giveOperatorGlass(MyOperator o)
	{
		o.g = glass;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.WORKSTATION_DO_LOAD_GLASS,null);
		//TODO add in args, change POPUP to specific workstation
		o.state = OperatorState.FULL;
		glass = null;
		state = PopupState.WAIT;
		stateChanged();
	}
	
	public void sendPart(MyOperator o)
	{
		myTransducer.fireEvent(TChannel.POPUP, TEvent.WORKSTATION_RELEASE_GLASS, null);
		//TODO Add in args, change POPUP to specific workstation
		o.state = OperatorState.APPROVED;
		state = PopupState.WAIT;
		stateChanged();
	}
	
	public void givePrevAnswer()
	{
		for(MyOperator o:operators)
		{
			if(o.state == OperatorState.EMPTY)
			{
				previousConveyor.conveyor.msgGiveMeGlass();
				previousConveyor.state = ConveyorState.APPROVED;
				state = PopupState.WAIT;
				stateChanged();
				return;
			}

		}
		if(previousConveyor.action ==0)
		{
			previousConveyor.conveyor.msgGiveMeGlass();
			previousConveyor.state = ConveyorState.APPROVED;
			state = PopupState.WAIT;
			stateChanged();
			return;
		}
	}
	
	public void raisePopup()
	{
		status = PopupStatus.ANIM;
		//myTransducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_DO_MOVE_UP,null);
		//myTransducer.POPUP_DO_MOVE_UP();
		stateChanged();
	}
	
	public void lowerPopup()
	{
		status = PopupStatus.DOWN;
		//myTransducer.fireEvent(TChannel.ALL_GUI, TEvent.POPUP_DO_MOVE_DOWN, null);
		//myTransducer.POPUP_DO_MOVE_DOWN();
		stateChanged();
	}
	
	//!!EXTRA!!
	public void setPrevious(Conveyor c)
	{
		previousConveyor = new MyConveyor(c);
	}
	
	public void setNext(ConveyorFamilyInterface cf)
	{
		nextConveyor = cf;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		 if(channel == TChannel.ALL_GUI)
			{
				if(event == TEvent.POPUP_GUI_MOVED_DOWN)
					status = PopupStatus.DOWN;
				if(event == TEvent.POPUP_GUI_MOVED_UP)
					status = PopupStatus.UP;
				if(event == TEvent.POPUP_GUI_LOAD_FINISHED)
				{
					state = PopupState.READY;
				}
				if(event == TEvent.POPUP_GUI_RELEASE_FINISHED)
				{
					status = PopupStatus.DOWN;
					state = PopupState.READY;
				}
				if(event == TEvent.WORKSTATION_LOAD_FINISHED)
				{
					status = PopupStatus.UP;
					state = PopupState.READY;
				}
				if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
				{
					//if(WORKSTATION ONE)
					status = PopupStatus.UP;
					state = PopupState.READY;
					glass = operators.get(0).g;
					operators.get(0).state = OperatorState.EMPTY;
					gState = GlassState.PROCESSED;
					state = PopupState.WAIT;
					//if(WORKSTATION TWO)	
				}
				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
				{
					//if(WORKSTATION ONE)
					operators.get(0).state = OperatorState.DONE;
				}
			}
	}
	
	public void setTransducer(Transducer transducer)
	{
		
		myTransducer = transducer;
		myTransducer.register(this, TChannel.POPUP);
	}
	
	public String getName()
	{
		return ("Popup " + name + ": ");
	}
	
}