package xfix.fitness.xbi;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;

import xfix.Constants;
import xfix.OptimalRootCause;
import xfix.RootCause;
import xfix.RootCauseList;
import xfix.Util;
import xfix.WebDriverSingleton;
import xfix.XFixConstants;
import xfix.XbiElementRelationship;
import xfix.input.xbi.ReadXpertOutput;
import xpert.edu.gatech.xpert.XpertMain;

public class XbiFitnessFunction
{
	private static int localFitnessCalls;
	private static double localFitnessTimeInSec;
	private static int globalFitnessCalls;
	private static double globalFitnessTimeInSec;
	private static String jsonRef;
	private static Map<String, Rectangle> boundingBoxesRef;
	private Map<String, Rectangle> boundingBoxesTest;
	private static Map<String, XbiElementRelationship> elementRelationshipsRef;
	private static Map<String, XbiElementRelationship> elementRelationshipsTest;
	
	private static HtmlDomTree domTreeRef;
	private static HtmlDomTree domTreeTest;
	
	public XbiFitnessFunction()
	{
		if(elementNeighborsCache == null)
		{
			elementNeighborsCache = new HashMap<String, Set<String>>();
		}
	}
	
	private static Map<String, Set<String>> elementNeighborsCache;
	
	public static void setLocalFitnessCalls(int localFitnessCalls) 
	{
		XbiFitnessFunction.localFitnessCalls = localFitnessCalls;
	}

	public static void setLocalFitnessTimeInSec(double localFitnessTimeInSec) 
	{
		XbiFitnessFunction.localFitnessTimeInSec = localFitnessTimeInSec;
	}

	public static void setGlobalFitnessCalls(int globalFitnessCalls) 
	{
		XbiFitnessFunction.globalFitnessCalls = globalFitnessCalls;
	}

	public static void setGlobalFitnessTimeInSec(double globalFitnessTimeInSec) 
	{
		XbiFitnessFunction.globalFitnessTimeInSec = globalFitnessTimeInSec;
	}

	public static int getLocalFitnessCalls()
	{
		return localFitnessCalls;
	}

	public static double getLocalFitnessTimeInSec()
	{
		return localFitnessTimeInSec;
	}

	public static int getGlobalFitnessCalls()
	{
		return globalFitnessCalls;
	}

	public static double getGlobalFitnessTimeInSec()
	{
		return globalFitnessTimeInSec;
	}

	public static String getJsonRef()
	{
		return jsonRef;
	}

	public static void setJsonRef(String jsonRef)
	{
		XbiFitnessFunction.jsonRef = jsonRef;
	}

	public static Map<String, Rectangle> getBoundingBoxesRef()
	{
		return boundingBoxesRef;
	}

	public static void setBoundingBoxesRef(Map<String, Rectangle> boundingBoxesRef)
	{
		XbiFitnessFunction.boundingBoxesRef = boundingBoxesRef;
	}

	public Map<String, Rectangle> getBoundingBoxesTest()
	{
		return boundingBoxesTest;
	}

	public void setBoundingBoxesTest(Map<String, Rectangle> boundingBoxesTest)
	{
		this.boundingBoxesTest = boundingBoxesTest;
	}

	public static Map<String, XbiElementRelationship> getElementRelationshipsRef()
	{
		return elementRelationshipsRef;
	}

	public static void setElementRelationshipsRef(Map<String, XbiElementRelationship> elementRelationshipsRef)
	{
		XbiFitnessFunction.elementRelationshipsRef = elementRelationshipsRef;
	}

	public static Map<String, XbiElementRelationship> getElementRelationshipsTest()
	{
		return elementRelationshipsTest;
	}

	public static void setElementRelationshipsTest(Map<String, XbiElementRelationship> elementRelationshipsTest)
	{
		XbiFitnessFunction.elementRelationshipsTest = elementRelationshipsTest;
	}

	public static HtmlDomTree getDomTreeRef() {
		return domTreeRef;
	}

	public static void setDomTreeRef(HtmlDomTree domTreeRef) {
		XbiFitnessFunction.domTreeRef = domTreeRef;
	}

	public static HtmlDomTree getDomTreeTest() {
		return domTreeTest;
	}

	public static void setDomTreeTest(HtmlDomTree domTreeTest) {
		XbiFitnessFunction.domTreeTest = domTreeTest;
	}

