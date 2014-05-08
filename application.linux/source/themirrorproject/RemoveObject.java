package themirrorproject;

import processing.core.PApplet;

import net.nexttext.Book;
import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;

public class RemoveObject extends AbstractAction {
	/**
     * Remove TextObject from book.
     */
    public RemoveObject() {

    }
    
    public ActionResult behave(TextObject col, TextObject to) {
    	
    	Book book = to.getBook();
    	book.removeObject(to);
    	PApplet.println("REMOVE OBJECT - collider", col.toString());
    	PApplet.println("REMOVE OBJECT - test against", to.toString());

        return new ActionResult(true, true, false);
    }

}
