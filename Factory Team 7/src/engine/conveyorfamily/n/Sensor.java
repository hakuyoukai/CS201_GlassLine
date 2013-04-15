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

public class Sensor implements TReceiver{
	Controller parent;
	int sensorIndex;
	
	// constructor
	Sensor(Transducer t, Controller parent, int sensorIndex){
		t.register(this, TChannel.SENSOR);				// Register with the Transducer
		this.parent = parent;
		this.sensorIndex = sensorIndex;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// The sensor has only registered on the SENSOR channel so you only need to look at the event
		if((Integer)args[0] == sensorIndex){
			if(event == TEvent.SENSOR_GUI_PRESSED)
				parent.sensorPressed(sensorIndex);
			else
				parent.sensorReleased(sensorIndex);
		}
	}
}