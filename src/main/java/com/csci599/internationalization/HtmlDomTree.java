package com.csci599.internationalization;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xml.sax.SAXException;

public class HtmlDomTree
{
	private Node<HtmlElement> root;
	private int rectId;
	private Map<Integer, Node<HtmlElement>> rectIdHtmlDomTreeNodeMap;
	private CSSParser cssParser;
	private HtmlAttributesParser htmlAttributesParser;
	FontAnalyser fontanalyser;
	static WebDriver driver;
	WebDriver oracle;
	int oracleHeight,oracleWidth;
	public static final String[] NON_VISUAL_TAGS = new String[] {"head", "script", "link", "meta", "style"};
	
	public HtmlDomTree(WebDriver driver,WebDriver oracle, String htmlFileFullPath,String oracleFileFullPath) throws SAXException, IOException
	{
		// parse CSS
		this.driver = driver;
		this.oracle=oracle;
		cssParser = new CSSParser(htmlFileFullPath);
		cssParser.parseCSS();
//		
		// parse HTML attributes
		htmlAttributesParser = new HtmlAttributesParser(htmlFileFullPath);
		
		WebElement rootElementFromSelenium = driver.findElement(By.xpath("//*"));
		HtmlElement htmlRootElement = new HtmlElement();
		int x = rootElementFromSelenium.getLocation().x;
		int y = rootElementFromSelenium.getLocation().y;
		int w = rootElementFromSelenium.getSize().width;
		int h = rootElementFromSelenium.getSize().height;
				
		htmlRootElement.setSeleniumWebElement(rootElementFromSelenium);
		htmlRootElement.setTagName(rootElementFromSelenium.getTagName());
		htmlRootElement.setX(x);
		htmlRootElement.setY(y);
		htmlRootElement.setWidth(w);
		htmlRootElement.setHeight(h);
		this.root = new Node<HtmlElement>(null, htmlRootElement);
		htmlRootElement.setXpath(computeXpath(this.root));
		htmlRootElement.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(htmlRootElement.getXpath()));
		try
		{
			htmlRootElement.setCssProperties(cssParser.getCSSPropertiesForElement(htmlRootElement.getXpath()));
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		fontanalyser = new FontAnalyser();
//		htmlRootElement.setRectId(rectId);
	}

	public Node<HtmlElement> getRoot()
	{
		return root;
	}

	public void setRoot(Node<HtmlElement> root)
	{
		this.root = root;
	}

	public void buildHtmlDomTree()
	{
		buildHtmlDomTreeFromNode(this.root);
	}
	
