package xfix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import xfix.fitness.xbi.XbiFitnessFunction;
import xfix.fitness.xbi.XbiUtil;
import xfix.input.xbi.ReadXpertOutput;
import xfix.input.xbi.XpertXbi;
import xpert.edu.gatech.xpert.XpertMain;

public class XbiMainIterator
{
	private RootCauseList chromosome;
	private static int generation;
	
	private Set<XpertXbi> xpertXbis;
	private int totalNumberOfXBIsReported;
	
	private int beforeXbis;
	private int afterXbis;
	
	private String subjectBasepath;
	private String originalPageLocation;
	
	public XbiMainIterator() 
	{
		xpertXbis = new HashSet<>();
		beforeXbis = -1;
	}
	
	public int getTotalNumberOfXBIsReported() {
		return totalNumberOfXBIsReported;
	}

	public void setTotalNumberOfXBIsReported(int totalNumberOfXBIsReported) {
		this.totalNumberOfXBIsReported = totalNumberOfXBIsReported;
	}
	
	public static int getGeneration()
	{
		return generation;
	}

	public RootCauseList getChromosome()
	{
		return chromosome;
	}

	public String getSubjectBasepath() {
		return subjectBasepath;
	}

	public void setSubjectBasepath(String subjectBasepath) {
		this.subjectBasepath = subjectBasepath;
	}

	public void setChromosome(RootCauseList chromosome) {
		this.chromosome = chromosome;
	}

	public static void setGeneration(int generation) {
		XbiMainIterator.generation = generation;
	}

	public String getOriginalPageLocation() {
		return originalPageLocation;
	}

	public void setOriginalPageLocation(String originalPageLocation) {
		this.originalPageLocation = originalPageLocation;
	}

	public int getBeforeXbis() {
		return beforeXbis;
	}

	public int getAfterXbis() {
		return afterXbis;
	}
	
