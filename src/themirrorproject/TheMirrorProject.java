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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.xpath.internal.operations.Bool;

import net.nexttext.Book;
import net.nexttext.FastMath;
import net.nexttext.PLocatableVector;
import net.nexttext.TextObject;
import net.nexttext.TextObjectBuilder;
import net.nexttext.TextObjectGlyph;
import net.nexttext.TextObjectGlyphIterator;
import net.nexttext.TextObjectGroup;
import net.nexttext.TextObjectIterator;
import net.nexttext.TextPage;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.Behaviour;
import net.nexttext.behaviour.control.ApplyToGlyph;
import net.nexttext.behaviour.control.Condition;
import net.nexttext.behaviour.control.Delay;
import net.nexttext.behaviour.control.Multiplexer;
import net.nexttext.behaviour.control.Repeat;
import net.nexttext.behaviour.physics.Gravity;
import net.nexttext.behaviour.physics.Move;
import net.nexttext.behaviour.physics.Stop;
import net.nexttext.behaviour.standard.DoNothing;
import net.nexttext.behaviour.standard.FadeTo;
import net.nexttext.behaviour.standard.MoveBy;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.ColorProperty;
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import themirrorproject.nexttext.behaviour.physics.FlightTo;
import themirrorproject.nexttext.control.ApplyToGlyphDelay;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;

public class TheMirrorProject extends PApplet {

	// DEBUG
	private TrackerExtra trackAction;
	// end DEBUG
	
	private Book book;
	private PFont fenix;
	private int arcHeight = 200;
	private int arcVariant = 10;
	private int angleVariant = 10;
	private double strMax = 0.03;
	private double strMin = 0.0;
	private float g = 0.081f;
	private int staggerDelay = 300;
	private int delayVariant = 150;
	private int fadeSpeed = 3;
	String test = "I am the sea . I hold the Land " + '\n' +
			"as one holds an apple in his hand." + '\n' +
			"Hold it fast with sleepless eyes.";
	String[] poemTwo = {"I am the sea. I hold the Land",
							"as one holds an apple in his hand.",
							"Hold it fast with sleepless eyes.", 
							"Watching the continents sink and rise.",
							"Out of my bosom the mountains grow,",
							"Back to its depths they crumble slow;"
							};
	String[] poemOne = {"pur means the place where", 
							"a goddess resides", 
							"stretch marks the sky in a color of war",
							"we pull on either end",
							"giant five-headed snake",
							"wrapped around us"
							};
	private TextObjectBuilder builder;
	private TextObjectGroup poem1Root;
	private TextObjectGroup poem2Root;
	private TextObjectGroup sourceWord;
	private TextObjectGroup targetWord;
	private int sourceWidth;
	private int targetWidth;
	private Rectangle targetBox;

	private Map<String,Property> ps = new HashMap<String,Property>();
	private List<TextObject> postTargetWords = new ArrayList<TextObject>();
			
	// create and add the Move Behaviour, required by all physics Actions
	AbstractAction move = new Move();
	Behaviour moveBehaviour = move.makeBehaviour();
	AbstractAction moveGlyph = new ApplyToGlyph(new Move());
	Behaviour moveGlyphBehaviour = moveGlyph.makeBehaviour();
	
	AbstractAction moveTo;
	Behaviour moveToBhvr;	
	
	ControlP5 cp;
	Button startButton;
	
	public void setup() {
		  
		// init the applet
		size(1200, 800);
		smooth();
		
		// init and set the font
		fenix = createFont("Fenix-Regular.ttf", 28, true);
		textFont(fenix);
		
		// set the text colour
	  	fill(0);
	  	noStroke();
	  	
	  	buildUI();
	  	textSize(32);
//	  	text("22", 200, 19+29+29+29+29+29);  // Specify a z-axis value
	  	
	  	prepareBook();
	  	buildText();

	}
	
