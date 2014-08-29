import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.Color; 
import java.awt.Rectangle; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import java.util.Set; 
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
import controlP5.Button; 
import controlP5.ControlEvent; 
import controlP5.ControlP5; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TheMirrorProject extends PApplet {

/*
 *     This file is part of The Mirror Project.
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











































  // DEBUG
  private TrackerExtra trackAction;
  // end DEBUG
  
  private Book book;
  private PFont fenix;
  private int arcHeight = 200;
  private int arcVariant = 50;
  private int angleVariant = 10;
  private double strMax = 0.03f;
  private double strMin = 0.0f;
  private float g = 0.081f;
  private int staggerDelay = 300;
  private int delayVariant = 150;
  private int fadeSpeed = 3;
  private float minFlightTime = Float.MAX_VALUE;
  private float maxFlightTime = 0.0f;
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
  private String targetText = "marks";
  private String sourceText = "apple";
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

    startButton = cp.addButton("startThrow")
        .setCaptionLabel("Start")
                .setPosition(0, 0)
                .setSize(200, 19);

    cp.addSlider("updateStrength")  
       .setColorCaptionLabel(0)
       .setCaptionLabel("Gravity strength")
       .setValue((float)g)
         .setRange((float)0.02f, (float)0.2f)
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
    
/*    cp.addSlider("updateDelay")  
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
  
  public void prepareBook(){
    
      book = new Book(this);
      Book.bRemoveEmptyGroups = false;
      // Must be added for any physics action to work.
    book.addBehaviour(moveBehaviour);
    book.addBehaviour(moveGlyphBehaviour);
    
    builder = new TextObjectBuilder(book);
    builder.setFont(fenix, 28);
    builder.addGlyphProperty("StrokeColor", new ColorProperty(new Color(0,0,0,0)));
//    builder.setAddToSpatialList(true);

    TextPage poem1 = book.addPage("poem1");
    TextPage poem2 = book.addPage("poem2");
    poem1Root = poem1.getTextRoot();
    poem2Root = poem2.getTextRoot();
  
  }
  
  public void getTarget(TextObjectGroup poemRoot, String search){

    boolean found = false;
    targetBox = null;

    postTargetWords.clear();
    // Get target word and collect words to right of this.
    TextObjectIterator itrT = poemRoot.iterator();
    TextObjectGroup targetLine = null;
    while (itrT.hasNext()) {
      TextObject element = itrT.next();
      if(found == true
//          && !element.toString().equals(targetLine.toString())
          && element.getClass().getSimpleName().equals("TextObjectGroup")){

        if(element.toString().equals(targetLine.toString())){
          break;
        }
        postTargetWords.add(element);
      }

      if(!element.getClass().getSimpleName().equals("TextObjectGlyph")
        && element.toString().equals(search)){
        targetWord = (TextObjectGroup)element;
        targetLine = targetWord.getParent();
        targetBox = targetWord.getBounds();
        targetWidth = targetWord.getBounds().width;
        book.getSpatialList().add(targetWord);
        found = true;
      }
    }  
  }
  
  public void getSource(TextObjectGroup poemRoot, String search){
    
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

  public void buildText() {

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
//    builder.buildSentence(test, 0, 0);
    
  }

  public void startThrow(){

    startButton.hide();

    getSource(poem1Root, "goddess");
    getTarget(poem2Root, "Land");

    println("sourceWord", sourceWord);
    println("targetWord", targetWord);

    applySourceActions(0);
    applyTargetActions(0);
    applyTargetLineActions(0);

    getSource(poem2Root, "bosom");
    getTarget(poem1Root, "goddess");
    
    println("sourceWord", sourceWord);
    println("targetWord", targetWord);

    applySourceActions(5);
    applyTargetActions(5);
    applyTargetLineActions(5);

    getSource(poem1Root, "sky");
    getTarget(poem2Root, "bosom");
    
    println("sourceWord", sourceWord);
    println("targetWord", targetWord);

    applySourceActions(10);
    applyTargetActions(10);
    applyTargetLineActions(10);

  }

  public void applySourceActions(int delaySeconds){
    
    PVector targetPos = targetWord.getPositionAbsolute().get();
    PVector sourcePos = sourceWord.getPositionAbsolute().get();

    PVector diff = new PVector();
    diff.set(targetPos);
    diff.sub(sourcePos);
        TextObject dto = makeDuplicate(sourceWord, true);
    FlightTo flight = new FlightTo(diff, (int)arcHeight, arcVariant, (float)g);
    ApplyToGlyphDelay applyToGlyph = new ApplyToGlyphDelay(flight, (float)staggerDelay/1000);
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
        sourceWord.getColor().set(new Color(0,0,0,20));

    /*
        // Delay.
        float d = ((index*staggerDelay)/1000.0f);
        AbstractAction throwActionsDelay = new Delay(throwActions, d);
        if(minFlightTime > flightTime+(d*frameRate)){
          minFlightTime = flightTime+(d*frameRate);
        }
        if(maxFlightTime < flightTime+(d*frameRate)){
          maxFlightTime = flightTime+(d*frameRate);
        }
        */

        /*
          // Fade back.
          int additionalDelay = (int)(minFlightTime/frameRate)*1000;
          float d = ((j*staggerDelay)+additionalDelay)/1000.0f;
          AbstractAction fadeTo = new FadeTo(255, 5, true, false);
          AbstractAction fadeToDelay = new Delay(fadeTo, d);
          Behaviour fadeToBhvr = fadeToDelay.makeBehaviour();
          fadeToBhvr.addObject(originalGlyph);
          book.addGlyphBehaviour(fadeToBhvr);
          */
  }
        

