package themirrorproject;

import processing.core.PApplet;

import net.nexttext.TextObject;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Action;

public class TrackerExtra extends AbstractAction {
	Action action;

    /**
     * Construct a Tracker for the given Action.
     */
    public TrackerExtra(Action action) {
        this.action = action;
    }

    /**
     * Pass the TextObject on to the contained Action, tracking the object.
     */
    public ActionResult behave(TextObject to) {

        textObjectData.put(to, null);

        ActionResult res = action.behave(to);
		PApplet.println(res.canComplete, res.complete);

        if (res.complete) {
            complete(to);
        }
        return res;
    }

    /**
     * Get the count of objects currently being processed by the action.
     */
    public int getCount() {
        return textObjectData.size();
    }

    /**
     * Determine if a specific object is being processed by the action.
     */
    public boolean isProcessing(TextObject to) {
        return textObjectData.containsKey(to);
    }
}