	private void buildHtmlDomTreeFromNode(Node<HtmlElement> node)
	{
		try
		{
			List<WebElement> children = node.getData().getSeleniumWebElement().findElements(By.xpath("*"));
			
			for (WebElement child : children)
			{
				int x = child.getLocation().x;
				int y = child.getLocation().y;
				int w = child.getSize().width;
				int h = child.getSize().height;
				
				// adjust size of option to that of the parent (select)
				if(child.getTagName().equals("option"))
				{
					if(node.getData().getTagName().equals("select"))
					{
						x = node.getData().getX();
						y = node.getData().getY();
					}
				}
				
				// don't process elements with no visual impact
				//if(x >= 0 && y >= 0 && w > 0 && h > 0)
				if(!Arrays.asList(NON_VISUAL_TAGS).contains(child.getTagName()))
				{
					HtmlElement newChild = new HtmlElement();
					
					// set tag name
					newChild.setTagName(child.getTagName());
					
					// set id
					newChild.setId(child.getAttribute("id"));
					newChild.setText(child.getAttribute("innerHTML"));
					
					// set web element
					newChild.setSeleniumWebElement(child);
					
					// set rectangle information
					newChild.setX(x);
					newChild.setY(y);
					newChild.setWidth(w);
					newChild.setHeight(h);
					
					Node<HtmlElement> newNode = new Node<HtmlElement>(node, newChild);
					// set xpath by traversing the built html dom tree
					newChild.setXpath(computeXpath(newNode));

					// set css properties
					newChild.setCssProperties(cssParser.getCSSPropertiesForElement(newChild.getXpath()));
					
					// set html attributes
					newChild.setHtmlAttributes(htmlAttributesParser.getHTMLAttributesForElement(newChild.getXpath()));
					
//					newChild.setRectId(rectId);
//					rectIdHtmlDomTreeNodeMap.put(rectId, newNode);
					
					buildHtmlDomTreeFromNode(newNode);
				}
			}
		//	System.out.println("ali "+ node.getData().getTagName()+" "+node.getData().getId());
		}
		catch (NoSuchElementException e)
		{
			return;
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void postOrderTraversalForAdjustingElementSize(Node<HtmlElement> node)
	{
		if (node == null)
		{
			return;
		}
		
		if (node.getChildren() != null)
		{
			for (Node<HtmlElement> child : node.getChildren())
			{
				postOrderTraversalForAdjustingElementSize(child);
			}
		}
		
		int x = node.getData().getX();
		int y = node.getData().getY();
		int width = node.getData().getWidth();
		int height = node.getData().getHeight();
		
		Node<HtmlElement> parent = node.getParent();
		
		// adjust size of option to that of the parent (select)
		if(node.getData().getTagName().equals("option"))
		{
			if(parent.getData().getTagName().equals("select"))
			{
				x = parent.getData().getX();
				y = parent.getData().getY();
				
				node.getData().setX(x);
				node.getData().setY(y);
			}
		}
	}
	
	/**
	 * 	compute xpath of the invoking element from the root
	 */
	private String computeXpath(Node<HtmlElement> node)
	{
			return getElementTreeXPath(node);
	}
	
	private static String getElementTreeXPath(Node<HtmlElement> node)
	{
		ArrayList<String> paths = new ArrayList<String>();
		for(; node != null ; node = node.getParent())
		{
			HtmlElement element = node.getData();
			int index = 0;
			
			int siblingIndex = node.getCurrentNodeSiblingIndex();
			for(Node<HtmlElement> sibling = node.getSiblingNodeAtIndex(--siblingIndex) ; sibling != null ; sibling = node.getSiblingNodeAtIndex(--siblingIndex))
			{
				if(sibling.getData().getTagName().equals(element.getTagName()))
				{
					++index;
				}
			}
			String tagName = element.getTagName().toLowerCase();
			String pathIndex = "[" + (index + 1) + "]";
			paths.add(tagName + pathIndex);
		}
		
		String result = null;
		if(paths.size() > 0)
		{
			result = "/";
			for (int i = paths.size()-1 ; i > 0 ; i--)
			{
				result = result + paths.get(i) + "/";
			}
			result = result + paths.get(0);
		}
		
		return result;
	}
	
	
	public Node<HtmlElement> searchHtmlDomTreeByRectId(int rectId)
	{
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);
		
		while(!q.isEmpty())
		{
			Node<HtmlElement> node = q.remove();
			if(node.getData().getRectId() == rectId)
			{
				return node;
			}
			if (node.getChildren() != null)
			{
				for (Node<HtmlElement> child : node.getChildren())
				{
					q.add(child);
				}
			}
		}
		return null;
	}
	
	public Node<HtmlElement> searchHtmlDomTreeByNode(Node<HtmlElement> searchNode)
	{
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);
		
		while(!q.isEmpty())
		{
			Node<HtmlElement> node = q.remove();
			if(node.equals(searchNode))
			{
				return node;
			}
			if (node.getChildren() != null)
			{
				for (Node<HtmlElement> child : node.getChildren())
				{
					q.add(child);
				}
			}
		}
		return null;
	}
	
	public HtmlElement searchHtmlDomTreeByXpath(String xpath)
	{
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);
		
