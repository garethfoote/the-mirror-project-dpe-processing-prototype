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

public class TheMirrorProject extends PApplet {

	private Book book;
	private PFont fenix;
	private String sourceSentence = "I am the Rock";
	private String targetSentence = "I am the Sea";
	private String sourceText = "Rock";
	private String targetText = "Sea";
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
		sourceWord = builder.buildSentence(sourceText, width/2+20, 50);
		TextObjectGroup targetLine = builder.buildSentence(targetSentence, width/2+20, 200);
		
		TextObjectIterator itrS = sourceLine.iterator();
		while (itrS.hasNext()) {
			TextObject element = itrS.next();	
			if(element.toString().equals(sourceText)){
				TextObjectGroup el = (TextObjectGroup)element;
				TextObjectGlyphIterator glyphs = el.glyphIterator();
				while (glyphs.hasNext()) {
					AbstractAction doA = dropGlyphOut(glyphs.next());
					// TODO - find equivalent sourceWord glyph.
					
				}
			}
		}
    	
		// Get target word.
		TextObjectIterator itrT = targetLine.iterator();
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(element.toString().equals(targetText)){
				targetWord = (TextObjectGroup)element;
			}
		}
		
		dropIn(sourceWord, targetWord);
		
	}
	
	public void dropIn(TextObjectGroup sourceWord, TextObject targetWord){
		
		TextObjectGlyphIterator glyphs = sourceWord.glyphIterator();
		PVector targetPos = targetWord.getLocation();
		
		int offset = (int)(targetPos.x-sourceWord.getLocation().x);
		
		while (glyphs.hasNext()) {
			
			TextObject gl = glyphs.next();
			dropGlyphIn(gl);

		}
	}
	
	public void dropInGlyph(TextObject gl){
		
		PVector glPos = gl.getLocation();

		MoveTo moveInstant = new MoveTo((int)(glPos.x+(offset)), 0);
		Gravity dropIn = new Gravity((float)0.05);
		Stop stop = new Stop();
		
		Chain chainMove = new Chain();		
		chainMove.add(moveInstant);
		chainMove.add(dropIn);
		
		Multiplexer chainStop = new Multiplexer();		
		chainStop.add(new Repeat(stop));
		chainStop.add(new MoveTo((int)(glPos.x+(offset)), (int)targetPos.y));
		
		Condition condition = new HasReachedTarget(chainStop, chainMove, targetPos.y);
				
		Behaviour topBhvr = condition.makeBehaviour();
		book.addGlyphBehaviour(topBhvr);
		
		topBhvr.addObject(gl);
		moveBehaviour.addObject(gl);
		
	}
	
	public AbstractAction dropGlyphOut(TextObject gl){
			
		double rand = strMin + (strMax-strMin) * Math.random();
		
		// create and add the Gravity Behaviour
		AbstractAction gravity = new Gravity((float) rand);
		Behaviour gravityBehaviour = gravity.makeBehaviour();
		book.addGlyphBehaviour(gravityBehaviour);
					
		moveBehaviour.addObject(gl);
		gravityBehaviour.addObject(gl);
		
		// TODO - Make this HasReachedTarget condition/action.
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
//		ActionResult ar = moveTo.behave(sourceWord);
//		println(ar.complete);
//		moveToBhvr.behaveAll();
	}

}
