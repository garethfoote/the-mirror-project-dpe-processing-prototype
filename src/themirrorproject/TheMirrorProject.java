package themirrorproject;

import java.awt.BasicStroke;
import java.awt.Color;
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
import net.nexttext.property.Property;

public class TheMirrorProject extends PApplet {

	private Book book;
	private PFont fenix;
	private String sourceSentence = "I am the Rock";
	private String targetSentence = "I am the Seamus";
	private String sourceText = "Rock";
	private String targetText = "am";
	private TextObjectBuilder builder;
	private TextPage poem1;
	private TextPage poem2;
	private TextObjectGroup sourceWord;
	private TextObjectGroup targetWord;
	private Boolean applied = false;
	private double strMax = 0.06;
	private double strMin = 0.03;
	
//	private List<AbstractBehaviour> activeB = new ArrayList<AbstractBehaviour>();
//	private List<AbstractAction> activeA = new ArrayList<AbstractAction>();
			
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
		
		poem1 = book.addPage("poem1");
		builder.setParent(poem1.getTextRoot());
		
		// Do the building up front.
		TextObjectGroup sourceLine = builder.buildSentence(sourceSentence, 20, 200);
		TextObjectGroup targetLine = builder.buildSentence(targetSentence, width/2+20, 200);
		
		// Make propertyset hashmap.
		Map<String, Property> ps = new HashMap();
		for (String prop : sourceLine.getPropertyNames()) {
			ps.put(prop, sourceLine.getProperty(prop));
		}
		ps.put("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
		
		// Get target word.
		TextObjectIterator itrT = targetLine.iterator();
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(element.toString().equals(targetText)){
				targetWord = (TextObjectGroup)element;
			}
		}
		
		PVector targetPos = targetWord.getLocation();
		
		// Find source word and drop each glyph.
		TextObjectIterator itrS = sourceLine.iterator();
		while (itrS.hasNext()) {
			TextObject element = itrS.next();	
			if(element.toString().equals(sourceText)){
				TextObjectGroup el = (TextObjectGroup)element;
				TextObjectGlyphIterator glyphs = el.glyphIterator();
				while (glyphs.hasNext()) {
					// Drop glyphs and return action for chaining.
					TextObjectGlyph dropOutGlyph = glyphs.next();
					PVector dropOutLocation = dropOutGlyph.getLocation();
					float dropOutParentXPos = dropOutGlyph.getParent().getLocation().x;
					float dropOutGlyphXPos = dropOutLocation.x-dropOutParentXPos;
					float dropInStartYPos = 0-(height-dropOutLocation.y);
					AbstractAction outGlyphGravity = dropGlyphOut(dropOutGlyph);
					// Make Glyph with same string.
					// PositionY out of screen
					// PositionX at targetWord x pos + sourceWord glyph x pos
					TextObjectGlyph dropInGlyph = new TextObjectGlyph(dropOutGlyph.toString(), fenix, 28, ps, new PVector(targetPos.x+dropOutGlyphXPos, dropInStartYPos));
					poem1.getTextRoot().attachChild(dropInGlyph);
					
					dropGlyphIn(dropInGlyph, outGlyphGravity, targetPos.y);
				}
			}
		}
	}

	public AbstractAction dropGlyphIn(TextObject gl, AbstractAction outGlyphGravity, float yPosition){
		
		PVector glPos = gl.getLocation();
		
		Stop stop = new Stop();	
		Multiplexer chainStop = new Multiplexer();		
		chainStop.add(new Repeat(stop));
		chainStop.add(new MoveTo((int)(glPos.x), (int)yPosition));
		
		Condition condition = new HasReachedTarget(chainStop, outGlyphGravity, yPosition, false);
				
		Behaviour conditionBhvr = condition.makeBehaviour();
		book.addGlyphBehaviour(conditionBhvr);
		
		conditionBhvr.addObject(gl);
		moveBehaviour.addObject(gl);
		
		return condition;
	}
	
	public AbstractAction dropGlyphOut(TextObject gl){
			
		double rand = strMin + (strMax-strMin) * Math.random();
		
		// create and add the Gravity Behaviour
		AbstractAction gravity = new Gravity((float) rand);
		Behaviour gravityBehaviour = gravity.makeBehaviour();
		book.addGlyphBehaviour(gravityBehaviour);
					
		moveBehaviour.addObject(gl);
		gravityBehaviour.addObject(gl);
		
		return gravity;
	}
	
	public void draw() {
		
		background(255);

		// apply the behaviours to the text and draw it
		book.step();
		book.draw();
		
//		ActionResult ar = moveTo.behave(sourceWord);
//		println(ar.complete);
//		moveToBhvr.behaveAll();
		
	}

}
