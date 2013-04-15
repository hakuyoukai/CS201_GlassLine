package engine.conveyorfamily.n.interfaces;

import engine.util.*;

public interface Controller {
	public void bindNeighbors(ConveyorFamilyInterface source, ConveyorFamilyInterface destination);
	public void sensorPressed(int si);
	public void sensorReleased(int si);
	public void releaseToPopup();
	public void releasingGlass(Glass g);
}
