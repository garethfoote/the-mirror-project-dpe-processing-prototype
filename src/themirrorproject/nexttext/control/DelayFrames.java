package themirrorproject.nexttext.control;

import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action;
import net.nexttext.property.NumberProperty;

public class DelayFrames extends AbstractAction {
    Action action;
    long timeLeft; // for handling clock discrepancies
    
    /**
     * Creates a Delay for the given action.
     * 
     * @param duration In seconds
     */
    public DelayFrames( Action action, float frames ) {
        this.action = action;
        properties().init("Duration", new NumberProperty(frames));
    }
    
    /**
     * Applies the delay.
     * 
     * <p>During the delay the ActionResult will set neither event nor
     * complete.  Once the delay is complete, the result of the delayed Action
     * will be returned.  </p>
     */
    public ActionResult behave(TextObject to) {
        
        // get the time elapsed for that object
        Long currentFrame = (Long)textObjectData.get(to);
        
        // create a map entry for new objects
        if ( currentFrame == null ) {
            textObjectData.put(to, ((NumberProperty)properties().get("Duration")).getLong());
        }
        
        if ( currentFrame <= 0 ) {
            ActionResult res = action.behave(to);
            if (res.complete) {
                complete(to);
            }
            return res;
        }        
        return new ActionResult(false, true, false);
    }
}
