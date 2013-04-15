package engine.conveyorfamily.n.interfaces;

import engine.util.*;

public interface Popup {
	public void glassStaged(Glass g);
	public void operatorLoadFinished();
	public void operatorReleaseFinished();
	public void operatorDone(engine.conveyorfamily.n.Operator operator);
	public void popupRaised();
	public void push();
}
