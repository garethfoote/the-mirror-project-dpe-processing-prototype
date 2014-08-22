
import java.util.Map;

import processing.core.PApplet;

import net.nexttext.TextObject;
import net.nexttext.TextObjectGlyph;
import net.nexttext.TextObjectGlyphIterator;
import net.nexttext.TextObjectGroup;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.Property;

public class ApplyToGlyphDelay extends AbstractAction {
	
    private Action action;
    private int index;
    private Long startTime;
 
    public ApplyToGlyphDelay(Action descendantAction, float duration) {
        this.action = descendantAction;
        properties().init("Duration", new NumberProperty(1000*duration));
    }

    /**
     * Gets the set of properties required by the descendant Action.
     * @return Map containing the properties
     */
    public Map<String, Property> getRequiredProperties() 
    {
    	Map<String, Property> reqProps = super.getRequiredProperties();
    	reqProps.putAll(action.getRequiredProperties());
        return reqProps;
    }

    /**
     * Apply the given action to the TextObject's descendants.
     *
     * <p>The results of the action calls are combined using the method
     * described in Action.ActionResult.  </p>
     */
    public ActionResult behave(TextObject to) {
        if (to instanceof TextObjectGlyph) {
            return action.behave((TextObjectGlyph) to);
        } 
        else {

            ActionResult res = new ActionResult();
            TextObjectGlyphIterator i = ((TextObjectGroup)to).glyphIterator();            
            while (i.hasNext()) {                
            	TextObject gl = i.next();
            	index++;
            	// get duration property
            	long now = System.currentTimeMillis();
            	long duration = getDuration(gl, index);
            	if(startTime == null){
            		startTime = System.currentTimeMillis();
            	}

            	if ( now-startTime.longValue() >= duration ) {
                    ActionResult tres = action.behave(gl); 
                    if(tres.complete == true){
                    	// Individual glyph complete first event.
                    	// May come in handy.
//                    	tres.event = true;
                    }
                    res.combine(tres);
            	}        
            }
            /*
             * see the ActionResult class for details on how
             * ActionResults are combined.
             */
            res.endCombine();
            if (res.complete){
                action.complete(to);
                complete(to);
            }
            return res;
        }
    }

    /**
     * End this action for this object and end the passed in 
     * action for all its descendants.
     */
    public void complete(TextObject to) {
        super.complete(to);
        if (to instanceof TextObjectGlyph) {
            action.complete(to);
        }
        else{
            TextObjectGlyphIterator i = ((TextObjectGroup) to).glyphIterator();
            while (i.hasNext()) {
                TextObject next = i.next();
                action.complete(next);
            }
        }
    }
    
    
    public Long getDuration(TextObject to, int index){

        long duration = ((NumberProperty)properties().get("Duration")).getLong();

    	// get the time elapsed for that object
    	Long toDuration = (Long)textObjectData.get(to);
    
    	// create a map entry for new objects
    	if ( toDuration == null ) {
    		toDuration = new Long( duration*index );   
    		textObjectData.put(to, toDuration);
    	}
        
        return toDuration;
    	
    }

}