	private void buildUI(){

		cp = new ControlP5(this);
		
		cp.addButton("resetText")
				.setCaptionLabel("Reset")
                .setPosition(0, 0)
                .setSize(200, 19);

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
			 .setValue(arcVariant)
		     .setRange(0.0f, 50.0f)
		     .setPosition(0, 19+29+29)
		     .setSize(200, 29);
		
/*		cp.addSlider("updateDelay")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Max variant in additional time delay (millisecond)")
			 .setValue(delayVariant)
		     .setRange(0, 300)
             .setPosition(0, 19+29+29+29)
		     .setSize(200, 29);*/

		cp.addSlider("updateAngleVariant")	
			 .setColorCaptionLabel(0)
			 .setCaptionLabel("Max variant in rotation from 12 o'clock (degrees)")
			 .setValue(angleVariant)
		     .setRange(0.0f, 15.0f)
             .setPosition(0, 19+29+29+29)
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
	
	private void prepareBook(){
		
	  	book = new Book(this);
	  	Book.bRemoveEmptyGroups = false;
	  	// Must be added for any physics action to work.
		book.addBehaviour(moveBehaviour);
		book.addBehaviour(moveGlyphBehaviour);
		
		builder = new TextObjectBuilder(book);
		builder.setFont(fenix, 28);
		builder.addGlyphProperty("StrokeColor", new ColorProperty(new Color(0,0,0,0)));

		TextPage poem1 = book.addPage("poem1");
		TextPage poem2 = book.addPage("poem2");
		poem1Root = poem1.getTextRoot();
		poem2Root = poem2.getTextRoot();
	}
	
	private void getTarget(TextObjectGroup poemRoot, String search){

		boolean found = false;
		targetBox = null;

		postTargetWords.clear();
		// Get target word and collect words to right of this.
		TextObjectIterator itrT = poemRoot.iterator();
		TextObjectGroup targetLine = null;
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(found == true
					&& element.getClass().getSimpleName().equals("TextObjectGroup")){

				if(element.toString().equals(targetLine.toString())){
					break;
				}
				postTargetWords.add(element);
			}
			
			if(!element.getClass().getSimpleName().equals("TextObjectGlyph")
				&& element.toString().equals(search)
				|| element.toString().equals(search+".")){
				targetWord = (TextObjectGroup)element;
				targetLine = targetWord.getParent();
				targetBox = targetWord.getBounds();
				targetWidth = targetWord.getBounds().width;
				book.getSpatialList().add(targetWord);
				found = true;
			}
		}	
	}
	
	private void getSource(TextObjectGroup poemRoot, String search){
		
		// Get source word TextObject from TextObjectGroup.
		TextObjectIterator itrS = poemRoot.iterator();
		while (itrS.hasNext()) {
			TextObject element = itrS.next();	
			if(element.toString().equals(search)){
				sourceWord = (TextObjectGroup)element;
			}
		}
		
		sourceWidth = sourceWord.getBounds().width;
	}

	private void buildText() {

		// Position poem2.
		poem2Root.getPosition().set(new PVector(width/2,0));
		
		int lineHeight = 40;

		// Build source sentence (left).
		builder.setParent(poem1Root);
		for (int i = 0; i < poemOne.length; i++) {
			builder.buildSentence(poemOne[i], 0, (lineHeight*i)+(height/2));
		}

		// Build target sentence (right).
		builder.setParent(poem2Root);
		for (int i = 0; i < poemTwo.length; i++) {
			builder.buildSentence(poemTwo[i], 0, (lineHeight*i)+(height/2));
		}
		
	}

	private void startThrow(){

		startButton.hide();

		getSource(poem1Root, "goddess");
		getTarget(poem2Root, "sea");

		frameRate(60);
		
		println("frameRate", frameRate);
		
		println("sourceWord", sourceWord);
		println("targetWord", targetWord);

		applySourceActions(0);
		applyTargetActions(0);
		applyTargetLineActions(0);

		getSource(poem2Root, "bosom");
		getTarget(poem1Root, "goddess");
		
		println("sourceWord", sourceWord);
		println("targetWord", targetWord);

		applySourceActions(8);
		applyTargetActions(8);
		applyTargetLineActions(8);

		getSource(poem1Root, "sky");
		getTarget(poem2Root, "bosom");
		
		println("sourceWord", sourceWord);
		println("targetWord", targetWord);

		applySourceActions(16);
		applyTargetActions(16);
		applyTargetLineActions(16);

		applyLastSourceActions(10);

	}

	private void applyLastSourceActions(int delaySeconds){
	
        float d = calculateMaxFlightTime()/frameRate;
        AbstractAction fadeTo = new FadeTo(255, 5, true, false);
        Delay flightDelay = new Delay(fadeTo, d);
        Delay pauseDelay = new Delay(flightDelay, delaySeconds);

        // Change letter opacity.
        TextObjectGlyphIterator sourcegi = ((TextObjectGroup)sourceWord).glyphIterator();
        // For some reason you cannot apply a ApplyToGlyph action to TextObjectGroup.
        // ...therefore must iterate through Glyphs.
        while (sourcegi.hasNext()) {
        	TextObjectGlyph gl = sourcegi.next();
            Behaviour fadeToBhvr = pauseDelay.makeBehaviour();
            fadeToBhvr.addObject(gl);
            book.addGlyphBehaviour(fadeToBhvr);
        }

	}

