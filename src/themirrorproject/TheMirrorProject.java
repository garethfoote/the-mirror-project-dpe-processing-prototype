package themirrorproject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import processing.opengl.*;

import net.nexttext.Book;
import net.nexttext.TextObject;
import net.nexttext.TextObjectBuilder;
import net.nexttext.TextObjectGlyph;
import net.nexttext.TextObjectGlyphIterator;
import net.nexttext.TextObjectGroup;
import net.nexttext.TextObjectIterator;
import net.nexttext.TextPage;
import net.nexttext.behaviour.AbstractAction;
import net.nexttext.behaviour.AbstractBehaviour;
import net.nexttext.behaviour.Action;
import net.nexttext.behaviour.Behaviour;
import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.behaviour.control.Chain;
import net.nexttext.behaviour.control.Condition;
import net.nexttext.behaviour.control.Multiplexer;
import net.nexttext.behaviour.control.Repeat;
import net.nexttext.behaviour.physics.Approach;
import net.nexttext.behaviour.physics.Gravity;
import net.nexttext.behaviour.physics.Move;
import net.nexttext.behaviour.physics.Push;
import net.nexttext.behaviour.physics.Stop;
import net.nexttext.behaviour.standard.DoNothing;
import net.nexttext.behaviour.standard.MoveBy;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.ColorProperty;
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;

public class TheMirrorProject extends PApplet {

	// DEBUG
	private TrackerExtra trackAction;
	// end DEBUG

	private Book book;
	private PFont fenix;
	private String sourceSentence = "I am the tremendous Rock";
	private String targetSentence = "I am the Sea";
	private String sourceText = "tremendous";
	private String targetText = "am";
	private TextObjectBuilder builder;
	private TextObjectGroup poem1Root;
	private TextObjectGroup poem2Root;
	private TextObjectGroup sourceWord;
	private TextObjectGroup targetWord;
	private Boolean applied = false;
	private double strMax = 0.03;
	private double strMin = 0.01;
	private Map<String,Property> ps = new HashMap<String,Property>();
	
	private List<TextObject> postTargetWords = new ArrayList<TextObject>();
			
	// create and add the Move Behaviour, required by all physics Actions
	AbstractAction move = new Move();
	Behaviour moveBehaviour = move.makeBehaviour();
	
	AbstractAction moveTo;
	Behaviour moveToBhvr;	
	
