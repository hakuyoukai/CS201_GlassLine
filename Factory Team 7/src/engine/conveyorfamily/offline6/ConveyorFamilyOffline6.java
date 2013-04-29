package engine.conveyorfamily.offline6;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;

public class ConveyorFamilyOffline6 implements ConveyorFamilyInterface, TReceiver{

	private Integer cNumber, pNumber;
	private ConveyorAgent conveyor;
	private PopupAgent popup;
	private Transducer transducer;
	private TChannel opChannel;
	
	public ConveyorFamilyOffline6(Transducer t, ConveyorFamilyInterface prev, Integer num){
		transducer = t;
		cNumber = num;
		pNumber = num - 5;
		conveyor = new ConveyorAgent(t,cNumber);
		popup = new PopupAgent(t,cNumber);
		conveyor.setNext(popup);
		conveyor.setPrevious(prev);
		popup.setPrevious(conveyor);
		transducer.register(this, TChannel.CONTROL_PANEL);
	}
	
	public void setNext(ConveyorFamilyInterface cF){
		popup.setNext(cF);
	}
			
	public void startThread(){
		popup.startThread();
		conveyor.startThread();
	}
	
	public void setOpChannel(TChannel opChannel){
		this.opChannel = opChannel;
	}
	
	@Override
	public void msgHereIsGlass(Glass g){
		conveyor.msgHereIsGlass(g);
	}

	@Override
	public void msgIAmReady(){
		popup.msgIAmReady();
	}
	
	@Override
	public void msgDeleteGlass(Glass g){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// listen for jam evnts and send them to the appr agents
		if( event == TEvent.CONVEYOR_JAM){
			if((Integer)args[0] == cNumber){
				System.err.println("Message received conveyer jammed");
				conveyor.setJamState(true);
			}
		}
		else if( event == TEvent.CONVEYOR_UNJAM){
			if((Integer)args[0] == cNumber){
				System.err.println("Message received conveyer un-jammed");
				conveyor.setJamState(false);
			}
		}
		else if( event == TEvent.POPUP_JAM){
			if((Integer)args[0] == pNumber){
				System.err.println("Message received upper popup jammed");
				popup.jamPopup(true);
			}
		}
		else if( event == TEvent.POPUP_UNJAM){
			if((Integer)args[0] == pNumber){
				System.err.println("Message received popup un-jammed");
				popup.jamPopup(false);
			}
		}
		// For workstation events
		// args[0] == index of workstation
		// args[1] == upper or lower workstation 0 = top, 1 = bottom
		else if( event == TEvent.WORKSTATION_BROKEN){
			if((Integer)args[0] == pNumber){
				if( (Integer)args[1] == 0 ){
					System.err.println("Message received upper workstation jammed");
				}
				else if( (Integer)args[1] == 1){
					System.err.println("Message received lower workstation jammed");
				}
			}
		}
		else if( event == TEvent.WORKSTATION_WORKING){
			if((Integer)args[0] == pNumber){
				if( (Integer)args[1] == 0 ){
					System.err.println("Message received upper workstation working");
				}
				else if( (Integer)args[1] == 1){
					System.err.println("Message received lower workstation working");
				}
			}
		}
		else if( event == TEvent.WORKSTATION_BREAK_GLASS){
			if((Integer)args[0] == pNumber){
				if( (Integer)args[1] == 0 ){
					System.err.println("Message received upper workstation glass broke");
				}
				else if( (Integer)args[1] == 1){
					System.err.println("Message received lower workstation glass broke");
				}
			}
		}
		else if( event == TEvent.WORKSTATION_DONT_BREAK_GLASS){
			if((Integer)args[0] == pNumber){
				if( (Integer)args[1] == 0 ){
					System.err.println("Message received upper workstation glass ok");
				}
				else if( (Integer)args[1] == 1){
					System.err.println("Message received lower workstation glass ok");
				}
			}
		}
	}
}