	public void runIterator()
	{
		long startInitTime = System.nanoTime();
		// run XPERT
		System.out.println("Open browsers " + Constants.REFERENCE_BROWSER + " and " + Constants.TEST_BROWSER);
		WebDriverSingleton.openBrowsers(new WebDriverSingleton.Browser[] {Constants.REFERENCE_BROWSER, Constants.TEST_BROWSER});
		WebDriverSingleton.loadPage(XFixConstants.getOraclePageFullPath(), Constants.REFERENCE_BROWSER);
		
		WebDriverSingleton.loadPage(XFixConstants.getTestPageFullPath(), Constants.TEST_BROWSER);
		String jsonRef = XbiUtil.getDOMJson(Constants.REFERENCE_BROWSER);
		WebDriverSingleton.closeBrowser(Constants.REFERENCE_BROWSER);
		XbiFitnessFunction.setJsonRef(jsonRef);
		String jsonTest = XbiUtil.getDOMJson(Constants.TEST_BROWSER);
		XpertMain xm = new XpertMain();
		Map<String, String> resultsMap = xm.runXpertWithoutGeneratingReports(jsonRef, jsonTest, "", "");
		
		// get info from reference browser
		XbiFitnessFunction.setBoundingBoxesRef(XbiUtil.getBoundingBoxes(jsonRef));
		XbiFitnessFunction.setElementRelationshipsRef(XbiUtil.getElementRelationshipsFromString(resultsMap.get("agRef")));
		XbiFitnessFunction.setElementRelationshipsTest(XbiUtil.getElementRelationshipsFromString(resultsMap.get("agTest")));
		
		// build dom tree
		System.out.println("Build R-tree for reference");
		XbiFitnessFunction.setDomTreeRef(XbiUtil.buildRTreeFromJson(jsonRef));
	
		System.out.println("Build R-tree for test");
		XbiFitnessFunction.setDomTreeTest(XbiUtil.buildRTreeFromJson(jsonTest));

		Set<String> finalXbis = new HashSet<>();
		
		generation = 1;
		RootCauseList prevChromosome = null;

		// read input from XPERT
		Set<String> currentXbis = readInputsFromString(resultsMap);
		
		long endInitTime = System.nanoTime();
		System.out.println("Init time = " + Util.convertNanosecondsToSeconds((endInitTime - startInitTime)) + " sec");

		while(true)
		{
			System.out.println("\n\nGENERATION " + generation);
			System.out.println("Reported XBIs by XPERT (size = " + currentXbis.size() + ")");
			for(String xbi : currentXbis)
			{
				System.out.println(xbi);
			}
			if(beforeXbis == -1)
			{
				beforeXbis = currentXbis.size();
			}
			
			// populate chromosome
			chromosome = new RootCauseList();
			chromosome = populateChromosome();
			chromosome.setGlobalFitnessScore(totalNumberOfXBIsReported);
			// copy previous generation values/genes from chromosome
			if(prevChromosome != null)
			{
				for(RootCause pg : prevChromosome.getGenes())
				{
					if(chromosome.getGene(pg.getXpath()) == null)
					{
						if(pg.getProcessedProperties().size() > 0)
						{
							// add gene to the new chromosome
							RootCause g = pg.copy();
							g.setProcess(false);
							chromosome.addGene(g);
						}
					}
					else
					{
						// update values
						RootCause newGene = pg.copy();
						RootCause oldGene = chromosome.getGene(pg.getXpath());
						chromosome.replaceGene(oldGene, newGene);
					}
				}
			}
			System.out.println("Chromosome = " + chromosome);
			
			// run local and global search
			XbiSearch s = new XbiSearch(chromosome);
            s.search();
            
            // rerun XPERT
			WebDriverSingleton.loadPage(XFixConstants.getTestPageFullPath(), Constants.TEST_BROWSER);
			Util.applyNewValues(chromosome, WebDriverSingleton.getDriver(Constants.TEST_BROWSER));
			jsonTest = XbiUtil.getDOMJson(Constants.TEST_BROWSER);
			resultsMap = xm.runXpertWithoutGeneratingReports(XbiFitnessFunction.getJsonRef(), jsonTest, "", "");
			XbiFitnessFunction.setElementRelationshipsTest(XbiUtil.getElementRelationshipsFromString(resultsMap.get("agTest")));
			
			// read XPERT's output
			Set<String> newXbis = readInputsFromString(resultsMap);
			
			// check termination conditions
			int ret = isTerminate(currentXbis, newXbis);
			if(ret == 0 || ret == 2)
			{
				finalXbis = newXbis;
				break;
			}
			else if(ret == 1)
			{
				// revert to the previous iteration chromosome and XBIs
				finalXbis = currentXbis;
				chromosome = prevChromosome.copy();
				break;
			}
			else
			{
				// continue with the loop
				currentXbis = newXbis;
				prevChromosome = chromosome.copy();
			}
			generation++;
		}
		
		// get after XBIs
		System.out.println("After XBIs by XPERT (size = " + finalXbis.size() + ")");
		for(String xbi : finalXbis)
		{
			System.out.println(xbi);
		}
		afterXbis = finalXbis.size();
		
		// create fixed test page
		outputFixedTestPage();
		
		WebDriverSingleton.closeAllOpenBrowsers();
	}
	
	private int isTerminate(Set<String> currentXbis, Set<String> newXbis)
	{
		int retValue = -1;
		
		if(newXbis.size() == 0)
		{
			System.out.println("Reason to terminate the approach: new XBIs = 0");
			retValue = 0;
		}
		else if(newXbis.size() > currentXbis.size())
		{
			System.out.println("Reason to terminate the approach: new XBIs more than previous iteration");
			retValue = 1;
		}
		else if(newXbis.size() == currentXbis.size())
		{
			boolean isSame = true;
			for(String xbi : currentXbis)
			{
				if(!newXbis.contains(xbi))
				{
					isSame = false;
					break;
				}
			}
			if(isSame)
			{
				System.out.println("Reason to terminate the approach: new XBIs same in content as previous iteration");
				retValue = 2;
			}
		}
		
		return retValue;
	}
	
