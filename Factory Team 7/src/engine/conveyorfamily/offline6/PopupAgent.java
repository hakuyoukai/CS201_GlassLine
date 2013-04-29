package engine.conveyorfamily.offline6;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class PopupAgent extends Agent implements TReceiver{
	private Transducer transducer;
	private Integer name;
	private Glass pGlass;
	private ConveyorFamilyInterface nextConveyor;
	private ArrayList<MyOperator> operators;
	private MyConveyor prevConveyor;
	private Integer[] popArgs = new Integer[1];
	private TChannel opChannel;
	
	enum PopupStatus {UP,DOWN,ANIM};						// is it raised or lowered
	enum PopupState {WAIT,READY};							// is the popup free to go do something else now
	enum ConveyorState {ASKED, APPROVED, DEFAULT};
	enum NextConveyorState {READY, DEFAULT};
	enum GlassState {PROCESSED, UNPROCESSED};
	enum OperatorState {FULL, EMPTY, DONE, APPROVED, GLASS_BROKEN, JAMMED};
	enum AnimState {DONE, RUN, DEFAULT};
	
	GlassState gState;				// this is the state of the glass on the popup
	PopupState popState;
	PopupStatus popStatus;
	NextConveyorState nextState;
	AnimState animState;
	
	
	// ************************************************ //
    // ************* CLASS DECLARATIONS *************** //
    // ************************************************ //
	private class MyOperator{
		Glass g;					// the glass currently loaded onto the operator
		OperatorState state;		// operator state
		TChannel channel;			// channel to listen on
		Integer args[] = new Integer[1];
		
		public MyOperator(TChannel c, Integer num){
			channel = c;
			args[0] = num;
			g = null;
			state = OperatorState.EMPTY;
		}
	}
	
	private class MyConveyor{
		ConveyorAgent conveyor;
		ConveyorState state;
		Boolean action;
		
		public MyConveyor(ConveyorAgent c){
			conveyor = c;
			state = ConveyorState.DEFAULT;
		}
	}
	
	public PopupAgent(Transducer t, Integer n){
		transducer = t;
		name = n;
		switch(n){
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
		popStatus = PopupStatus.DOWN;
		operators = new ArrayList<MyOperator>();
		operators.add(new MyOperator(opChannel, 0));
		operators.add(new MyOperator(opChannel,1));
		animState = AnimState.DEFAULT;
	}
	
	// ************************************************ //
    // ***************** MESSAGES ********************* //
    // ************************************************ //
	public void msgIAmReady(){							// msg from dest informing they are ok to accept
		nextState = NextConveyorState.READY;
		stateChanged();
	}
	
	public void msgCanIGive(){							// msg from source asking if it's ok to release glass
		prevConveyor.state = ConveyorState.ASKED;
		stateChanged();
	}
	
	public void msgHereIsGlass(ConveyorAgent c, Glass g){	// msg from source giving you glass
		System.err.println("popup accept glass");
		pGlass = g;
		if(g.recipe.get(name) == false)						// identifies if the glass needs to be proccessed or not
			gState = GlassState.PROCESSED;
		else
			gState = GlassState.UNPROCESSED;
		popState = PopupState.WAIT;							// popup is now waiting for animation load to complete
		stateChanged();
	}

	// ************************************************ //
    // ***************** SCHEDULER ******************** //
    // ************************************************ //
	// Priority levels
	// 1: clear popup of glass
	// 2: clear operators of glass
	// 3: accept new glass
	@Override
	public boolean pickAndExecuteAnAction() {
		if(popState!=PopupState.WAIT && popStatus!=PopupStatus.ANIM){		// if the popup is not waiting or animating 
			if(pGlass!=null){												// if there is glass on the popup
				if(gState == GlassState.PROCESSED){							// if glass has been processed check if the next conveyor can accept the glass
					if(nextState == NextConveyorState.READY){				// if the next conveyor can accept the glass and the popup is down, off load glass, else lower the popup
						if(popStatus == PopupStatus.DOWN){
							giveConveyorGlass();				// --> offload popup glass to next CF
							return true;	
						}
						else{
							lowerPopup();						// --> lower popup
							return true;
						}
					}
				}
				else if(gState == GlassState.UNPROCESSED){						// there is glass on the popup that needs to be processed
					for(MyOperator o:operators){								// raise the popup then find the first operator that is free and give it to that operator
						if(o.state == OperatorState.EMPTY){						// IMPLEMENT OPERATOR NON-NORMS HERE
							if(popStatus == PopupStatus.UP){
								giveOperatorGlass(o);							// --> popup is raised and operator can accept glass
								return true;
							}
							else{
								raisePopup();									// --> popup needs to be raised before it can give to operator
								return true;
							}
						}
					}
				}
			}
			else{																// There is no glass on the popup
				printState("Scheduler no glass on popup");
				for(MyOperator o:operators){
					if(o.state == OperatorState.DONE){
							if(popStatus == PopupStatus.UP){
								printState("scheduler accept workstation glass");
								sendPart(o);									// --> offload part from operator
								return true;
							}
							else {
								raisePopup();									// --> raise popup before offloading operator
								return true;
							}
						}
					}
				}
				if(prevConveyor.state == ConveyorState.ASKED){					// operators are not done and they might be full no guarantee of being free
					if(popStatus == PopupStatus.DOWN){							// if the popup is down, check to see if there is a free op and then give the answer
						givePrevAnswer();	
						return true;
					}
					else{														
						lowerPopup();
						return true;
					}
				}
			}
		return false;
	}
	
	
	// ************************************************ //
    // ****************** ACTIONS ********************* //
    // ************************************************ //
	public void giveConveyorGlass(){							// offload the glass to the next conveyor and wait till the animation completes
		System.err.println("offload glass to next CF");
		nextConveyor.msgHereIsGlass(pGlass);
		nextState = NextConveyorState.DEFAULT;
		popState = PopupState.WAIT;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, popArgs);
		stateChanged();
	}
	
	public void giveOperatorGlass(MyOperator o){				// offload the glass to the free operator and wait till the animation completes. OP state = full
		System.err.println("giving operator glass");
		transducer.fireEvent(o.channel, TEvent.WORKSTATION_DO_LOAD_GLASS,o.args);
		o.state = OperatorState.FULL;
		popState = PopupState.WAIT;
		printState("operator accepting glass from popup");
		stateChanged();
	}
	
	public void sendPart(MyOperator o){							// accept finished part from operator, OP state = approved; approved to release
		transducer.fireEvent(o.channel, TEvent.WORKSTATION_RELEASE_GLASS,o.args);
		o.state = OperatorState.APPROVED;
		popState = PopupState.WAIT;
		printState("popup accept part from operator");
		stateChanged();
	}
	
	public void givePrevAnswer(){								// if there is a free operator, tell the conveyor to release glass to the popup
		for(MyOperator o:operators){
			if(o.state == OperatorState.EMPTY){
				prevConveyor.conveyor.msgGiveMeGlass();
				prevConveyor.state = ConveyorState.APPROVED;	// approved to release glass
				popState = PopupState.WAIT;
				stateChanged();
				return;
			}
		}
	}
	
	public void raisePopup(){									// popup is being raised
		popStatus = PopupStatus.ANIM;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, popArgs);
		stateChanged();
	}
	
	public void lowerPopup(){									// // popup is being lowered
		popStatus = PopupStatus.ANIM;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, popArgs);
		stateChanged();
	}
	
	// ************************************************ //
    // ****************** TRANSDUCER ****************** //
    // ************************************************ //
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args){
		if(channel == TChannel.POPUP){										// Popup events
			if(args[0] == popArgs[0]){
				if(event == TEvent.POPUP_GUI_MOVED_DOWN){					// Popup moved down
					System.out.println("POPUP MOVED DOWN");
					popStatus = PopupStatus.DOWN;
					popState = PopupState.READY;
					stateChanged();
				}
				else if(event == TEvent.POPUP_DO_MOVE_UP){					// popup moved up
					System.out.println("POPUP MOVED UP");
					popStatus = PopupStatus.UP;
					popState = PopupState.READY;
					stateChanged();
				}
				else if(event == TEvent.POPUP_GUI_LOAD_FINISHED){			// popup gui load finished --> load from conveyor to popup. The popup must logically be down when this happens
					System.out.println("POPUP GUI LOAD FINISHED");
					popStatus = PopupStatus.DOWN;
					popState = PopupState.READY;
					stateChanged();
				}
				else if(event == TEvent.POPUP_GUI_RELEASE_FINISHED){		// popup gui release finished --> this only happens when the popup releases to the next CF
					System.out.println("POPUP GUI RELEASE FINISHED");
					popStatus = PopupStatus.DOWN;
					popState = PopupState.READY;							// once the relese has happened, clear the operator for next use
					pGlass = null;
				}
			}
		}
		else if(channel == operators.get(0).channel){						// workstation events
			if(event == TEvent.WORKSTATION_RELEASE_FINISHED){				// when the workstation releases the glass to the popup
				popStatus = PopupStatus.UP;
				for(MyOperator o: operators){
					if(o.state == OperatorState.APPROVED){
						pGlass = o.g;
						o.g = null;
						o.state = OperatorState.EMPTY;
						gState = GlassState.PROCESSED;
						popState = PopupState.READY;
						stateChanged();
						printState("workstation release glass to popup");
						return;
					}
				}

			}
			else if(event == TEvent.WORKSTATION_LOAD_FINISHED){					// when the workstation loads the glass from the popup 
				System.out.println("WORKSTATION LOAD FINISHED");
				popStatus = PopupStatus.UP;
				for(MyOperator o:operators){
					if(args[0] == o.args[0]){
						o.g = pGlass;
						pGlass = null;
						transducer.fireEvent(o.channel, TEvent.WORKSTATION_DO_ACTION, o.args);
						o.state = OperatorState.FULL;
						popState = PopupState.READY;
						stateChanged();
						printState("workstation load from popup");
						return;
					}
				}
			}
			else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){			// when the workstation has finihsed processing the glass
				for(MyOperator o:operators){
					if(args[0] == o.args[0]){
						o.state = OperatorState.DONE;
						stateChanged();
						return;
					}
				}
			}
		}
	}

	// ************************************************ //
    // ***************** UTILITIES ******************** //
    // ************************************************ //
	public void setPrevious(ConveyorAgent conveyor){
		prevConveyor = new MyConveyor(conveyor);
	}

	public void setNext(ConveyorFamilyInterface cF) {
		nextConveyor = cF;
	}
	
	public String getName(){
		return "CONVEYOR " + name;
	}
	
	public boolean freeOP(){
		for(MyOperator o:operators){
			if(o.state == OperatorState.EMPTY)
				return true;
		} 
		
		return false;
	}
	
	public void setState(){
	}
	
	public void printState(String msg){
		System.out.println("-------CF Popup---------");
		System.out.println(msg);
		System.out.println("popState: "+popState);
		System.out.println("popStatus: "+popStatus);
		System.out.println("pGlass: "+pGlass);
		System.out.println("------------------------");
	}
}