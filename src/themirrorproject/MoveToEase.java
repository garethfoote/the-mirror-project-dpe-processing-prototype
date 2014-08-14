package themirrorproject;

import processing.core.PApplet;
import processing.core.PVector;
import net.nexttext.PLocatableVector;
import net.nexttext.TextObject;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;

public class MoveToEase extends MoveTo {

	int currentFrame;
	PVector originalPos;
    /**
     * Move a TextObject to a specified position.
     * @param x x target position
     * @param y y target position
     */
    public MoveToEase(int x, int y) {
    	super(x,y);
    }

    /**
     * Move a TextObject to a specified position.
     * @param target position to move to
	 * @param speed The speed of the approach represented as the number of
	 * pixels to move in each frame.  Use a very large number for instant
	 * travel.
     */
    public MoveToEase( PVector target, long speed ) {
    	super(target, speed);
//        PApplet.println("target", target);
        properties().init("CurrentFrame", new NumberProperty(0));
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
        currentFrame++;

        float speed = ((NumberProperty)properties().get("Speed")).get();
        if(originalPos == null){
        	originalPos = to.getPosition().get();
        }

        // Total difference
        PVector newDir = target.getLocation();
        newDir.sub(originalPos);
        
        // Total frames.
        if(properties().get("TotalFrames") == null){
        	float frames = (int)(newDir.mag()/speed);
        	properties().init("TotalFrames", new NumberProperty(frames));
        }

        // Calculate unit position with ease and multiply by that factor.
        int t = (int)((NumberProperty)properties().get("TotalFrames")).get();
        float factor = easeInOutCubic(currentFrame, 0, 1, t);
        newDir.mult(factor);
        newDir.add(originalPos);

        ActionResult result = new ActionResult(false, true, false);
        
        PVectorProperty posProp = to.getPosition();
        posProp.set(newDir);
        
        if (newDir.mag()-target.getLocation().get().mag() <= 0) {
        	posProp.set(target.getLocation().get());
            result.complete = true;
        }

        return result;
    }
}
