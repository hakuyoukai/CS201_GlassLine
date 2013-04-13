package engine.conveyorfamily.d;

//import generalClasses.Glass;
import engine.util.*;

public interface Sensor {
	public void msgHereIsGlass(Glass g);
	public void msgPopupReady();
	public void msgPopupNotReady();
	public void msgConveyorReady();
	public void msgConveyorNotReady();
}
