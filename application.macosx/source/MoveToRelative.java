
import processing.core.PVector;
import net.nexttext.PLocatableVector;
import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.PVectorProperty;

/**
 * Move an object to the location.
 */
/* $Id$ */
public class MoveToRelative extends AbstractAction {

    protected PVectorProperty offset;
    protected PLocatableVector target;

    public MoveToRelative(float x, float y, long speed) {
    	this(new PVector(x, y), speed);
    }

    public MoveToRelative(float x, float y) {
    	this(new PVector(x, y), Long.MAX_VALUE);
    }

    /**
     * Move a TextObject to a specified position relative to current.
     * @param target position to move to
     */
    public MoveToRelative( PVector offset, long speed) {
        this.offset = new PVectorProperty(offset);
        properties().init("Speed", new NumberProperty(speed));
    }
    
    /**
     * Add a vector to the position on the first instance of behave
     * thus making the offset relative to the current position.
     *
     * <p>Result is complete if it has reached its target. </p>
     */
    public ActionResult behave(TextObject to) {
        float speed = ((NumberProperty)properties().get("Speed")).get();

    	PVectorProperty posProp = getPosition(to);

        if(this.target == null){
        	PVector targetPos = posProp.get();
        	targetPos.add(offset.get());
        	target = new PLocatableVector(targetPos);
        }

        PVector newDir = target.getLocation();
 	 	newDir.sub(posProp.get());

        ActionResult result = new ActionResult(true, true, false);

	 	// Scale the vector down to the speed if needed.
        if (newDir.mag() > speed) {
            newDir.normalize();
            newDir.mult(speed);
            result.complete = false;
        }

       	posProp.add(newDir);
        return result;
    }

}
