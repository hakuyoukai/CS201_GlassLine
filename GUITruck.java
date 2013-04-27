
package gui.components;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * The truck collects glass that has been taken off of the line collecting three
 * glass parts and emptying itself on the fourth
 */
@SuppressWarnings("serial")
public class GUITruck extends GuiComponent
{

	private int TRUCK_SPEED = 5;//added by monroe. default was 2 before, and hard-coded everywhere.

	private int MAX_TRUCK_SPEED =10;//added by monroe.
	private int MIN_TRUCK_SPEED = 1;//added by monroe.

	//added to allow multiple parts to be loaded and shown on the truck
	List<MyGUIGlass> parts = Collections.synchronizedList( new ArrayList<MyGUIGlass>() );
	
	//the boolean prevents GUITruck from sending multiple load confirmations to the agents
	private class MyGUIGlass {
		GUIGlass part;
		boolean isLoaded = false;
		
		public MyGUIGlass( GUIGlass part ) {
			this.part = part;
		}
	}

	/**
	* Allows changes to be made mid-program to GUITruck speed (perhaps a GUI button).
	* @author Monroe
	*/
	public void changeTruckSpeed(int change){
		TRUCK_SPEED +=change;
		if(TRUCK_SPEED > MAX_TRUCK_SPEED){
			TRUCK_SPEED = MAX_TRUCK_SPEED;
		}
		else if(TRUCK_SPEED < MIN_TRUCK_SPEED){
			TRUCK_SPEED = MIN_TRUCK_SPEED;
		}
		//System.out.println("Truck speed changed to: " + TRUCK_SPEED);
	}

	/**
	 * Image of default truck
	 */
	ImageIcon truckLeft = new ImageIcon("imageicons/truck/truckV3Image.png");

	/**
	 * Image of truck facing right
	 */
	ImageIcon truckRight = new ImageIcon("imageicons/truckRight.png");

	/**
	 * Trucks original location
	 */
	Point truckOriginalLoc;


	enum TruckState
	{
		LOADING, LEAVING, RETURNING
	};

	TruckState state;

	/**
	 * Trucks original location
	 */
	Point truckOrig;
	
	public void setLocation(int x, int y){//added by monroe
		super.setLocation(x,y);
		truckOrig.setLocation(getCenterLocation());
	}

	/** Public constructor for GUITruck */
	public GUITruck(Transducer t)
	{
		setIcon(truckLeft);
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
		state = TruckState.LOADING;
		truckOrig = new Point(this.getCenterX(), this.getCenterY());

		transducer = t;
		transducer.register(this, TChannel.TRUCK);
	}

	int counter = 0;
	/**
	 * Moves the glass pieces from the last conveyor onto the truck
	 */
	public void movePartIn()
	{
		synchronized( parts ) {
			for( MyGUIGlass part : parts ) {
				if ( !part.isLoaded ) {
					//Taken out to prevent the glass piece from retriggering the sensor as it is
					//loaded on to the truck.  It now moves directly downward.
/*					if (part.part.getCenterX() < getCenterX() )
						part.part.setCenterLocation(part.part.getCenterX() + 1, part.part.getCenterY());
					else if (part.part.getCenterX() > getCenterX() )
						part.part.setCenterLocation(part.part.getCenterX() - 1, part.part.getCenterY());
*/			
					if (part.part.getCenterY() < getCenterY())
						part.part.setCenterLocation(part.part.getCenterX(), part.part.getCenterY() + 1);
					else if (part.part.getCenterY() > getCenterY())
						part.part.setCenterLocation(part.part.getCenterX(), part.part.getCenterY() - 1);
			
					if (//part.part.getCenterX() == getCenterX() &&  //removed because only the y coordinate is changed
							part.part.getCenterY() == getCenterY())
					{
						part.isLoaded = true;
						//part.part.setVisible(false);//monroe moved to after the part is completely removed
						//part = null;//monroe moved to after the part is completely removed
						transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_GUI_LOAD_FINISHED, null);
					}
				}
			}
		}

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (state == TruckState.LOADING && !parts.isEmpty() )
		{
			movePartIn();
		}
		if (state == TruckState.LEAVING)
		{
			moveTruckOut();
		}
		if (state == TruckState.RETURNING)
		{
			moveTruckIn();
		}
	}

	private void moveTruckOut()
	{
		setCenterLocation(getCenterX() + TRUCK_SPEED, getCenterY());
		synchronized( parts ) {
			for( MyGUIGlass part : parts ) {
				part.part.setLocation((int)part.part.getLocation().getX() + TRUCK_SPEED, (int)part.part.getLocation().getY());//added by monroe to show part leaving on truck
			}
		}
		if (getCenterX() > (parent./*getParent().getGuiParent().*/getWidth() + this.getWidth()*3/4))//changed from < to > by monroe and added math to make truck actually leave the right panel
		{
			state = TruckState.RETURNING;
			//part.setVisible(false);//moved here by monroe
			//part = null;//moved here by monroe
		}
	}

	private void moveTruckIn()
	{
		setCenterLocation(getCenterX() - TRUCK_SPEED, getCenterY());//changed from -1 to -TRUCK_SPEED by monroe to speed backing up animation up
		if (getCenterX() < truckOrig.getX())//changed from > to < by monroe
		{
			setCenterLocation(((int)truckOrig.getX()), ((int)truckOrig.getY()));
			state = TruckState.LOADING;
			transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_GUI_EMPTY_FINISHED, null);
		}
	}

	@Override
	public void addPart(GUIGlass part)
	{
		parts.add( new MyGUIGlass( part ) );
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if (event == TEvent.TRUCK_DO_EMPTY)
		{
			state = TruckState.LEAVING;
		}
	}
}