	private void applySourceActions(int delaySeconds){
		
		PVector targetPos = targetWord.getPositionAbsolute().get();
		PVector sourcePos = sourceWord.getPositionAbsolute().get();
		boolean leftToRight = targetPos.x > sourcePos.x;

		PVector diff = new PVector();
		diff.set(targetPos);
		diff.sub(sourcePos);
        TextObject dto = makeDuplicate(sourceWord, true);
		FlightTo flight = new FlightTo(diff, (int)arcHeight, arcVariant, (float)g);
		ApplyToGlyphDelay applyToGlyph = new ApplyToGlyphDelay(flight, (float)staggerDelay/1000, leftToRight);
		Delay delay = new Delay(applyToGlyph, delaySeconds);
		Behaviour flightBhvr = delay.makeBehaviour();
		
        TextObjectGlyphIterator togi = ((TextObjectGroup)dto).glyphIterator();
        // For some reason you cannot apply a ApplyToGlyph action to TextObjectGroup.
        // ...therefore must iterate through Glyphs.
        while (togi.hasNext()) {
        	TextObjectGlyph gl = togi.next();
        	moveBehaviour.addObject(gl);
        }
        book.addBehaviour(flightBhvr);
        flightBhvr.addObject(dto);

        // Change letter opacity.
        TextObjectGlyphIterator sourcegi = ((TextObjectGroup)sourceWord).glyphIterator();
        // For some reason you cannot apply a ApplyToGlyph action to TextObjectGroup.
        // ...therefore must iterate through Glyphs.
        while (sourcegi.hasNext()) {
        	TextObjectGlyph gl = sourcegi.next();
        	gl.getColor().set(new Color(0,0,0, 127));
        }


	}
	
//        	http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
//        	// Uniform distribution
//        	delay = delayMin + (delayMax-delayMin)*x;
//        	double u = Math.random();
//        	// Increase expoentially.
//        	double x = Math.log(1-u)/(-0.1);
//        	delay = delayMin + (delayMax-delayMin)*x;
//        	println(delay);

	private void applyTargetActions(int delaySeconds){
		
		PVector targetPos = targetWord.getPositionAbsolute().get();
		PVector sourcePos = sourceWord.getPositionAbsolute().get();
        int minFlightMillis = (int)((calculateMinFlightTime()/frameRate)*1000);
		boolean leftToRight = targetPos.x > sourcePos.x;

		TextObjectGlyphIterator glyphs = targetWord.glyphIterator();
		int j = (leftToRight == false) ? targetWord.getNumChildren() : 0;
        while (glyphs.hasNext()) {
        	j = (leftToRight == false) ? j-1 : j+1;
        	TextObject glyph = glyphs.next();
        	// Ensure the last glyph has started to fade at minFlightTime.
        	float d = (minFlightMillis-(j*staggerDelay))/1000.f;

        	AbstractAction fadeTo = new FadeTo(1, 5, true, false);
        	AbstractAction fadeToDelay = new Delay(fadeTo, d);
        	// Keeping these two delays separate for the moment.
        	Delay pauseDelay = new Delay(fadeToDelay, delaySeconds);
        	
        	Behaviour fadeToBhvr = pauseDelay.makeBehaviour();
        	fadeToBhvr.addObject(glyph);
        	book.addGlyphBehaviour(fadeToBhvr);
        }
	}

	private void applyTargetLineActions(int delaySeconds){
		
		PVector targetPos = targetWord.getPositionAbsolute().get();
		PVector sourcePos = sourceWord.getPositionAbsolute().get();
        float minFlight = calculateMinFlightTime();
		boolean leftToRight = targetPos.x > sourcePos.x;
		
		float speed = 0.5f; // pixels per frame
		float distance = (float)(sourceWidth-targetWidth);
		float time = Math.abs(distance)/speed; // frames

		// If right to left and space if reducing.
		if(leftToRight == false && distance < 0){
			// Wait until full minFlightTime
			time = 0-(127/5);
		}

        float d = (minFlight-time)/frameRate;

		// Shift words along to create space as letters land.
        for (TextObject to : postTargetWords) {
                // Move post target words along incrementally.
                PVector pos = to.getPosition().get();
                pos.add(distance, 0, 0);
                // Move at 0.5 px per frame.
        		AbstractAction moveAction = new MoveToEase(pos, speed);
        		AbstractAction moveDelay = new Delay(moveAction, d);
        		Delay pauseDelay = new Delay(moveDelay, delaySeconds);
        		Behaviour moveBhvr = pauseDelay.makeBehaviour();
                moveBhvr.addObject(to);
                book.addBehaviour(moveBhvr);
        }
		
	}

	private float calculateMinFlightTime(){
		
		PVector targetPos = targetWord.getPositionAbsolute().get();
		PVector sourcePos = sourceWord.getPositionAbsolute().get();

        return FlightTo.calculateFlightTime(sourcePos, targetPos, arcHeight, g);
	}
        
	private float calculateMaxFlightTime(){
		
		PVector targetPos = targetWord.getPositionAbsolute().get();
		PVector sourcePos = sourceWord.getPositionAbsolute().get();

		float maxFlightTime = FlightTo.calculateFlightTime(sourcePos, targetPos, arcHeight+arcVariant, g);
        maxFlightTime += frameRate*((staggerDelay*(sourceWord.getNumChildren()-1))/1000);

        return maxFlightTime;
	}

	private TextObject makeDuplicate(TextObject to, Boolean addToSpatialList){
        
		builder.setParent(poem1Root);
		TextObject duplicate = builder.buildSentence(to.toString(), to.getLocation(), Integer.MAX_VALUE);
		
		return duplicate;
	}

	public void draw() {
		
		background(255);
		
		// apply the behaviours to the text and draw it
		book.step();
		book.draw();

	}

	
    public static void main(String args[]){
    	PApplet.main(new String[] { themirrorproject.TheMirrorProject.class.getName() });
    }

}
