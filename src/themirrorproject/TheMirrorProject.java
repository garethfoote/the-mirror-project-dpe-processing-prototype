/*
 * 	  This file is part of The Mirror Project.
 * 
 *    Copyright (C) 2014 Gareth Foote and Mary Flanagan
 *
 *    The Mirror Project program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    The Mirror Project program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with The Mirror Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package themirrorproject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

import net.nexttext.Book;
import net.nexttext.FastMath;
import net.nexttext.TextObject;
import net.nexttext.TextObjectBuilder;
import net.nexttext.TextObjectGlyph;
import net.nexttext.TextObjectGlyphIterator;
import net.nexttext.TextObjectGroup;
import net.nexttext.TextObjectIterator;
import net.nexttext.TextPage;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Behaviour;
import net.nexttext.behaviour.control.Condition;
import net.nexttext.behaviour.control.Delay;
import net.nexttext.behaviour.control.Multiplexer;
import net.nexttext.behaviour.control.OnCollision;
import net.nexttext.behaviour.control.Repeat;
import net.nexttext.behaviour.physics.Gravity;
import net.nexttext.behaviour.physics.Move;
import net.nexttext.behaviour.physics.Push;
import net.nexttext.behaviour.physics.Spin;
import net.nexttext.behaviour.physics.Stop;
import net.nexttext.behaviour.standard.DoNothing;
import net.nexttext.behaviour.standard.FadeTo;
import net.nexttext.behaviour.standard.MoveBy;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.ColorProperty;
import net.nexttext.property.NumberProperty;
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;

public class TheMirrorProject extends PApplet {

	// DEBUG
	private TrackerExtra trackAction;
	// end DEBUG
	
	private Book book;
	private PFont fenix;
	private int arcHeight = 200;
	private int arcVariant = 50;
	private int angleVariant = 10;
	private double strMax = 0.03;
	private double strMin = 0.0;
	private float g = 0.081f;
	private int delay = 0;
	private int delayVariant = 150;
	private int fadeSpeed = 3;
	String[] targetPoem = {"I am the sea . I hold the Land",
							"as one holds an apple in his hand.",
							"Hold it fast with sleepless eyes.", 
							"Watching the continents sink and rise.",
							"Out of my bosom the mountains grow,",
							"Back to its depths they crumble slow;"
							};
	String[] sourcePoem = {"pur means the place where", 
							"a goddess resides", 
							"stretch marks the sky in a color of war",
							"we pull on either end",
							"giant five-headed snake",
							"wrapped around us"
							};
	private String targetText = "bosom";
	private String sourceText = "goddess";
	private String targetSentence = "Hold it fast with sleepless eyes";
	private String sourceSentence = "The preening water laps with the ambivalence of your look.";
	private TextObjectBuilder builder;
	private TextObjectGroup poem1Root;
	private TextObjectGroup poem2Root;
	private TextObjectGroup sourceWord;
	private TextObjectGroup targetWord;
	private int sourceWidth;
	private Rectangle targetBox;

	private Map<String,Property> ps = new HashMap<String,Property>();
	private List<TextObject> postTargetWords = new ArrayList<TextObject>();
			
	// create and add the Move Behaviour, required by all physics Actions
	AbstractAction move = new Move();
	Behaviour moveBehaviour = move.makeBehaviour();
	
	AbstractAction moveTo;
	Behaviour moveToBhvr;	
	
	ControlP5 cp;
	Button startButton;
	
	public void setup() {
		  
		// init the applet
		size(1200, 800);
//		size(1024, 768);
		smooth();
		
		// init and set the font
		fenix = createFont("Fenix-Regular.ttf", 28, true);
		textFont(fenix);
		
		// set the text colour
	  	fill(0);
	  	noStroke();
	  	
	  	prepareBook();
	  	buildText();
	  	
	  	buildUI();

	}
	
	public void buildUI(){

		cp = new ControlP5(this);
		
		cp.addButton("resetText")
				.setCaptionLabel("Reset")
                .setPosition(0, 0)
                .setSize(200, 19);

//		startButton = cp.addButton("startDrop")
		startButton = cp.addButton("startThrow")
				.setCaptionLabel("Start")
                .setPosition(0, 0)
                .setSize(200, 19);

		cp.addSlider("updateStrength")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Gravity strength")
			 .setValue((float)g)
		     .setRange((float)0.02, (float)0.2)
		     .setPosition(0, 19)
		     .setSize(200, 29);

		cp.addSlider("updateHeight")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Arc height (pixels)")
			 .setValue((float)arcHeight)
		     .setRange((float)0, (float)760)
		     .setPosition(0, 19+29)
		     .setSize(200, 29);

		cp.addSlider("updateHeightVariant")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Max arc height variant (pixels)")
			 .setValue((float)arcVariant)
		     .setRange((float)0, (float)arcVariant)
		     .setPosition(0, 19+29+29)
		     .setSize(200, 29);
		
		cp.addSlider("updateDelay")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Max variant in time delay (millisecond)")
			 .setValue(delayVariant)
		     .setRange(0, 300)
             .setPosition(0, 19+29+29+29)
		     .setSize(200, 29);

		cp.addSlider("updateAngleVariant")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Max variant in rotation from 12 o'clock (degrees)")
			 .setValue(angleVariant)
		     .setRange(0.0f, 15.0f)
             .setPosition(0, 19+29+29+29+29)
		     .setSize(200, 29);

		/*
		cp.addSlider("updateFadeSpeed")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Fade out speed")
			 .setValue((int)fadeSpeed)
		     .setRange(fadeSpeedMin, fadeSpeedMax)
		     .setPosition(0, 19+29+29+29+29)
		     .setSize(200, 29)
		     .setNumberOfTickMarks((fadeSpeedMax-fadeSpeedMin)+1)
		     .showTickMarks(true)
		     .snapToTickMarks(true);
		*/

	}
	
	public void updateFadeSpeed(ControlEvent ce){
		fadeSpeed = (int)ce.getValue();
	}

	public void updateDelay(ControlEvent ce){
		delayVariant = (int) ce.getValue();
	}

	public void updateHeight(ControlEvent ce){
		arcHeight = (int) ce.getValue();
	}

	public void updateHeightVariant(ControlEvent ce){
		arcVariant = (int) ce.getValue();
	}

	public void updateAngleVariant(ControlEvent ce){
		angleVariant = (int) ce.getValue();
	}

	public void updateStrengthVariant(ControlEvent ce){
		strMax = ce.getValue();
	}
	
	public void updateStrength(ControlEvent ce){
		g = ce.getValue();
	}
	
	public void resetText(){
		
		book.clear();
		book.removeQueuedObjects();
		book.removeAllGlyphBehaviours();
		book.removeAllGroupBehaviours();
		book.removeAllWordBehaviours();
		ps = new HashMap<String,Property>();
		postTargetWords = new ArrayList<TextObject>();

	  	buildText();
	  	startButton.show();
		
	}
	
	public void prepareBook(){
		
	  	book = new Book(this);
	  	book.bRemoveEmptyGroups = false;
	  	// Must be added for any physics action to work.
		book.addGlyphBehaviour(moveBehaviour);
		
		builder = new TextObjectBuilder(book);
		builder.setFont(fenix, 28);
		builder.addGlyphProperty("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
		builder.setAddToSpatialList(true);

		TextPage poem1 = book.addPage("poem1");
		TextPage poem2 = book.addPage("poem2");
		poem1Root = poem1.getTextRoot();
		poem2Root = poem2.getTextRoot();
	
	}
	
	public void buildText() {
		  
		TextObjectGroup sourceLine = null;
		TextObjectGroup targetLine = null;

		// Position poem2.
		poem2Root.getPosition().set(new PVector(width/2,0));
		
		int lineHeight = 40;

		// Build source sentence (left).
		builder.setParent(poem1Root);
		for (int i = 0; i < sourcePoem.length; i++) {
			if(sourcePoem[i].contains(sourceText)){
				// Capture source line.
				sourceLine = builder.buildSentence(sourcePoem[i], 0, (lineHeight*i)+(height/2));
			} else {
				builder.buildSentence(sourcePoem[i], 0, (lineHeight*i)+(height/2));
			}
		}

		// Build target sentence (right).
		builder.setParent(poem2Root);
		for (int i = 0; i < targetPoem.length; i++) {
			if(targetPoem[i].contains(targetText)){
				// Capture target line.
				targetLine = builder.buildSentence(targetPoem[i], 0, (lineHeight*i)+(height/2));
			} else {
				builder.buildSentence(targetPoem[i], 0, (lineHeight*i)+(height/2));
			}
		}
		
		// Get source word TextObject from TextObjectGroup.
		TextObjectIterator itrS = sourceLine.iterator();
		while (itrS.hasNext()) {
			TextObject element = itrS.next();	
			if(element.toString().equals(sourceText)){
				sourceWord = (TextObjectGroup)element;
			}
		}

		// Create property set from TextObject.
		Set<String> pNames = sourceLine.getPropertyNames();
		for (String propName: pNames) {
			ps.put(propName, sourceLine.getProperty(propName));
		}
		ps.put("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
    	
		targetBox = null;
		sourceWidth = sourceWord.getBounds().width+sourceWord.getRightSibling().getBounds().width;
		boolean found = false;

		// Get target word and collect words to right of this.
		TextObjectIterator itrT = targetLine.iterator();
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(found == true
					&& !element.toString().equals(targetLine.toString())
					&& element.getClass().getSimpleName().equals("TextObjectGroup")){

				postTargetWords.add(element);
				// TODO - Perhaps add this to all TextObjects.
				moveBehaviour.addObject(element);
			}

			if(element.toString().equals(targetText)){
				targetWord = (TextObjectGroup)element;
				targetBox = targetWord.getBounds();

				OnCollision col = new OnCollision(new RemoveObject());
				Behaviour colBhvr = col.makeBehaviour();
				colBhvr.addObject(element);
				book.addBehaviour(colBhvr);
				found = true;
			}
		}
		
//		startThrow();
	}
	

	public void startThrow(){

		startButton.hide();

		int delayMin = 0;
		int delayMax = delayVariant;
		int delay = 0;

		int angleMin = 0;
		int angleMax = angleVariant;
		int angle = 0;

		PVector targetWordPos = targetWord.getPositionAbsolute().get();
		PVector sourceWordPos = sourceWord.getPositionAbsolute().get();
		float horizontalDistance = targetWordPos.x - sourceWordPos.x;

		TextObjectGlyphIterator glyphs = sourceWord.glyphIterator();
		int j = 0;
        while (glyphs.hasNext()) {

        	int variant = (int) (Math.random()*arcVariant);
        	int verticalUpDistance = (int) arcHeight + variant;
        	int verticalDownDistance = (int) arcHeight + variant - (int)(sourceWordPos.y-targetWordPos.y);

//        	http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
//        	// Uniform distribution
//        	delay = delayMin + (delayMax-delayMin)*x;
//        	double u = Math.random();
//        	// Increase expoentially.
//        	double x = Math.log(1-u)/(-0.1);
//        	delay = delayMin + (delayMax-delayMin)*x;
//        	println(delay);

        	delay = delayMin + (int)((delayMax-delayMin)*Math.random());
        	angle = angleMin + (int)((angleMax-angleMin)*Math.random());

        	j++;
        	TextObject originalGlyph = glyphs.next();
        	TextObject glyph = makeDuplicate(originalGlyph, true);
        	PVectorProperty gLocal = originalGlyph.getPosition();
        	
        	originalGlyph.getColor().set(new Color(0,0,0,127));

        	float hv = horizontalVelocityToTravelDistance((int)horizontalDistance, totalFlightTime(verticalUpDistance, verticalDownDistance));
        	float vv = verticalVelocityToReachHeight((int)verticalUpDistance);
        	PVector pv = new PVector(hv, 0-vv);
        	
        	float d = ((j*300+delay)/1000.0f);
    		Multiplexer throwActions = new Multiplexer();		
        	AbstractAction gravity = new Delay(new Gravity(g), d);
        	AbstractAction push = new Delay(new MoveBy(pv), d);
        	AbstractAction rotate = new Delay(new Rotate((0-angle)*FastMath.DEG_TO_RAD), d);

    		throwActions.add(gravity);
        	throwActions.add(push);
        	throwActions.add(rotate);

    		Multiplexer stopActions = new Multiplexer();		
    		Stop stop = new Stop();
    		stopActions.add(new Repeat(stop));
    		stopActions.add(new MoveTo((int)(gLocal.getX()+targetWordPos.x), (int)targetWordPos.y));
    		stopActions.add(new Rotate(0));
    		Condition condition = new HasReachedTarget(stopActions, throwActions, targetWordPos.y);
    		// Tracker works out if object is being processed by action.
    		trackAction = new TrackerExtra(condition);
    		Behaviour topBehaviour = trackAction.makeBehaviour();
    		
    		shiftTargetLine(trackAction);

    		book.addGlyphBehaviour(topBehaviour);
    		topBehaviour.addObject(glyph);
    		moveBehaviour.addObject(glyph);
        }
		
	}

	public float verticalVelocityToReachHeight(float h) {
	    // Same as equation to find velocity at point of impact for a body
	    // dropped from a given height.
	    // http://en.wikipedia.org/wiki/Equations_for_a_falling_body
	    return (float) Math.sqrt(2 * g * h);
	}

	
	public float horizontalVelocityToTravelDistance(int d, float t) {
	    // Speed = distance / time, there is no acceleration horizontally
	    return d / t;
	}

	public float timeToFallHeight(int h){
		// Just the time to travel a given distance from stationary under a
	    // constant acceleration (g).
	    // http://en.wikipedia.org/wiki/Equations_for_a_falling_body
		return (float) Math.sqrt((2 * h) / g);
	}

	public float totalFlightTime(int upH, int downH) {
	    // Just add the times for the upwards and downwards journeys separately
	    return timeToFallHeight(upH) + timeToFallHeight(downH);
	}
	
	public TextObject makeDuplicate(TextObject to, Boolean addToSpatialList){
        
		TextObject duplicate = new TextObjectGlyph(to.toString(), fenix, 28, ps, to.getLocation());
        poem1Root.attachChild(duplicate);
        if(addToSpatialList == true){
        	book.getSpatialList().add(duplicate);
        }
		
		return duplicate;
		
	}
	
	public void shiftTargetLine(TrackerExtra tracker){

        // Apply shift to words right of target.
		float incrementX = (sourceWidth-targetBox.width)/sourceText.length();
        for (TextObject to : postTargetWords) {
        	// TODO - (??) Maybe add delay based on sentence index.
        	// Move post target words along incrementally.
            AbstractAction shiftPostTargetWord = new MoveToRelative(incrementX, 0);
            // The HasReachedTarget condition has stopped processing.
            HasStoppedProcessing hsp = new HasStoppedProcessing(shiftPostTargetWord, new DoNothing(), tracker);
            Behaviour hspBhvr = hsp.makeBehaviour();
            hspBhvr.addObject(to);
            book.addBehaviour(hspBhvr);
        }

	}

	public void startDrop(){
		
		startButton.hide();
		
		// TODO - (??) Possibly count how many collisions with post target 
		//        glyphs and then increment only on those drops.

		// Do the business. Drop out, then queue drop in and shift words.
		int sourceLength = sourceText.length();
		PVector tPos = targetWord.getPosition().get();
		TextObjectGlyphIterator glyphs = sourceWord.glyphIterator();
		int j = 0;
        while (glyphs.hasNext()) {
        	TextObject glyph = glyphs.next();
        	PVectorProperty gLocal = glyph.getPosition();
        	PVector gPos = new PVector(gLocal.getX()+tPos.x, sourceWord.getLocation().y-height);
        	// Duplicate glyph and position in same place.
        	TextObjectGlyph toDropOut = new TextObjectGlyph(glyph.toString(), fenix, 28, ps, glyph.getLocation());
        	poem1Root.attachChild(toDropOut);
        	// Duplicate glyph again and position above target.
        	TextObjectGlyph toDropIn = new TextObjectGlyph(glyph.toString(), fenix, 28, ps, gPos);
        	poem2Root.attachChild(toDropIn);
        	book.getSpatialList().add(toDropIn);
        	// Pass gravity action to this new glyph from drop out.
        	AbstractAction doA = dropGlyphOut(toDropOut, j);
        	// Change opacity or original.
        	glyph.getColor().set(new Color(0,0,0,127));
        	// Returns condition action for glyph drop which is a condition for shifting words right.
        	TrackerExtra dropAction = (TrackerExtra)dropGlyphIn(toDropIn, doA, targetWord.getLocation());

			// Apply shift to words right of target.
        	float incrementX = (sourceWidth-targetBox.width)/sourceLength;
			for (TextObject to : postTargetWords) {
				// TODO - (??) Maybe add delay based on sentence index.
				// Move post target words along incrementally.
				// AbstractAction shiftPostTargetWord = new MoveTo((int)(pos.x+(incrementX*(j+1))), (int)pos.y, 3);
				AbstractAction shiftPostTargetWord = new MoveToRelative(incrementX, 0);
				// The HasReachedTarget condition has stopped processing.
				HasStoppedProcessing hsp = new HasStoppedProcessing(shiftPostTargetWord, new DoNothing(), dropAction);
				Behaviour hspBhvr = hsp.makeBehaviour();
				hspBhvr.addObject(to);
				book.addBehaviour(hspBhvr);
        	}

        	j++;
        }
		
	}
	
	public AbstractAction dropGlyphIn(TextObject gl, AbstractAction dropInAction, PVector targetPos){
		
		PVector glPos = gl.getLocation();

		Multiplexer stopActions = new Multiplexer();		
		Stop stop = new Stop();
		stopActions.add(new Repeat(stop));
		stopActions.add(new MoveTo((int)(glPos.x), (int)targetPos.y));
		
		Condition condition = new HasReachedTarget(stopActions, dropInAction, targetPos.y);
        trackAction = new TrackerExtra(condition);

        // Combined condition, multiplexer, etc.
		Behaviour topBhvr = trackAction.makeBehaviour();
		book.addGlyphBehaviour(topBhvr);
		
		topBhvr.addObject(gl);
		moveBehaviour.addObject(gl);

		return trackAction;
	}
	
	public AbstractAction dropGlyphOut(TextObject gl, int index){
			
		double rand = strMin + (strMax-strMin) * Math.random();

		AbstractAction gravityNoFade = new Delay(new Gravity((float)g+(float)rand), (float)delay*index);
		AbstractAction gravity = new Delay(new GravityFadeTo((float)g+(float)rand, 0, fadeSpeed, true, false), (float)delay*index);

		Behaviour gravityBehaviour = gravity.makeBehaviour();
		book.addGlyphBehaviour(gravityBehaviour);
		gravityBehaviour.addObject(gl);

		moveBehaviour.addObject(gl);
		
		return gravityNoFade;
	}

	public void draw() {
		
		background(255);
		
		// apply the behaviours to the text and draw it
		book.step();
		book.draw();

//		if(trackAction != null && trackAction.getCount() > 0){
//			println("COUNT>>",trackAction.getCount());
//		}
	}
	
    public static void main(String args[]){
    	PApplet.main(new String[] { themirrorproject.TheMirrorProject.class.getName() });
    }

}
