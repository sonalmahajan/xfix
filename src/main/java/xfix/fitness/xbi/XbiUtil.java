package xfix.fitness.xbi;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import xfix.Constants;
import xfix.Util;
import xfix.WebDriverSingleton;
import xfix.XbiElementRelationship;
import xfix.WebDriverSingleton.Browser;

public class XbiUtil
{
	private static Map<String, String> matchedNodeXpaths;
	
	public static Map<String, String> getMatchedNodeXpaths()
	{
		return matchedNodeXpaths;
	}

	public static void setMatchedNodeXpaths(Map<String, String> matchedNodeXpaths)
	{
		XbiUtil.matchedNodeXpaths = matchedNodeXpaths;
	}

	public static void populateMatchedNodes(String content)
	{
		matchedNodeXpaths = new HashMap<String, String>();
		String[] contentArr = content.split(System.getProperty("line.separator"));
		for(int i = 0; i < contentArr.length; i++)
		{
			String line = contentArr[i];
			String[] lineArr = line.split(",");
			String node1 = lineArr[0];
			String node2 = lineArr[1];
			matchedNodeXpaths.put(node2.toLowerCase(), node1.toLowerCase());	// <test, reference>
		}
	}
	
	public static int getXpathDistance(String xpath1, String xpath2)
	{
		xpath1 = xpath1.toLowerCase();
		xpath2 = xpath2.toLowerCase();
		
		String xpath1Array[] = xpath1.split("/");
		String xpath2Array[] = xpath2.split("/");
		int xpath1Length = xpath1Array.length - 1;
		int xpath2Length = xpath2Array.length - 1;
		int distance;

		int matchingCount = 0;
		for (int i = 1; i < xpath1Array.length && i < xpath2Array.length; i++)
		{
			if (xpath1Array[i].equals(xpath2Array[i]))
			{
				matchingCount++;
			}
			else
			{
				break;
			}
		}

		distance = (xpath2Length - matchingCount) + (xpath1Length - matchingCount);

		return distance;
	}
	
	public static HtmlDomTree buildRTreeFromJson(String json)
	{
		HtmlDomTree rTree = null;
		rTree = new HtmlDomTree();
		rTree.buildHtmlDomTreeFromJson(json);
		return rTree;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map)
	{
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

		return result;
	}
	