	public double getFitnessScoreLocal(RootCauseList chromosome, RootCause gene)
	{
		double fitnessScore = Double.MAX_VALUE;
		long startTime = System.nanoTime();
		
		// apply new value to gene in test browser
		WebDriverSingleton.loadPage(XFixConstants.getTestPageFullPath(), Constants.TEST_BROWSER);
		Util.applyNewValues(chromosome, WebDriverSingleton.getDriver(Constants.TEST_BROWSER));
		
		// parse json to collect necessary coordinates information
		String jsonTest = XbiUtil.getDOMJson(Constants.TEST_BROWSER);
		boundingBoxesTest = XbiUtil.getBoundingBoxes(jsonTest);
		
		fitnessScore = calculateFitnessScore(gene.getXpath());
		
		long endTime = System.nanoTime();
		localFitnessTimeInSec = localFitnessTimeInSec + Util.convertNanosecondsToSeconds((endTime - startTime));
		localFitnessCalls++;
		
		return fitnessScore;
	}
	
	public double getFitnessScoreGlobal(RootCauseList chromosome)
	{
		double fitnessScore = 0.0;
		long startTime = System.nanoTime();
		
		// run xpert
		String fileContents = "";
		try
		{
			// read dom info fetching javascript in string
			fileContents = FileUtils.readFileToString(new File("src/main/resources/domInfo.js"), "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(jsonRef == null || jsonRef.isEmpty())
		{
			// get dom info from reference browser
			WebDriverSingleton.loadPage(XFixConstants.getTestPageFullPath(), Constants.REFERENCE_BROWSER);
			JavascriptExecutor js1 = (JavascriptExecutor) WebDriverSingleton.getDriver(Constants.REFERENCE_BROWSER);         
			jsonRef = (String) js1.executeScript(fileContents);
			WebDriverSingleton.closeBrowser(Constants.REFERENCE_BROWSER);
		}
		
		// get dom info from test browser
		WebDriverSingleton.loadPage(XFixConstants.getTestPageFullPath(), Constants.TEST_BROWSER);
		Util.applyNewValues(chromosome, WebDriverSingleton.getDriver(Constants.TEST_BROWSER));
		JavascriptExecutor js2 = (JavascriptExecutor) WebDriverSingleton.getDriver(Constants.TEST_BROWSER);         
		String jsonTest = (String) js2.executeScript(fileContents);
		
		XpertMain xm = new XpertMain();
		Map<String, String> resultsMap = xm.runXpertWithoutGeneratingReports(jsonRef, jsonTest, "", "");
		
		// read input from XPERT's reported file
		ReadXpertOutput rxo = new ReadXpertOutput();
		rxo.readInputFromString(resultsMap.get("layout"));
		rxo.readInputFromString(resultsMap.get("content"));
		fitnessScore = rxo.getTotalNumberOfXBIsReported();
		System.out.println("Global fitness score: XBIs by XPERT (size = " + rxo.getXbiStrings().size() + ")");
		for(String xbi : rxo.getXbiStrings())
		{
			System.out.println(xbi);
		}
		
		long endTime = System.nanoTime();
		globalFitnessCalls++;
		globalFitnessTimeInSec = globalFitnessTimeInSec + Util.convertNanosecondsToSeconds((endTime - startTime));
		
		return fitnessScore;
	}
	
	public double getFitnessScoreBestCombination(RootCauseList realChromosome, List<OptimalRootCause> candidateRootCausesChromosome, String binaryChromosome)
	{
		// update chromosome with values from binary chromosome
		RootCauseList tempChromosome = realChromosome.copy();
		int i = 0;
		for(OptimalRootCause og : candidateRootCausesChromosome)
		{
			if(binaryChromosome.charAt(i) == '1')
			{
				// set value in chromosome
				RootCause tempGene = tempChromosome.getGene(og.getXpath());
				tempGene.updateValue(og.getProp(), og.getVal());
			}
			i++;
		}
		return getFitnessScoreGlobal(tempChromosome);
	}
	
	private Set<String> getNeighbors(HtmlDomTree rTree, String fromElementXpath, int distance)
	{
		// check if element present in cache
		if(elementNeighborsCache.containsKey(fromElementXpath))
		{
			// return from cache
			return elementNeighborsCache.get(fromElementXpath);
		}
		
		Set<String> neighbors = new HashSet<>();
		Node<HtmlElement> fromElementNode = rTree.searchHtmlDomTreeByXpath(fromElementXpath);
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(fromElementNode);
		
		while(!q.isEmpty())
		{
			Node<HtmlElement> node = q.remove();
			
			// inspect parent of fromElement
			if(node.getParent() != null)
			{
				Node<HtmlElement> parent = node.getParent();
				if(!neighbors.contains(parent.getData().getXpath()) && !parent.getData().equals(fromElementNode.getData()) &&  
						XbiUtil.getXpathDistance(fromElementXpath, parent.getData().getXpath()) <= distance)
				{
					neighbors.add(parent.getData().getXpath());
					q.add(parent);
				}
			}
			
			// inspect children of fromElement
			if(node.getChildren() != null && node.getChildren().size() > 0)
			{
				for(Node<HtmlElement> child : node.getChildren())
				{
					if(!neighbors.contains(child.getData().getXpath()) && !child.getData().equals(fromElementNode.getData()) &&  
							XbiUtil.getXpathDistance(fromElementXpath, child.getData().getXpath()) <= distance)
					{
						neighbors.add(child.getData().getXpath());
						q.add(child);
					}
				}
			}
			
			// inspect siblings of fromElement
			if(node.getNodeSiblings() != null && node.getNodeSiblings().size() > 0)
			{
				for(Node<HtmlElement> sibling : node.getNodeSiblings())
				{
					if(!neighbors.contains(sibling.getData().getXpath()) && !sibling.getData().equals(fromElementNode.getData()) &&  
							XbiUtil.getXpathDistance(fromElementXpath, sibling.getData().getXpath()) <= distance)
					{
						neighbors.add(sibling.getData().getXpath());
						q.add(sibling);
					}
				}
			}
		}
		elementNeighborsCache.put(fromElementXpath.toLowerCase(), neighbors);
		System.out.println("Neighbors of " + fromElementXpath + " = " + neighbors);
		return neighbors;
	}
	
	private double absolutePositionRefTestDOMNeighborsMethod(String eRef, String eTest)
	{
		double W1 = 1.0;
		double W2 = 2.0;
		double W3 = 0.5;
		
		// get coordinates from reference browser
		Rectangle rectERef = boundingBoxesRef.get(eRef);
		int eRefx1 = rectERef.x;
		int eRefy1 = rectERef.y;
		int eRefx2 = rectERef.x + rectERef.width;
		int eRefy2 = rectERef.y + rectERef.height;
		
		// get coordinates from test browser
		Rectangle rectETest = boundingBoxesTest.get(eTest);
		int eTestx1 = rectETest.x;
		int eTesty1 = rectETest.y;
		int eTestx2 = rectETest.x + rectETest.width;
		int eTesty2 = rectETest.y + rectETest.height;
		
		// distance between top left and bottom right corners of current element
		double diffLocationTopLeft = getDistanceBetweenPoints(eTestx1, eRefx1, eTesty1, eRefy1);
		double diffLocationBottomRight = getDistanceBetweenPoints(eTestx2, eRefx2, eTesty2, eRefy2);
		double ePos = (diffLocationTopLeft + diffLocationBottomRight);
		
		// distance between diagonal of current element
		double eSize = Math.abs(rectERef.width - rectETest.width) + Math.abs(rectERef.height - rectETest.height);
		
		// get DOM neighbors
		List<String> neighborsTest = new ArrayList<>(getNeighbors(domTreeTest, eTest, Constants.NEIGHBORHOOD_RADIUS));
		
		// distance between top left and bottom right corners of neighbor in ref and test browser
		double neighborhoodScore = 0.0;
		double nPos = 0.0;
		if(neighborsTest.size() > 0)
		{
			for(String nTest : neighborsTest)
			{
				String nRef = XbiUtil.getMatchedNodeXpaths().get(nTest);
				if(nRef != null && !nRef.isEmpty())
				{
					Rectangle nTestRect = boundingBoxesTest.get(nTest);
					Rectangle nRefRect = boundingBoxesRef.get(nRef);
					
					// top left & bottom right
					double nTL = getDistanceBetweenPoints(nRefRect.x, nTestRect.x, nRefRect.y, nTestRect.y);
					double nBR = getDistanceBetweenPoints((nRefRect.x+nRefRect.width), (nTestRect.x+nTestRect.width), (nRefRect.y+nRefRect.height), (nTestRect.y+nTestRect.height));
					double neighborTLBRDist = (nTL + nBR);
					neighborhoodScore = neighborhoodScore + neighborTLBRDist;
				}
			}
			nPos = neighborhoodScore;
		}
		
		double fitnessScore = (W1 * ePos) + (W2 * eSize) + (W3 * nPos);
		System.out.println("ePos = " + ePos);
		System.out.println("eSize = " + eSize);
		System.out.println("nPos = " + nPos);
		System.out.println("Local fitness score = " + fitnessScore);
		if(ePos == 0.0 && eSize == 0.0)
		{
			System.out.println("ePos and eSize == 0, hence fitness score set to 0");
			fitnessScore = 0.0;
		}
		return fitnessScore;
	}
	
	private double getDistanceBetweenPoints(int x1, int x2, int y1, int y2)
	{
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}
	
	private double calculateFitnessScore(String eTest)
	{
		// get matching element from reference browser
		String eRef = XbiUtil.getMatchedNodeXpaths().get(eTest);
		
		return absolutePositionRefTestDOMNeighborsMethod(eRef, eTest);
	}
}
