package themirrorproject;

import processing.core.PApplet;
import processing.core.PVector;

import net.nexttext.TextObject;
import net.nexttext.behaviour.Action;
import net.nexttext.behaviour.control.Condition;

public class HasReachedTarget extends Condition {

	private PVector target;
	
	/** 
     * Creates a new instance of HasReachedTarget
     *
     * @param trueAction the actions to perform if the TextObject is in the snake
     * @param falseAction the actions to perform if the TextObject is not in the snake
     */
    public HasReachedTarget(Action trueAction, Action falseAction, PVector target) {
        super(trueAction, falseAction);
        this.target = target;
    }
	
	@Override
	public boolean condition(TextObject to) {
		PVector v = to.getLocation();
		
//		PApplet.println(to, v.y, this.yPos, v.y>this.yPos);
		
		if(v.y > this.target.y-5 
			&& v.x > this.target.x-5)
			return true;
		
		return false;
	}

}