		while(!q.isEmpty())
		{
			Node<HtmlElement> node = q.remove();
			if(node.getData().getXpath().equalsIgnoreCase(xpath))
			{
				return node.getData();
			}
			if (node.getChildren() != null)
			{
				for (Node<HtmlElement> child : node.getChildren())
				{
					q.add(child);
				}
			}
		}
		return null;
	}
		
	public void preOrderTraversalRTree()
	{
		preOrderTraversalRTree(this.root);
	}
	
	private boolean elementOverflows (int fw, int fh, int w, int h) {
		
		if(w == 0 || h == 0)
			return false;
		
		if(fw > w || Math.abs(fh - h) >1) 
			return true;
		
		return false;
	}
	private void preOrderTraversalRTree(Node<HtmlElement> node)
	{
		if (node == null) {
			System.out.println("Node==Null");
			return;
		}
		String text=node.getData().getText();
		String xpath =  node.getData().getXpath();
		//String fixedness = driver.findElement(By.xpath(xpath)).getCssValue("width");
		WebElement curEle = driver.findElement(By.xpath(xpath));
		String fsize = curEle.getCssValue("font-size");
		String ftype = curEle.getCssValue("font-family");
		int idx;
		if((idx = ftype.indexOf(',')) != -1) {
			ftype = ftype.substring(0, idx);
		}
				
//		for (String key: node.getData().getCssProperties().keySet() ) {
//			System.out.println("k: " + key +  " v: " + node.getData().getCssProperties().get(key) );
//		}

		if (text!=null && !text.isEmpty() && text != " ") {
			text = text.trim();
			String singleLineText = text.replace('\n', ' ');
			
			int lastIdx;
			int fontSize = 0;
			if(fsize != null) {
				lastIdx = fsize.indexOf('p');
				if (lastIdx == -1) 
					lastIdx = 0;
				fontSize = (int)Double.parseDouble(fsize.substring(0,lastIdx));
			}
			//Dimension d = fontanalyser.getTextDimenesion(text, ftype, fontSize, Font.PLAIN) ;
			
			boolean containsHTML = singleLineText.matches(".*\\<[^>]+>.*");
			if (containsHTML) {
				
				//System.out.println("contains " + text);
				Pattern placeholder = Pattern.compile("<input.*placeholder=\"([^\"]+)\".*");
				Matcher m = placeholder.matcher(singleLineText);
				if (m.matches()) {
					//System.out.println(m.group(1));
					String placeHolder = m.group(1);
					Dimension d = fontanalyser.getTextDimenesion(placeHolder, ftype, fontSize, Font.PLAIN) ;
					if(elementOverflows(d.width, d.height, node.getData()
							.getWidth(), node.getData().getHeight())) {
						System.out.println("PLACE " + placeHolder);
						System.out.println(text
								+ " height: " + node.getData().getHeight()
								+ " Width: " + node.getData().getWidth()
								+ " fontHeight: " + d.height + " fontWidth: "
								+ d.width);
					}
				}
			}
			else {
				Dimension d = fontanalyser.getTextDimenesion(text, ftype, fontSize, Font.PLAIN) ;
				//System.out.println(text + " size " + fsize);			
				if(!text.equalsIgnoreCase("&nbsp;")) {
					text = text.replaceAll("&nbsp;", " ");
					
					if (elementOverflows(d.width, d.height, node.getData()
							.getWidth(), node.getData().getHeight())) {
						try{
						oracleHeight=oracle.findElement(By.xpath(node.getData().getXpath())).getSize().height;
						oracleWidth=oracle.findElement(By.xpath(node.getData().getXpath())).getSize().width;
						}catch(Exception e){
							System.out.println("Missing Xpath from Oracle");
						}
						System.out.println("Oracle H,W "+ oracleHeight+oracleWidth+ "testedElement H,W: "+ node.getData().getHeight()+node.getData().getWidth());
						if(elementOverflows(node.getData().getWidth(),node.getData().getHeight(), oracleWidth,oracleHeight)){
							System.out.println("Possilble word wrapping for: "+text);
						}
						System.out.println(text
								+ " height: " + node.getData().getHeight()
								+ " Width: " + node.getData().getWidth()
								+ " fontHeight: " + d.height + " fontWidth: "
								+ d.width);
						//System.out.println(ftype);
					}
				}
			}

		}
		
		if (node.getChildren() != null)
		{
			for (Node<HtmlElement> child : node.getChildren())
			{
				preOrderTraversalRTree(child);
			}
		}
	}	

	public static void main(String[] args) throws SAXException, IOException
	{
		long start = System.currentTimeMillis();
		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		// String prefix = "C:\\Users\\Soumili\\Documents\\GitHub\\internationalization\\page1.html";
		//String prefix = "C:\\Users\\ramgo\\Downloads\\internationalization\\page1.html";	
		String prefix = "C:\\Users\\Ali\\Documents\\internationalization\\page1.html";
		String prefix2 = "file:///C:\\Users\\Ali\\Documents\\internationalization\\oracle.html";
		instance.loadPage(prefix);
		WebDriver driver2 = new FirefoxDriver();
		driver2.get(prefix2);
		WebDriver driver = instance.getDriver();
		HtmlDomTree rt = new HtmlDomTree(driver, driver2,prefix,prefix2);
		rt.buildHtmlDomTree();
		rt.preOrderTraversalRTree();
		WebDriverSingleton.closeDriver();
		System.out.println("run time" + (System.currentTimeMillis() - start));
	}
}