	public void outputFixedTestPage()
	{
		System.out.println("\n------------------- Output fixed test file --------------------");
		
		// copy test page to fixed test page
		String fixedTestFile = new File(originalPageLocation).getParent() + File.separatorChar + "test_fixed.html";
		try
		{
			FileUtils.copyFile(new File(originalPageLocation), new File(fixedTestFile));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// get repair patch and write it to a new css file
		RepairPatch rp = new RepairPatch();
		String repairPatch = rp.getRepairPatch(chromosome);
		System.out.println("Repair patch = \n" + repairPatch);
		File repairCssFile = new File(new File(originalPageLocation).getParent() + File.separatorChar + Constants.REPAIR_CSS_FILENAME);
		BufferedWriter bw = null;
		FileWriter fw = null;
		try
		{
			fw = new FileWriter(repairCssFile);
			bw = new BufferedWriter(fw);
			bw.write(repairPatch);
			bw.close();
			fw.close();
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		
		// add the repair css file to the copied test page
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ArrayList<String> list = new ArrayList<String>();

		try 
		{
			reader = new BufferedReader(new FileReader(fixedTestFile));
			String tmp;
			while ((tmp = reader.readLine()) != null)
			{
				if(tmp.contains("</head>"))
				{
					tmp = tmp.replace("</head>", "<link href=\"" + Constants.REPAIR_CSS_FILENAME + "\" rel=\"stylesheet\" type=\"text/css\" media=\"all\">");
				}
				list.add(tmp);
			}
			reader.close();

			writer = new BufferedWriter(new FileWriter(fixedTestFile));
			for (int i = 0; i < list.size(); i++)
			{
				writer.write(list.get(i) + "\r\n");
			}
			writer.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private Set<String> readInputsFromString(Map<String, String> resultsMap)
	{
		ReadXpertOutput rxo = new ReadXpertOutput();
		rxo.readInputFromString(resultsMap.get("layout"));
		rxo.readInputFromString(resultsMap.get("content"));
		xpertXbis = rxo.getXpertXbis();
		totalNumberOfXBIsReported = rxo.getTotalNumberOfXBIsReported();
		
		// read matched DOM elements
		XbiUtil.populateMatchedNodes(resultsMap.get("matchedNodes"));
		
		return rxo.getXbiStrings();
	}
	
	private RootCauseList populateChromosome()
	{
		RootCauseList chromosome = new RootCauseList();
		
		for(XpertXbi xbi : xpertXbis)
		{
			List<RootCause> genes = getGenes(xbi);
			
			for(RootCause g : genes)
			{
				// check if gene already exists in the chromosome
				if(chromosome.getGene(g.getXpath()) != null)
				{
					// update existing gene with new CSS properties
					RootCause oldGene = chromosome.getGene(g.getXpath());
					RootCause newGene = chromosome.getGene(g.getXpath()).copy();
					for(String property : g.getPropValueMap().keySet())
					newGene.addProperty(property, g.getPropValueMap().get(property));
					newGene.clearProcessedProperties();
					chromosome.replaceGene(oldGene, newGene);
				}
				else
				{
					g.setProcess(true);
					chromosome.addGene(g);
				}
			}
		}
		return chromosome;
	}
	
	private List<RootCause> getGenes(XpertXbi xbi)
	{
		List<RootCause> geneList = new ArrayList<>();
		
		// add xpath
		if(Constants.CONTAINS_XBIS.contains(xbi.getLabel()))
		{
			if(xbi.getE1Test() != null && xbi.getE2Test() != null)
			{
				// add child element
				RootCause g1 = new RootCause();
				g1.setXpath(xbi.getE1Test());
				Set<String> properties = getApplicableCSSProperties(xbi);
				g1.addExplicitProperties(properties);
				
				// add parent element
				RootCause g2 = new RootCause();
				g2.setXpath(xbi.getE2Test());
				g2.addExplicitProperties(properties);
				
				geneList.add(g1);
				geneList.add(g2);
			}
		}
		else if(Constants.SIBLING_XBIS.contains(xbi.getLabel()))
		{
			if(xbi.getE1Test() != null && xbi.getE2Test() != null)
			{
				// add first sibling
				RootCause g1 = new RootCause();
				g1.setXpath(xbi.getE1Test());
				Set<String> properties = getApplicableCSSProperties(xbi);
				g1.addExplicitProperties(properties);
				
				// add second sibling
				RootCause g2 = new RootCause();
				g2.setXpath(xbi.getE2Test());
				g2.addExplicitProperties(properties);
				
				geneList.add(g1);
				geneList.add(g2);
			}
			
			// add reference browser siblings that missing in test browser
			if(xbi.getLabel().equalsIgnoreCase("MISSING-SIBLING-2"))
			{
				RootCause g3 = new RootCause();
				g3.setXpath(xbi.getE1Ref());
				Set<String> properties3 = getApplicableCSSProperties(xbi);
				g3.addExplicitProperties(properties3);
				
				RootCause g4 = new RootCause();
				g4.setXpath(xbi.getE2Ref());
				g4.addExplicitProperties(properties3);
				
				geneList.add(g3);
				geneList.add(g4);
			}
		}
		return geneList;
	}
	
	private Set<String> getApplicableCSSProperties(XpertXbi xbi)
	{
		Set<String> cssProperties = new HashSet<>();
		
		// contains: vertical
		if(xbi.getLabel().equalsIgnoreCase("TOP-ALIGNMENT"))
		{
			cssProperties.add("margin-top");
			cssProperties.add("top");
		}
		else if(xbi.getLabel().equalsIgnoreCase("BOTTOM-ALIGNMENT"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("line-height");
			cssProperties.add("bottom");
			cssProperties.add("margin-top");
		}
		else if(xbi.getLabel().equalsIgnoreCase("VMID-ALIGNMENT"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("line-height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("margin-top");
			cssProperties.add("top");			
			cssProperties.add("padding-bottom");
			cssProperties.add("bottom");
			cssProperties.add("margin-bottom");
		}
		else if(xbi.getLabel().equalsIgnoreCase("VFILL"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("line-height");
		}
		
		// contains: horizontal
		else if(xbi.getLabel().equalsIgnoreCase("LEFT-JUSTIFICATION"))
		{
			cssProperties.add("margin-left");
			cssProperties.add("left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("RIGHT-JUSTIFICATION"))
		{
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("margin-left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("CENTER-ALIGNMENT"))
		{
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("margin-left");
			cssProperties.add("left");			
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("margin-right");
		}
		else if(xbi.getLabel().equalsIgnoreCase("HFILL"))
		{
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
		}
		
		// contains: other
		else if(xbi.getLabel().equalsIgnoreCase("PARENTS DIFFER"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("line-height");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
		}
		else if(xbi.getLabel().equalsIgnoreCase("MISSING-PARENT-1"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("line-height");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
		}
		else if(xbi.getLabel().equalsIgnoreCase("MISSING-PARENT-2"))
		{
			
		}
		
		// sibling: vertical
		else if(xbi.getLabel().equalsIgnoreCase("TOP-EDGE-ALIGNMENT"))
		{
			cssProperties.add("margin-top");
			cssProperties.add("top");
		}
		else if(xbi.getLabel().equalsIgnoreCase("BOTTOM-EDGE-ALIGNMENT"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("line-height");
			cssProperties.add("bottom");
			cssProperties.add("margin-top");
		}
		else if(xbi.getLabel().equalsIgnoreCase("TOP-BOTTOM"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("top");
			cssProperties.add("bottom");
			cssProperties.add("margin-top");
		}
		else if(xbi.getLabel().equalsIgnoreCase("BOTTOM-TOP"))
		{
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("top");
			cssProperties.add("bottom");
			cssProperties.add("margin-top");
		}
		
		// sibling: horizontal
		else if(xbi.getLabel().equalsIgnoreCase("LEFT-EDGE-ALIGNMENT"))
		{
			cssProperties.add("margin-left");
			cssProperties.add("left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("RIGHT-EDGE-ALIGNMENT"))
		{
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("margin-left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("LEFT-RIGHT"))
		{
			cssProperties.add("margin-left");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("RIGHT-LEFT"))
		{
			cssProperties.add("margin-left");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("left");
		}
		
		// sibling: other
		else if(xbi.getLabel().equalsIgnoreCase("MISSING-SIBLING-2"))
		{
			cssProperties.add("margin-top");
			cssProperties.add("top");
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("bottom");
			cssProperties.add("margin-left");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("left");
		}
		else if(xbi.getLabel().equalsIgnoreCase("MISSING-SIBLING-1"))
		{
			cssProperties.add("margin-top");
			cssProperties.add("top");
			cssProperties.add("padding-top");
			cssProperties.add("height");
			cssProperties.add("max-height");
			cssProperties.add("min-height");
			cssProperties.add("padding-bottom");
			cssProperties.add("bottom");
			cssProperties.add("margin-left");
			cssProperties.add("padding-left");
			cssProperties.add("width");
			cssProperties.add("max-width");
			cssProperties.add("min-width");
			cssProperties.add("padding-right");
			cssProperties.add("right");
			cssProperties.add("left");
		}
		
		return cssProperties;
	}
}
