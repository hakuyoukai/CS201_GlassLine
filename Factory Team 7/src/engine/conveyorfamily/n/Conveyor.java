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

public class Conveyor implements TReceiver{
	Transducer t;
	Controller parent;
	Integer[] conveyorIndex = new Integer[1];
	
	public Conveyor(Transducer t, Controller parent, int conveyorIndex){
		this.parent = parent;
		this.t = t;
		t.register(this, TChannel.CONVEYOR);
		this.conveyorIndex[0] = conveyorIndex; 
	}

	public void startConveyor(){
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorIndex);
	}
	
	public void stopConveyor(){
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorIndex);
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {}
}