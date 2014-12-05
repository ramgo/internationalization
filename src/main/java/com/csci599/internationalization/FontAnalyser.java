package com.csci599.internationalization;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/*import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;*/

public class FontAnalyser {

	AffineTransform at;
	FontRenderContext frc;
	
	public FontAnalyser() {
		frc = new FontRenderContext(at, true,
				true);
		at = new AffineTransform();
	}
	public Dimension getTextDimenesion(String text, String fontFamily, int fontSize, int style) {
		
		Font font = new Font(fontFamily, style, fontSize);
		int textwidth = (int) (font.getStringBounds(text, frc).getWidth());
		int textheight = (int) (font.getStringBounds(text, frc).getHeight());
		return new Dimension(textwidth, textheight);
	}
}