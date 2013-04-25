
package transducer;

/**
 * Events that the transducer can fire
 */
public enum TEvent
{
	// global events
	START,
	STOP,
	SET_RECIPE,

	
	// conveyor
	CONVEYOR_DO_START,
	CONVEYOR_DO_STOP,


	// sensor
	SENSOR_GUI_PRESSED,
	SENSOR_GUI_RELEASED,

	// popup
	POPUP_DO_MOVE_UP,
	POPUP_GUI_MOVED_UP,
	POPUP_DO_MOVE_DOWN,
	POPUP_GUI_MOVED_DOWN,
	POPUP_RELEASE_GLASS,
	POPUP_GUI_LOAD_FINISHED,
	POPUP_GUI_RELEASE_FINISHED,

	// truck
	TRUCK_DO_LOAD_GLASS,
	TRUCK_GUI_LOAD_FINISHED,
	TRUCK_DO_EMPTY,
	TRUCK_GUI_EMPTY_FINISHED,

	// workstations
	WORKSTATION_DO_ACTION,
	WORKSTATION_RELEASE_GLASS,
	WORKSTATION_GUI_ACTION_FINISHED,
	WORKSTATION_DO_LOAD_GLASS,
	WORKSTATION_LOAD_FINISHED,
	WORKSTATION_RELEASE_FINISHED,
	
	//bin and "Glass Ghosts"
	BIN_CREATE_PART,
	BIN_PART_CREATED,
	BIN_CANNOT_CREATE,
	BIN_WAIT_PART,
	
	//shuttle
	SHUTTLE_FINISHED_LOADING,
	
	// non-norms	// ALL CHANNEL= CONTROL_PANEL
	CONVEYOR_JAM, // args[0] = conveyor number
	CONVEYOR_UNJAM,// args[0] = conveyor number
	POPUP_JAM,// args[0] = popup number (0-2)
	POPUP_UNJAM,// args[0] = popup number (0-2)
	
	// args[0] = workstation assembly number
	//		0 = drill
	//		1 = crossseamer
	//		2 = grinder
	// args[1] = workstation number (0 or 1)
	
	WORKSTATION_BREAK_GLASS, 
	WORKSTATION_DONT_BREAK_GLASS,
	WORKSTATION_BROKEN,
	WORKSTATION_WORKING,
}
