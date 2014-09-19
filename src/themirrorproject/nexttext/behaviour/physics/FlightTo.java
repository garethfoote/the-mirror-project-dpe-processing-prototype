package themirrorproject.nexttext.behaviour.physics;

import processing.core.PApplet;
import processing.core.PVector;
import net.nexttext.FastMath;
import net.nexttext.Locatable;
import net.nexttext.PLocatableVector;
import net.nexttext.TextObject;
import net.nexttext.behaviour.Behaviour;
import net.nexttext.behaviour.TargetingAction;
import net.nexttext.behaviour.control.Condition;
import net.nexttext.behaviour.control.Multiplexer;
import net.nexttext.behaviour.control.Repeat;
import net.nexttext.behaviour.physics.Gravity;
import net.nexttext.behaviour.physics.PhysicsAction;
import net.nexttext.behaviour.physics.Stop;
import net.nexttext.behaviour.standard.MoveBy;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.PVectorProperty;

public class FlightTo extends PhysicsAction implements TargetingAction{

	private Locatable target;
	private int arcHeight;
	boolean arcRelative = true;
	private int arcPosition;
	private int arcVariant;
	private PVector difference;

    public FlightTo ( PVector difference, int arcHeight, int arcVariant, float strength ) {
    	this.arcHeight = arcHeight;
    	this.arcPosition = 300;
    	this.arcVariant = arcVariant;
        this.difference = difference;
        properties().init( "Strength", new NumberProperty(strength) );
    }

    public FlightTo ( Locatable target, int arcHeight, float strength ) {
        this.target = target;
    	this.arcHeight = arcHeight;
        properties().init( "Strength", new NumberProperty(strength) );
    }
    
    public PVector calculateLaunchVector(TextObject to, PVector source, PVector target, PVector sourceGlobal){

		int hDistance = (int)(target.x - source.x);
		int vDistance = (int)(source.y - target.y);

        int variant = (int) (Math.random()*this.arcVariant);
        int verticalUpDistance;
        int verticalDownDistance;
        if(arcRelative == true){
            verticalUpDistance = (int) arcHeight + variant;
            verticalDownDistance = (int) arcHeight + variant - vDistance;
        } else {
        	verticalUpDistance = (int) sourceGlobal.y - arcPosition;
        	verticalDownDistance = (int) (sourceGlobal.y+difference.y) - arcPosition;
        }
        
        // Calculate flight time and vectors for horizontal and vertical.
        float strength = ((NumberProperty)properties().get("Strength")).get();
        float flightTime = totalFlightTime(verticalUpDistance, verticalDownDistance, strength);
        float hv = horizontalVelocityToTravelDistance((int)hDistance, flightTime);
        float vv = verticalVelocityToReachHeight((int)verticalUpDistance);
        
        return new PVector(hv, 0-vv);
    }
    
    public static float calculateFlightTime(PVector from, PVector to, int arcHeight, float strength){
    	
        // Calculate flight time and vectors for horizontal and vertical.
        float flightTime = totalFlightTime(arcHeight, arcHeight - (int)(from.y - to.y), strength);
    	
    	return flightTime;
    }
    
    public ActionResult behave( TextObject to) {

    	int angle = 10;

        // Relative source.
        PVectorProperty sLocal = getPosition(to);
        // Set original position.
        PVectorProperty originalPosition = ((PVectorProperty)to.getProperty("OriginalPosition"));
        
        PVector tLocal;
		PVector launchVector = (PVector)textObjectData.get(to);
        // Launch vector calculated and original position set once per text object.
        if( launchVector == null){
            // Set original position once per text object.
        	originalPosition.set(sLocal.get());
        	originalPosition = sLocal;
        	// Calculate relative target.
        	tLocal = new PVector(originalPosition.getX(), originalPosition.getY());
        	tLocal.add(difference);
        	// Calculate throw vector.
        	PApplet.println( originalPosition.get(), tLocal);
        	launchVector = calculateLaunchVector(to, originalPosition.get(), tLocal, to.getLocation());
            textObjectData.put(to, launchVector);
            // Rotation. Apply only once.
            to.getRotation().set((0-angle)*FastMath.DEG_TO_RAD);
        } 
        tLocal = new PVector(originalPosition.getX(), originalPosition.getY());
        tLocal.add(difference);

        int targetArea = 10;
        // Has reached target?
		if(sLocal.getY() > tLocal.y-targetArea && sLocal.getY() < tLocal.y+targetArea
				&& sLocal.getX() > tLocal.x-targetArea && sLocal.getX() < tLocal.x+targetArea){
			
			// Stop at vector.
			sLocal.set(new PVector((int)tLocal.x, (int)tLocal.y));

			// Stop gravity.
	    	PVectorProperty velocity = getVelocity(to);
	        velocity.set(new PVector());
	        NumberProperty angVel = getAngularVelocity(to);
	        angVel.set(0);

	        // Rotation.
	        to.getRotation().set(0);

            return new ActionResult(true, true, false);
		}

        // Add launch vector.
        sLocal.add(launchVector);

		// Gravity.
    	PVector acc = new PVector(0, ((NumberProperty)properties().get("Strength")).get() );
        applyAcceleration(to, acc);

        return new ActionResult(false, true, false);
    }
    

	private float verticalVelocityToReachHeight(float h) {
        float g = ((NumberProperty)properties().get("Strength")).get();
	    // Same as equation to find velocity at point of impact for a body
	    // dropped from a given height.
	    // http://en.wikipedia.org/wiki/Equations_for_a_falling_body
	    return (float) Math.sqrt(2 * g * h);
	}

	public static float horizontalVelocityToTravelDistance(int d, float t) {
	    // Speed = distance / time, there is no acceleration horizontally
	    return d / t;
	}

	public static float timeToFallHeight(int h, float strength){
        float g = strength;
		// Just the time to travel a given distance from stationary under a
	    // constant acceleration (g).
	    // http://en.wikipedia.org/wiki/Equations_for_a_falling_body
		return (float) Math.sqrt((2 * h) / g);
	}

	public static float totalFlightTime(int upH, int downH, float strength) {
	    // Just add the times for the upwards and downwards journeys separately
	    return timeToFallHeight(upH, strength) + timeToFallHeight(downH, strength);
	}

    /**
     * Sets a target to approach.
     */
    public void setTarget(Locatable target) {
       	this.target = target;
    }

    /**
     * Sets a target to approach.
     */
    public void setTarget( float x, float y ) {
    	setTarget(x, y, 0);
    }
    
    /**
     * Sets a target to approach.
     */
    public void setTarget( float x, float y, float z ) {
    	setTarget(new PLocatableVector(x, y, z));
    }
    
    /**
     * Sets a target to approach.
     */
    public void setTarget( PVector target ) {
    	setTarget(new PLocatableVector(target));
    }	
}
