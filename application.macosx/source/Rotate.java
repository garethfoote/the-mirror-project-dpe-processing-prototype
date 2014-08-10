import processing.core.PVector;
import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.property.PVectorProperty;

public class Rotate extends AbstractAction {
	
	protected float radians = 0;

    public Rotate(int rads) {
    	this.radians = rads;
    }

    public Rotate(float rads) {
    	this.radians = rads;
    }

    public ActionResult behave(TextObject to) {

    	to.getRotation().set(this.radians);

        return new ActionResult(false, false, false);
    }
}