//          http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
//          // Uniform distribution
//          delay = delayMin + (delayMax-delayMin)*x;
//          double u = Math.random();
//          // Increase expoentially.
//          double x = Math.log(1-u)/(-0.1);
//          delay = delayMin + (delayMax-delayMin)*x;
//          println(delay);

  public void applyTargetActions(int delaySeconds){
    
    TextObjectGlyphIterator glyphs = targetWord.glyphIterator();
    int j = targetWord.getNumChildren();
        while (glyphs.hasNext()) {
          j--;
          TextObject glyph = glyphs.next();
          // Fade out.
          int additionalDelay = (int)(minFlightTime/frameRate)*1000;
          float d = (additionalDelay-(j*staggerDelay))/1000.0f;
          AbstractAction fadeTo = new FadeTo(1, 5, true, false);
          AbstractAction fadeToDelay = new Delay(fadeTo, d);
          // Keeping these two delays separate for the moment.
          Delay pauseDelay = new Delay(fadeToDelay, delaySeconds);
          
          Behaviour fadeToBhvr = pauseDelay.makeBehaviour();
          fadeToBhvr.addObject(glyph);
          book.addGlyphBehaviour(fadeToBhvr);
        }
    
  }

  public void applyTargetLineActions(int delaySeconds){
    
    // Shift words along to create space as letters land.
    float changeX = (float)(sourceWidth-targetWidth);
    int additionalDelay = (int)(maxFlightTime/frameRate);
        for (TextObject to : postTargetWords) {
                // Move post target words along incrementally.
                PVector pos = to.getPosition().get();
                pos.add(changeX, 0, 0);
            AbstractAction moveAction = new MoveToEase(pos, 1);
            Delay pauseDelay = new Delay(moveAction, delaySeconds);
            Behaviour moveBhvr = pauseDelay.makeBehaviour();
                moveBhvr.addObject(to);
                book.addBehaviour(moveBhvr);
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
  
  public TextObject makeDuplicateGlyph(TextObject to, Boolean addToSpatialList){
        
    TextObject duplicate = new TextObjectGlyph(to.toString(), fenix, 28, ps, to.getLocation());
        poem1Root.attachChild(duplicate);
        if(addToSpatialList == true){
          book.getSpatialList().add(duplicate);
        }
    
    return duplicate;
  }

  public TextObject makeDuplicate(TextObject to, Boolean addToSpatialList){
        
    builder.setParent(poem1Root);
    TextObject duplicate = builder.buildSentence(to.toString(), to.getLocation(), Integer.MAX_VALUE);
    
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
 
  public void draw() {
    
    background(255);
    
    // apply the behaviours to the text and draw it
    book.step();
    book.draw();
  }

  

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TheMirrorProject" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