	public static String getDOMJson(Browser browser)
	{
		String fileContents = "";
		try
		{
			fileContents = FileUtils.readFileToString(new File("src/main/resources/domInfo.js"), "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		JavascriptExecutor js = (JavascriptExecutor) WebDriverSingleton.getDriver(browser);         
		String json = (String) js.executeScript(fileContents);
		return json;
	}
	
	public static Map<String, Rectangle> getBoundingBoxes(String json)
	{
		Map<String, Rectangle> boundingBoxes = new HashMap<>();
		JSONArray arrDom = new JSONArray(json.trim());
		for (int i = 0; i < arrDom.length(); i++) 
		{
			JSONObject nodeData = arrDom.getJSONObject(i);
			int type = nodeData.getInt("type");
			if(type == 1)
			{
				String xpath = nodeData.getString("xpath");
				JSONArray data = nodeData.getJSONArray("coord");
				for (int i1 = 0; i1 < data.length(); i1++)
				{
					if(!NumberUtils.isNumber(data.get(i1).toString()))
					{
						data.put(i1, 0);
					}
				}
				int[] coords = { data.getInt(0), data.getInt(1), data.getInt(2),
						data.getInt(3) };
				boundingBoxes.put(xpath.toLowerCase(), new Rectangle(coords[0], coords[1], (coords[2] - coords[0]), (coords[3] - coords[1])));
			}
		}
		return boundingBoxes;
	}
	
	public static Map<String, XbiElementRelationship> getElementRelationshipsFromString(String content)
	{
		String[] contentArr = content.split(System.getProperty("line.separator"));
		Map<String, XbiElementRelationship> elementRelationships = new HashMap<>();
		String line = "";
		
		for(int i = 0; i < contentArr.length; i++)
		{
			line = contentArr[i].toLowerCase();
			String lineArr[] = line.split("-->");
			
			String element = "", parent = "";
			Set<String> children = new HashSet<>();
			Set<String> siblings = new HashSet<>();
			
			// element
			if(!lineArr[0].isEmpty())
			{
				lineArr[0] = lineArr[0].trim();
				element = lineArr[0];
			}
			
			// parent
			if(!lineArr[1].isEmpty())
			{
				lineArr[1] = lineArr[1].trim();
				parent = lineArr[1];
			}
			
			// children
			if(!lineArr[2].isEmpty())
			{
				lineArr[2] = lineArr[2].trim();
				if(!lineArr[2].isEmpty())
				{
					String childrenArr[] = lineArr[2].split(",");
					if(childrenArr.length > 0)
					{
						children.addAll(Arrays.asList(childrenArr));
					}
				}
			}
			
			// siblings
			if(!lineArr[3].isEmpty())
			{
				lineArr[3] = lineArr[3].trim();
				if(!lineArr[3].isEmpty())
				{
					String siblingsArr[] = lineArr[3].split(",");
					if(siblingsArr.length > 0)
					{
						siblings.addAll(Arrays.asList(siblingsArr));
					}
				}
			}
			
			XbiElementRelationship xer = new XbiElementRelationship(element, parent, children, siblings);
			elementRelationships.put(element, xer);
		}
		return elementRelationships;
	}
	
	public static double round(double value) {
		int places = 2;
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	private static double getRelValue(int numerator, int denominator)
	{
		return round(((double)numerator / (double)denominator) * 100.0);
	}
	
	public static String convertAbsoluteToRelativeValue(String xpath, String prop, String value)
	{
		String relValue = "";
		
		int absValue = Util.getNumbersFromString(value).get(0);

		// get parent 
		String parentXpath = xpath.substring(0, xpath.lastIndexOf("/"));
		WebElement parent = WebDriverSingleton.getDriver(Constants.TEST_BROWSER).findElement(By.xpath(parentXpath));
		int pW = parent.getSize().width;
		int pH = parent.getSize().height;
		while(pW == 0 || pH == 0)
		{
			// find a visible ancestor
			xpath = parentXpath;
			parentXpath = xpath.substring(0, xpath.lastIndexOf("/"));
			parent = WebDriverSingleton.getDriver(Constants.TEST_BROWSER).findElement(By.xpath(parentXpath));
			pW = parent.getSize().width;
			pH = parent.getSize().height;
		}
		
		if(prop.equalsIgnoreCase("width") || prop.equalsIgnoreCase("padding-top") || prop.equalsIgnoreCase("padding-bottom") ||
				prop.equalsIgnoreCase("padding-left") || prop.equalsIgnoreCase("padding-right") || prop.equalsIgnoreCase("margin-left") ||
				prop.equalsIgnoreCase("margin-top") || prop.equalsIgnoreCase("left") || prop.equalsIgnoreCase("right")
				/* || prop.equalsIgnoreCase("min-width") || prop.equalsIgnoreCase("max-width")*/)
		{
			relValue = getRelValue(absValue, pW) + "%";
		}
		else if(prop.equalsIgnoreCase("height") || prop.equalsIgnoreCase("top") || prop.equalsIgnoreCase("bottom") 
				/* || prop.equalsIgnoreCase("min-height") || prop.equalsIgnoreCase("max-height")*/)
		{
			relValue = getRelValue(absValue, pH) + "%";
		}
		else if(prop.equalsIgnoreCase("line-height"))
		{
			int fontSize =  Util.getNumbersFromString(WebDriverSingleton.getDriver(Constants.TEST_BROWSER).findElement(By.xpath(xpath)).getCssValue("font-size")).get(0);
			relValue = getRelValue(absValue, fontSize) + "%";
		}
		else
		{
			relValue = value;
		}
		
		return relValue;
	}
}
