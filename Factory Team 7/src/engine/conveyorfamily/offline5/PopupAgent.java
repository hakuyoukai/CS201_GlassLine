package engine.conveyorfamily.offline5;

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
	enum PopupState {WAIT,READY, JAMMED};
	enum ConveyorState {ASKED, APPROVED, DEFAULT};
	enum NextConveyorState {READY, DEFAULT};
	enum GlassState {PROCESSED, UNPROCESSED};
	enum OperatorState {FULL, EMPTY, DONE, APPROVED, DISABLED, GLASS_BROKEN, NO_PROCESS};
	enum AnimState {DONE, RUN, DEFAULT};
	enum GlassBreak {YES, NO};
	
	int counter = 0;
	
	GlassState gState;
	PopupState popState;
	PopupStatus popStatus;
	NextConveyorState nextState;
	AnimState animState;
	
	private class MyOperator
	{
		GlassBreak doesGlassBreak;
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
			doesGlassBreak = GlassBreak.NO;
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
		
		//doesGlassBreak = GlassBreak.NO;
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
	
	public void msgCanIGive()
	{
		prevConveyor.state = ConveyorState.ASKED;
		stateChanged();
	}
	
	public void msgHereIsGlass(ConveyorAgent c, Glass g)
	{
		counter++;
		System.err.print("*** THIS HAS BEEN CALLED " + counter + " times!");
		pGlass = g;
		if(g.recipe.get(name) == false)
			gState = GlassState.PROCESSED;
		else
			gState = GlassState.UNPROCESSED;
		popState = PopupState.WAIT;
		stateChanged();
	}
	
	public void msgOnlyTopWorkstation(){
		for(MyOperator o: operators){
			if(o.args[0] == 1){
				//operators.remove(o);
				o.state = OperatorState.DISABLED;
			}
		}
	}
	
	public void msgReEnableBot(){
		for(MyOperator o: operators){
			if(o.args[0] == 1){
				o.state = OperatorState.EMPTY;
			}
		}
	}
	
	public void msgOnlyBotWorkstation(){
		for(MyOperator o: operators){
			if(o.args[0] == 0){
				//operators.remove(o);
				o.state = OperatorState.DISABLED;
			}
		}
	}
	
	public void msgReEnableTop(){
		for(MyOperator o: operators){
			if(o.args[0] == 0){
				o.state = OperatorState.EMPTY;
			}
		}
	}
	
	public void msgNoWorkstations(){
		for(MyOperator o: operators){
			if(o.args[0] == 1){
				//operators.remove(o);
				o.state = OperatorState.DISABLED;
			}
		}
		for(MyOperator o: operators){
			if(o.args[0] == 0){
				//operators.remove(o);
				o.state = OperatorState.DISABLED;
			}
		}
	}
	
	public void msgBreakGlass(int i){
		Integer[] tempArg = new Integer[1];
		tempArg[0] = i;
		for(MyOperator o: operators){
			if(o.args[0] == tempArg[0]){
				o.doesGlassBreak = GlassBreak.YES;
			}
		}
		stateChanged();
		
	}
	
	public void msgNoProcessGlass(int i){
		Integer[] tempArg = new Integer[1];
		tempArg[0] = i;
		for(MyOperator o: operators){
			if(o.args[0] == tempArg[0]){
				o.state = OperatorState.NO_PROCESS;
				this.transducer.fireEvent(o.channel, TEvent.WORKSTATION_NO_PROCESS, o.args);
			}
		}
		stateChanged();
	}
	
	public void msgReProcessGlass(int i){
//		Integer[] tempArg = new Integer[1];
//		tempArg[0] = i;
//		for(MyOperator o: operators){
//			if(o.args[0] == tempArg[0]){
//				o.state = OperatorState.EMPTY;
//				//this.transducer.fireEvent(o.channel, TEvent.WORKSTATION_NO_PROCESS, o.args);
//			}
//		}
//		stateChanged();
	}
	
	public void msgUnBreakGlass(int i){
		Integer[] tempArg = new Integer[1];
		tempArg[0] = i;
		for(MyOperator o: operators){
			if(o.args[0] == tempArg[0]){
				o.doesGlassBreak = GlassBreak.NO;
				this.transducer.fireEvent(o.channel, TEvent.WORKSTATION_UNBROKEN, o.args);
			}
		}
		
		stateChanged();
		
	}
	
	
	public void msgJamPopup(){
		popState = PopupState.JAMMED;
	}
	
	public void msgUnJamPopup(){
		popState = PopupState.READY;
	}

	//!!SCHEDULER!!
	@Override
	public boolean pickAndExecuteAnAction() 
	{
		if(popState!=PopupState.WAIT && popStatus!=PopupStatus.ANIM && popState!=PopupState.JAMMED)
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
					if(!operators.isEmpty()){
						for(MyOperator o:operators)
						{
							if(o.state == OperatorState.EMPTY)
							{
								if(popStatus == PopupStatus.UP)
								{
									//System.err.println("GIVING OPERATOR GLASS");
									System.err.println("OPERATOR NUMBER: " + o.args[0]);
									giveOperatorGlass(o);
									return true;
								}
								else
								{
									raisePopup();
									return true;
								}
							}
							if(o.state == OperatorState.DISABLED){
								if(nextState == NextConveyorState.READY)
								{
									if(popStatus == PopupStatus.DOWN)
									{
										giveConveyorGlass();
										//o.state = OperatorState.
										givePrevAnswer();
										return true;
									}
									else
									{
										System.err.println("LOWERING POPUP");
										lowerPopup();
										return true;
									}
								}
							}
							
//							if(o.state == OperatorState.GLASS_BROKEN){
//								if(popStatus == PopupStatus.UP)
//								{
//									//System.err.println("GIVING OPERATOR GLASS");
//									giveOperatorGlass(o);
//									return true;
//								}
//								else
//								{
//									raisePopup();
//									return true;
//								}
//							}
						}
					}
				}
			}
			else
			{
				if(!operators.isEmpty()){
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
						//if(o.state == OperatorState.EMPTY
//						else if(o.state == OperatorState.GLASS_BROKEN){
//							if(popStatus == PopupStatus.UP)
//							{
//								lowerPopup();	
//								
//								return true;
//							}
////							else
////							{
////								return true;
////							}
//						}
						
					}
					}
				}
				
				if(prevConveyor.state == ConveyorState.ASKED)
				{
					if(popStatus == PopupStatus.DOWN)
					{
						System.err.println("GIVE PREVIOUS THE ANSWER DUDE");
						givePrevAnswer();
						return true;
					}
					else
					{
						System.err.println("LOWER ZE POPSS");
						lowerPopup();
						return true;
					}
				}
			}
		return false;
	}
	
	
	//!!ACTIONS!!
	public void giveConveyorGlass()
	{
		System.err.println("GIVE CONVEYOR GLASS CALLED");
		nextConveyor.msgHereIsGlass(pGlass);
		pGlass = null;
		nextState = NextConveyorState.DEFAULT;
		popState = PopupState.WAIT;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, popArgs);
		stateChanged();
	}
	
	public void giveOperatorGlass(MyOperator o)
	{
		System.err.println("GIVE OPERATOR GLASS CALLED");
		System.err.println("GIVING OPERATOR GLASS");
		
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
			if(o.state == OperatorState.EMPTY || o.state == OperatorState.DISABLED)
			{
				System.err.println("Told prev to gimme glass!");
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
						if(o.doesGlassBreak == GlassBreak.NO){
							transducer.fireEvent(o.channel, TEvent.WORKSTATION_DO_ACTION, o.args);
							o.state =OperatorState.FULL;
							popState = PopupState.READY;
							//System.out.println("i am not gonna break you");
							stateChanged();
						}
						else{
							transducer.fireEvent(o.channel, TEvent.WORKSTATION_BREAK_GLASS, o.args);
							o.state = OperatorState.FULL;
							popState = PopupState.READY;
							stateChanged();
						}
					}
				}
			}
			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				//if(doesGlassBreak == GlassBreak.NO){
					System.out.println("GUI ACTION FINISHED");
					for(MyOperator o:operators)
					{
						if(args[0] == o.args[0])
						{
							o.state = OperatorState.DONE;
						}
					}
					stateChanged();
//				}
//				else{
//					System.out.println("GLASS BREAKS");
//					
//				}

			}
			if(event == TEvent.WORKSTATION_BROKEN){
				//System.err.println("Hello");
				for(MyOperator o:operators)
				{
					if(args[0] == o.args[0])
					{
						//System.err.println("WHY THE HECK IS THIS NOT WORKING?");
//						o.g = null;
//						o.state = OperatorState.EMPTY;
						o.state = OperatorState.GLASS_BROKEN;
						//o.state = OperatorState.DONE;
					}
				}
				//givePrevAnswer();
				stateChanged();
			}			
		}
	}
	
	

	public void setPrevious(ConveyorAgent conveyor)
	{
		prevConveyor = new MyConveyor(conveyor);
	}

	public void setNext(ConveyorFamilyInterface cF) 
	{
		nextConveyor = cF;
	}
	
	public String getName()
	{
		return "CONVEYOR " + name;
	}
}
