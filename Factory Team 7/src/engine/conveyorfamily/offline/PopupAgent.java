package engine.conveyorfamily.offline;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class PopupAgent extends Agent implements TReceiver
{
	private Transducer transducer;
	private Integer name;
	private Glass pGlass;
	private ConveyorFamilyInterface nextConveyor;
	private ArrayList<MyOperator> operators;
	private MyConveyor prevConveyor;
	private Integer[] popArgs = new Integer[1];
	private TChannel opChannel;
	
	enum PopupStatus {UP,DOWN,ANIM};
	enum PopupState {WAIT,READY};
	enum ConveyorState {ASKED, APPROVED, DEFAULT};
	enum NextConveyorState {READY, DEFAULT};
	enum GlassState {PROCESSED, UNPROCESSED};
	enum OperatorState {FULL, EMPTY, DONE, APPROVED};
	enum AnimState {DONE, RUN, DEFAULT};
	
	GlassState gState;
	PopupState popState;
	PopupStatus popStatus;
	NextConveyorState nextState;
	AnimState animState;
	
	private class MyOperator
	{
		Glass g;
		OperatorState state;
		TChannel channel;
		Integer args[] = new Integer[1];
		
		public MyOperator(TChannel c, Integer num)
		{
			channel = c;
			args[0] = num;
			g = null;
			state = OperatorState.EMPTY;
		}
	}
	
	private class MyConveyor
	{
		ConveyorAgent conveyor;
		ConveyorState state;
		Boolean action;
		
		public MyConveyor(ConveyorAgent c)
		{
			conveyor = c;
			state = ConveyorState.DEFAULT;
		}
	}
	
	public PopupAgent(Transducer t, Integer n)
	{
		transducer = t;
		name = n;
		switch(n)
		{
			case 5:
				opChannel = TChannel.DRILL;
				break;
			case 6:
				opChannel = TChannel.CROSS_SEAMER;
				break;
			case 7:
				opChannel = TChannel.GRINDER;
				break;
		}
		t.register(this, opChannel);
		t.register(this, TChannel.POPUP);
		
		popArgs[0] = n-5;
		
		popState = PopupState.READY;
		popStatus = PopupStatus.UP;
		operators = new ArrayList<MyOperator>();
		operators.add(new MyOperator(opChannel, 0));
		operators.add(new MyOperator(opChannel,1));
		animState = AnimState.DEFAULT;
	}
	
	//!!MESSAGES!!
	public void msgIAmReady()
	{
		nextState = NextConveyorState.READY;
		stateChanged();
	}
	
	public void msgCanIGiveGlass(Boolean action)
	{
		prevConveyor.action = action;
		prevConveyor.state = ConveyorState.ASKED;
		stateChanged();
	}
	
	public void msgHereIsGlass(ConveyorAgent c, Glass g)
	{
		pGlass = g;
		if(g.recipe.get(name) == false)
			gState = GlassState.PROCESSED;
		else
			gState = GlassState.UNPROCESSED;
		popState = PopupState.WAIT;
		stateChanged();
	}

	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction() 
	{
		if(popState!=PopupState.WAIT && popStatus!=PopupStatus.ANIM)
		{
			if(pGlass!=null)
			{
				if(gState == GlassState.PROCESSED)
				{
					if(nextState == NextConveyorState.READY)
					{
						if(popStatus == PopupStatus.DOWN)
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
							if(popStatus == PopupStatus.UP)
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
				if(prevConveyor.state!=ConveyorState.APPROVED)
				{
				for(MyOperator o:operators)
				{
					if(o.state == OperatorState.DONE)
						{
							if(popStatus == PopupStatus.UP)
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
				}
				if(prevConveyor.state == ConveyorState.ASKED)
				{
					if(popStatus == PopupStatus.DOWN)
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
		nextConveyor.msgHereIsGlass(pGlass);
		pGlass = null;
		nextState = NextConveyorState.DEFAULT;
		popState = PopupState.WAIT;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, popArgs);
		stateChanged();
	}
	
	public void giveOperatorGlass(MyOperator o)
	{
		o.g = pGlass;
		pGlass = null;
		transducer.fireEvent(o.channel, TEvent.WORKSTATION_DO_LOAD_GLASS,o.args);
		o.state = OperatorState.FULL;
		popState = PopupState.WAIT;
		stateChanged();
	}
	
	public void sendPart(MyOperator o)
	{
		transducer.fireEvent(o.channel, TEvent.WORKSTATION_RELEASE_GLASS,o.args);
		o.state = OperatorState.APPROVED;
		popState = PopupState.WAIT;
		stateChanged();
	}
	
	public void givePrevAnswer()
	{
		for(MyOperator o:operators)
		{
			if(o.state == OperatorState.EMPTY)
			{
				prevConveyor.conveyor.msgGiveMeGlass();
				prevConveyor.state = ConveyorState.APPROVED;
				popState = PopupState.WAIT;
				stateChanged();
				return;
			}
		}
	}
	
	public void raisePopup()
	{
		popStatus = PopupStatus.ANIM;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, popArgs);
		stateChanged();
	}
	
	public void lowerPopup()
	{
		popStatus = PopupStatus.ANIM;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, popArgs);
		stateChanged();
	}
	

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == TChannel.POPUP)
		{
			if(args[0] == popArgs[0])
			{
				if(event == TEvent.POPUP_GUI_MOVED_DOWN)
				{
					System.out.println("POPUP MOVED DOWN");
					popStatus = PopupStatus.DOWN;
				}
				else if(event == TEvent.POPUP_DO_MOVE_UP)
				{
					System.out.println("POPUP MOVED UP");
					popStatus = PopupStatus.UP;
				}
				else if(event == TEvent.POPUP_GUI_LOAD_FINISHED)
				{
					popState = PopupState.READY;
				}
				else if(event == TEvent.POPUP_GUI_RELEASE_FINISHED)
				{
					popState = PopupState.READY;
					for(MyOperator o:operators)
					{
						if(o.state == OperatorState.APPROVED)
						{
							o.g = null;
							o.state = OperatorState.EMPTY;
							gState = GlassState.PROCESSED;
						}
					}
				}
				stateChanged();
			}
		}
		else if(channel == operators.get(0).channel)
		{
			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				for(MyOperator o: operators)
				{
					if(args[0] == o.args[0])
					{
						pGlass = o.g;
						o.g = null;
						o.state = OperatorState.EMPTY;
						gState = GlassState.PROCESSED;
						popState = PopupState.READY;
						stateChanged();
					}
				}

			}
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				for(MyOperator o:operators)
				{
					if(args[0] == o.args[0])
					{
						transducer.fireEvent(o.channel, TEvent.WORKSTATION_DO_ACTION, o.args);
						o.state =OperatorState.FULL;
						popState = PopupState.READY;
						stateChanged();
					}
				}
			}
			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				for(MyOperator o:operators)
				{
					if(args[0] == o.args[0])
					{
						o.state = OperatorState.DONE;
						stateChanged();
					}
				}
			}
		}
	}

	public void setPrevious(ConveyorAgent conveyor)
	{
		prevConveyor = new MyConveyor(conveyor);
	}

	public void setNextConveyor(ConveyorFamilyInterface cF) 
	{
		nextConveyor = cF;
	}
	
	public String getName()
	{
		return "CONVEYOR " + name;
	}
}