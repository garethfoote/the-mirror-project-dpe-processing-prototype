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
import net.nexttext.behaviour.physics.Stop;
import net.nexttext.behaviour.standard.DoNothing;
import net.nexttext.behaviour.standard.MoveTo;
import net.nexttext.property.ColorProperty;
import net.nexttext.property.PVectorProperty;
import net.nexttext.property.Property;

  // DEBUG
  private TrackerExtra trackAction;
  // end DEBUG

  private Book book;
  private PFont fenix;
  private double strMax = 0.03;
  private double strMin = 0.0;
  private double strength = 0.06;
  private double delay = 0.2;
  private String targetSentence = "Hold it fast with sleepless eyes";
  private String sourceSentence = "The preening water laps with the ambivalence of your look.";
  private String targetText = "sleepless";
  private String sourceText = "ambivalence";
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
//    size(1024, 768);
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

    startButton = cp.addButton("startDrop")
        .setCaptionLabel("Start")
                .setPosition(0, 0)
                .setSize(200, 19);

    cp.addSlider("updateStrength")  
       .setColorCaptionLabel(0)
       .setCaptionLabel("Gravity strength")
       .setValue((float)strength)
         .setRange((float)0.02, (float)0.2)
         .setPosition(0, 19)
         .setSize(200, 29);

    cp.addSlider("updateStrengthVariant")  
       .setCaptionLabel("Gravity strength variant")
       .setColorCaptionLabel(0)
       .setValue((float)strMax)
         .setRange((float)0, (float)0.08)
         .setPosition(0, 19+29+29)
         .setPosition(0, 19+29)
         .setSize(200, 29);
    
    cp.addSlider("updateDelay")  
       .setColorCaptionLabel(0)
       .setCaptionLabel("Drop time delay")
       .setValue((float)delay)
         .setRange((float)0, (float)0.5)
         .setPosition(0, 19+29+29)
         .setSize(200, 29);

  }
  
  public void updateDelay(ControlEvent ce){
    delay = ce.getValue();
  }

  public void updateStrengthVariant(ControlEvent ce){
    strMax = ce.getValue();
  }
  
  public void updateStrength(ControlEvent ce){
    strength = ce.getValue();
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
      
    // Position poem2.
    poem2Root.getPosition().set(new PVector(2*(width/3),0));
    
    // Do the building up front.
    builder.setParent(poem1Root);
    TextObjectGroup sourceLine = builder.buildSentence(sourceSentence, 0, height/2);

    builder.setParent(poem2Root);
    TextObjectGroup targetLine = builder.buildSentence(targetSentence, 0, height/2);
    
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
      
    targetBox = null;
    sourceWidth = sourceWord.getBounds().width+sourceWord.getRightSibling().getBounds().width;
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

        OnCollision col = new OnCollision(new RemoveObject());
        Behaviour colBhvr = col.makeBehaviour();
        colBhvr.addObject(element);
        book.addBehaviour(colBhvr);
        found = true;
      }
    }
    
//    startDrop();

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
          // Make text object glyph, and position above target.
          TextObjectGlyph toDropIn = new TextObjectGlyph(glyph.toString(), fenix, 28, ps, gPos);
          poem2Root.attachChild(toDropIn);
          book.getSpatialList().add(toDropIn);
          // Pass gravity action to this new glyph from drop out.
          AbstractAction doA = dropGlyphOut(toDropOut, j);
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

    // create and add the Gravity Behaviour
    AbstractAction gravity = new Delay(new Gravity((float)strength+(float)rand), (float)delay*index);
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

/*    int c1 = color(255);
    int c2 = color(unhex("999999"));
    for (int i = 0; i <= height; i++) {
      float inter = map(i, 0, height, 0, 1);
      int c = lerpColor(c1, c2, inter);
      stroke(c);
      line(0, i, width, i);
    }*/

    if(trackAction != null && trackAction.getCount() > 0){
      println("COUNT>>",trackAction.getCount());
    }
  }

