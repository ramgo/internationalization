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
	public static void main(String[] args) {
		/*
		 * WebDriver driver=new FirefoxDriver(); driver.get(
		 * "C:\\Users\\Soumili\\Dropbox\\internationalization\\Canvas.html");
		 * driver.manage().window(); //
		 * System.out.println(driver.getCurrentUrl());
		 * 
		 * 
		 * JavascriptExecutor js=(JavascriptExecutor) driver;
		 * 
		 * String javaScriptText=
		 * "var c = document.getElementById(\"myCanvas\");var ctx = c.getContext(\"2d\");ctx.font = \"30px Arial\";var txt = \"Hello World\";ctx.fillText(\"width:\" + ctx.measureText(txt).width, 10, 50);ctx.fillText(txt, 10, 100);"
		 * ; // javaScriptText =
		 * "document.defaultView.getComputedStyle(document.body,null).getPropertyValue(\"font-family\");"
		 * ; // System.out.println(javaScriptText); String domain_name=(String)
		 * js.executeScript(javaScriptText); System.out.println(domain_name);
		 */

		String text = "Hello \nWorld";
		System.out.println(text);
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true,
				true);
		Font font = new Font("Arial", Font.PLAIN, 30);
		int textwidth = (int) (font.getStringBounds(text, frc).getWidth());
		int textheight = (int) (font.getStringBounds(text, frc).getHeight());

		System.out.println(textwidth + "@@@@@@@@@@@" + textheight);
	}
}