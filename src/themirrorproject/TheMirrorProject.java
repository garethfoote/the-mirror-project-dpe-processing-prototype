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
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;

public class TheMirrorProject extends PApplet {

	private Book book;
	private PFont fenix;
	private String sourceSentence = "I am the Rock";
	private String targetSentence = "I am the Sea";
	private String sourceText = "Rock";
	private String targetText = "the";
	private TextObjectBuilder builder;
	private TextObjectGroup poem1Root;
	private TextObjectGroup poem2Root;
	private TextObjectGroup sourceWord;
	private TextObjectGroup targetWord;
	private Boolean applied = false;
	private double strMax = 0.06;
	private double strMin = 0.03;
	private Map<String,Property> ps = new HashMap<String,Property>();
	
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
    	
		// Get target word.
		TextObjectIterator itrT = targetLine.iterator();
		while (itrT.hasNext()) {
			TextObject element = itrT.next();
			if(element.toString().equals(targetText)){
				targetWord = (TextObjectGroup)element;
			}
		}

		PVector tPos = targetWord.getPosition().get();
		TextObjectGlyphIterator glyphs = sourceWord.glyphIterator();
        while (glyphs.hasNext()) {
        	TextObject glyph = glyphs.next();
        	PVectorProperty gLocal = glyph.getPosition();
        	PVector gPos = new PVector(gLocal.getX()+tPos.x, sourceWord.getLocation().y-height);

        	// Make text object glyph and position above target.
        	TextObjectGlyph toG = new TextObjectGlyph(glyph.toString(), fenix, 28, ps, gPos);
        	// Pass gravity action to this new glyph from drop out.
        	AbstractAction doA = dropGlyphOut(glyph);
        	poem2Root.attachChild(toG);
        	// Returns chain of events that occur as glyph finds target.
        	AbstractAction chainStop = dropGlyphIn(toG, doA, targetWord.getLocation());
        }

	}
	
	public AbstractAction dropGlyphIn(TextObject gl, AbstractAction dropIn, PVector targetPos){
		
		PVector glPos = gl.getLocation();

//		MoveTo moveInstant = new MoveTo((int)(glPos.x+(offset)), 0);
//		Gravity dropIn = new Gravity((float)0.05);
		Stop stop = new Stop();
		
		Chain chainMove = new Chain();
		chainMove.add(dropIn);
		
		println(poem2Root.getLocation());
		Multiplexer chainStop = new Multiplexer();		
		chainStop.add(new Repeat(stop));
		chainStop.add(new MoveTo((int)(glPos.x), (int)targetPos.y));
		
		Condition condition = new HasReachedTarget(chainStop, chainMove, targetPos.y);
				
		Behaviour topBhvr = condition.makeBehaviour();
		book.addGlyphBehaviour(topBhvr);
		
		topBhvr.addObject(gl);
		moveBehaviour.addObject(gl);
		
		return chainStop;
		
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
