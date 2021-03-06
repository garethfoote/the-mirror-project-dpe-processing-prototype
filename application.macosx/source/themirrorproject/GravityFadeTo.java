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

import net.nexttext.behaviour.Action.ActionResult;
import net.nexttext.behaviour.physics.PhysicsAction;

import processing.core.PVector;
import net.nexttext.Book;
import net.nexttext.TextObject;
import net.nexttext.property.ColorProperty;
import net.nexttext.property.NumberProperty;
/**
 * A combination of Gravity and FadeTo classes from NextText package.
 */
public class GravityFadeTo extends PhysicsAction {

    private boolean applyToFill = false;
    private boolean applyToStroke = false;

    public GravityFadeTo ( float strength, int fadeTo, int speed, boolean fill, boolean stroke   ) {
    	// FadeTo
        applyToFill = fill;
        applyToStroke = stroke;
        if (applyToFill) {
            properties().init("AlphaFill", new NumberProperty(fadeTo) );
            properties().init("SpeedFill", new NumberProperty(speed) );
        }
        if (applyToStroke) {
            properties().init("AlphaStroke", new NumberProperty(fadeTo) );
            properties().init("SpeedStroke", new NumberProperty(speed) );
        }
        // Gravity
        properties().init( "Strength", new NumberProperty(strength) );
    }
    
    public ActionResult behave( TextObject to) {
        
    	// Gravity
    	PVector acc = new PVector(0, ((NumberProperty)properties().get("Strength")).get() );
        applyAcceleration(to, acc);

        // FadeTo
        ColorProperty cProp;
        boolean doneFill = false;
        boolean doneStroke = false;
        
        if (applyToFill) {
            // retrieve this object's colour
            cProp = to.getColor();    
            // retrieve this action's properties
            int alphaFill = (int)((NumberProperty)(properties().get("AlphaFill"))).get();
            int speedFill = (int)((NumberProperty)(properties().get("SpeedFill"))).get();
            // fade the fill colour
            doneFill = fadeTo(cProp, alphaFill, speedFill);
        }
        if (applyToStroke) {
            // retrieve this object's stroke colour
            cProp = to.getStrokeColor(); 
            // retrieve this action's properties
            int alphaStroke = (int)((NumberProperty)(properties().get("AlphaStroke"))).get();
            int speedStroke = (int)((NumberProperty)(properties().get("SpeedStroke"))).get();
            // fade the stroke colour
            doneStroke = fadeTo(cProp, alphaStroke, speedStroke);
        }
        
        if ((applyToFill==doneFill) && (applyToStroke==doneStroke)){
        	Book book = to.getBook();
        	book.removeObject(to);
            return new ActionResult(true, true, true);
        }
        
        return new ActionResult(false, true, false);
    }

    private boolean fadeTo (ColorProperty prop, int fadeTo, int speed) {
        
        Color color = prop.get();
        int a = color.getAlpha();
        
        if ( a < fadeTo ) { 
            a += speed;
        	if ( a > fadeTo ) a = fadeTo; // check if we passed it             
    	}
    	else if ( a > fadeTo ) { 
	    a -= speed;           
	    	if ( a < fadeTo ) a = fadeTo; // check if we passed it
    	}
        
        // update the color property 
        Color newColor = new Color(color.getRed(), color.getGreen(), 
                                    color.getBlue(), a);    	
        prop.set( newColor );
        
        return a == fadeTo;
    }

}
