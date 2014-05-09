
import java.util.Map;

import processing.core.PVector;

import net.nexttext.TextObject;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.behaviour.physics.Gravity;
import net.nexttext.property.NumberProperty;

public class ZeroGravity extends Gravity {

	  public ZeroGravity ( float strength ) {
		  super( 0 );
	  }
	  
	  public ActionResult behave( TextObject to) {
		  
		PVector acc = new PVector(0, ((NumberProperty)properties().get("Strength")).get() );
		applyAcceleration(to, acc);
		
		return new ActionResult(false, false, false);
	  }
}

