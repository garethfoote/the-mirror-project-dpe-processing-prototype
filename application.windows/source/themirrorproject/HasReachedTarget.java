package themirrorproject;

import processing.core.PApplet;
import processing.core.PVector;

import net.nexttext.TextObject;
import net.nexttext.behaviour.Action;
import net.nexttext.behaviour.control.Condition;

public class HasReachedTarget extends Condition {

	private float yPos;
	
	/** 
     * Creates a new instance of HasReachedTarget
     *
     * @param trueAction the actions to perform if the TextObject is in the snake
     * @param falseAction the actions to perform if the TextObject is not in the snake
     */
    public HasReachedTarget(Action trueAction, Action falseAction, float yPos) {
        super(trueAction, falseAction);
        this.yPos = yPos;
    }
	
	@Override
	public boolean condition(TextObject to) {
		PVector v = to.getLocation();
		
//		PApplet.println(to, v.y, this.yPos, v.y>this.yPos);
		
		if(v.y > this.yPos-10)
			return true;
		
		return false;
	}

}
