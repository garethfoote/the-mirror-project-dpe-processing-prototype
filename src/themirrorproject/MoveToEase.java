package themirrorproject;

import processing.core.PApplet;
import processing.core.PVector;
import net.nexttext.Locatable;
import net.nexttext.PLocatableVector;
import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.PVectorProperty;

public class MoveToEase extends AbstractAction {

    protected Locatable target;
	int currentFrame;
	PVector originalPos;

    /**
     * Move a TextObject to a specified position.
     * @param target position to move to
	 * @param speed The speed of the approach represented as the number of
	 * pixels to move in each frame.  Use a very large number for instant
	 * travel.
     */
    public MoveToEase( PVector target, float speed ) {
    	this.target = new PLocatableVector(target);
        properties().init("Speed", new NumberProperty(speed));
    }
    
    public static float easeInOutCubic (float t,float b , float c, float d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	}

    public static float  easeInOutSine(float t,float b , float c, float d) {
		return -c/2 * ((float)Math.cos(Math.PI*t/d) - 1) + b;
	}

    public static float easeInOutQuintic(float t,float b , float c, float d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
		return c/2*((t-=2)*t*t*t*t + 2) + b;
	}

    /**
     * Add a vector to the position to bring it closer to the target.
     *
     * <p>Result is complete if it has reached its target. </p>
     */
    public ActionResult behave(TextObject to) {

    	// Px per frame.
        float speed = ((NumberProperty)properties().get("Speed")).get();
        if(originalPos == null){
        	originalPos = to.getPosition().get();
        }

        // Total distance
        PVector distance = target.getLocation();
        distance.sub(originalPos);
        
        // Total frames = distance/speed.
        if(properties().get("TotalFrames") == null){
        	float frames = (int)(distance.mag()/speed);
        	properties().init("TotalFrames", new NumberProperty(frames));
        }

        // Current frame / total frames.
        int t = (int)((NumberProperty)properties().get("TotalFrames")).get();
        // Factor on scale of 0-1.
        float factor = easeInOutSine(currentFrame, 0, 1, t);
        // Factor is on 0 to 1 scale so multiple this by distance.
        distance.mult(factor);
        // New position.
        distance.add(originalPos);

        ActionResult result = new ActionResult(false, true, false);
        
        PVectorProperty posProp = to.getPosition();
        posProp.set(distance);
        
        if(factor >= 1) {
        	posProp.set(target.getLocation().get());
            result.complete = true;
        }

        currentFrame++;

        return result;
    }
}