	public void setup() {
		  
		// init the applet
		size(640, 360);
		smooth();
		
		// init and set the font
		fenix = createFont("Fenix-Regular.ttf", 28, true);
		textFont(fenix);
		
		// set the text colour
	  	fill(0);
	  	noStroke();

	  	book = new Book(this);
	  	// Must be added for any physics action to work.
		book.addGlyphBehaviour(moveBehaviour);
		
		builder = new TextObjectBuilder(book);
		builder.setFont(fenix, 28);
		builder.addGlyphProperty("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
		
		TextPage poem1 = book.addPage("poem1");;
		TextPage poem2 = book.addPage("poem2");;
		
		poem1Root = poem1.getTextRoot();
		poem2Root = poem2.getTextRoot();
		// Position poem2.
		poem2Root.getPosition().set(new PVector(width/2,0));
		
		// Do the building up front.
		builder.setParent(poem1Root);
		TextObjectGroup sourceLine = builder.buildSentence(sourceSentence, 0, 200);

		builder.setParent(poem2Root);
		TextObjectGroup targetLine = builder.buildSentence(targetSentence, 0, 200);
		
		// Create property set from TextObject.
		Set<String> pNames = sourceLine.getPropertyNames();
		for (String propName: pNames) {
			ps.put(propName, sourceLine.getProperty(propName));
		}
		ps.put("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
		
		TextObjectIterator itrS = sourceLine.iterator();
		while (itrS.hasNext()) {
			TextObject element = itrS.next();	
			if(element.toString().equals(sourceText)){
				sourceWord = (TextObjectGroup)element;
			}
		}
    	
		Rectangle targetBox = null;
		Rectangle sourceBox = sourceWord.getBounds();
		boolean found = false;

		// Get target word and collect words to right of this.
		TextObjectIterator itrT = targetLine.iterator();
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(found == true
					&& !element.toString().equals(targetSentence)
					&& element.getClass().getSimpleName().equals("TextObjectGroup")){

				postTargetWords.add(element);
				// TODO - Perhaps add this to all TextObjects.
				moveBehaviour.addObject(element);
			}

			if(element.toString().equals(targetText)){
				targetWord = (TextObjectGroup)element;
				targetBox = targetWord.getBounds();
				found = true;
			}
		}
		
		// Alternate move action with a bit of drag.
//		Behaviour moveDragBehaviour = (new Move((float)0.25,0)).makeBehaviour();
//		book.addBehaviour(moveDragBehaviour);
//		for (TextObject to : postTargetWords) {
//			moveDragBehaviour.addObject(to);
//		}

		int sourceLength = sourceText.length();
		println("Space offset", book.getSpaceOffset());
        
		// Do the business. Drop out, then queue drop in and shift words.
		PVector tPos = targetWord.getPosition().get();
		TextObjectGlyphIterator glyphs = sourceWord.glyphIterator();
		int j = 0;
        while (glyphs.hasNext()) {
        	TextObject glyph = glyphs.next();
        	PVectorProperty gLocal = glyph.getPosition();
        	PVector gPos = new PVector(gLocal.getX()+tPos.x, sourceWord.getLocation().y-height);

        	// Make text object glyph and position above target.
        	TextObjectGlyph toG = new TextObjectGlyph(glyph.toString(), fenix, 28, ps, gPos);
        	// Pass gravity action to this new glyph from drop out.
        	AbstractAction doA = dropGlyphOut(glyph, sourceLength-j);
        	poem2Root.attachChild(toG);

        	// Returns condition for glyph drop.
        	TrackerExtra dropAction = (TrackerExtra)dropGlyphIn(toG, doA, targetWord.getLocation());

        	float incrementX = (sourceBox.width-targetBox.width)/sourceLength;
			// Apply shift to words right of target.
			for (TextObject to : postTargetWords) {
				TextObjectGroup postTargetWord = (TextObjectGroup)to;
				PVector pos = postTargetWord.getLocation();

				// TODO - Maybe add delay based on sentence index.
				println("Increment by", incrementX*(j+1));
				AbstractAction shiftPostTargetWord = new MoveTo((int)(pos.x+(incrementX*(j+1)))+book.getSpaceOffset(), (int)pos.y, 3);
//				AbstractAction shiftPostTargetWord = new Push((float)(pos.x+(incrementX*(j+1))), 0, (float)2);
				HasStoppedProcessing hsp = new HasStoppedProcessing(shiftPostTargetWord, new DoNothing(), dropAction);
				Behaviour hspBhvr = hsp.makeBehaviour();
				hspBhvr.addObject(to);

				book.addBehaviour(hspBhvr);

//				Behaviour shiftBhvr = shiftPostTargetWord.makeBehaviour();
//				shiftBhvr.addObject(postTargetWord);

				// Add to book...
//				book.addBehaviour(shiftBhvr);
				// and collect.
//				postTargetShiftActions.add(shiftPostTargetWord);

//        		shift.add(postTargetShiftActions.get(k+(j*sourceLength)));
//				book.addBehaviour(postTargetShiftActions.get(k+(j*sourceLength)).makeBehaviour());

        	}

//			shiftBhvr.addObject(to);
//        	book.addBehaviour(shiftBhvr);

        	j++;
        }

	}
	
	public void shiftTargetLineObject(TextObject to, AbstractAction aa, int shiftX){
		
		
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
		
		// create and add the Gravity Behaviour
		AbstractAction gravity = new Gravity((float)(0.03*index+rand));
		Behaviour gravityBehaviour = gravity.makeBehaviour();
		book.addGlyphBehaviour(gravityBehaviour);
					
		moveBehaviour.addObject(gl);
		gravityBehaviour.addObject(gl);
		
		return gravity;
	}

	
	
	public void draw() {
		
		background(255);

	/*	TextObjectGlyphIterator itr = line.glyphIterator();
		while (itr.hasNext()) {
			TextObject el = itr.next();
			PVector v = el.getLocation();
			if(applied == false && v.y > 300){
				
				for(AbstractAction a: activeA){
					println(a);
					a.complete(el);
				}
				move.complete(el);
				
				for(AbstractBehaviour b: activeB){
					book.removeGlyphBehaviour(b);
				}
				book.removeGlyphBehaviour(moveBehaviour);
				applied = true;
								
				AbstractAction stop = new Stop();
				Behaviour stopBehaviour = stop.makeBehaviour();
				book.addGlyphBehaviour(stopBehaviour);
				
				stopBehaviour.addObject(element);
			}
			
		}*/
		
		// apply the behaviours to the text and draw it
		book.step();
		book.draw();

		if(trackAction.getCount() > 0){
			println("COUNT>>",trackAction.getCount());
		}
	}

}
