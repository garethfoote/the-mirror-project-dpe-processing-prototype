package themirrorproject;

import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action;
import net.nexttext.behaviour.control.Condition;

public class HasStoppedProcessing extends Condition {

	private TrackerExtra watchAction;
	
	/** 
     * Creates a new instance of HasStoppedProcessing
     */
    public HasStoppedProcessing(Action trueAction, Action falseAction, TrackerExtra watchAction) {
        super(trueAction, falseAction);
        this.watchAction = watchAction;
    }
	
	@Override
	public boolean condition(TextObject to) {
		
		// No processes so call true action.
		if(watchAction.getCount() == 0)
			return true;
		
		return false;
	}

}
