/*
 * Author: 		Nikhil Handyal
 * Date: 		4/1/13
 * Project:		CS201-Factory
 * Description:	
 */

package engine.conveyorfamily.n;

import engine.conveyorfamily.n.interfaces.*;
import transducer.Transducer;
import transducer.TReceiver;
import transducer.TChannel;
import transducer.TEvent;

public class Operator implements TReceiver{
	Transducer t;
	Popup parent;
	Integer[] operatorIndex = new Integer[1];
	
	public Operator(Transducer t, TChannel channel, Popup parent, int operatorIndex){
		System.out.println("Operator: "+channel);
		t.register(this, channel);
		this.t = t;
		this.parent = parent;
		this.operatorIndex[0] = operatorIndex;
	}
	
	public void loadGlass(){
		t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, operatorIndex);
	}
	
	public void performAction(){
		t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, operatorIndex);
	}
	
	public void releaseGlass(){
		t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_GLASS, operatorIndex);
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if( (Integer)args[0] == (Integer)operatorIndex[0] ){
			if(event == TEvent.WORKSTATION_LOAD_FINISHED){
				// start the action
				parent.operatorLoadFinished();
				performAction();
			}
			else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
				parent.operatorDone(this);
			}
			else if(event == TEvent.WORKSTATION_RELEASE_FINISHED){
				parent.operatorReleaseFinished();
			}
		}
	}
	
	
}