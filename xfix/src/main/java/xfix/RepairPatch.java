package xfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xfix.WebDriverSingleton.Browser;
import xfix.fitness.xbi.XbiUtil;

public class RepairPatch
{
	private static String firefoxSelectorPrefix = "@-moz-document url-prefix()"; // _:-moz-fullscreen, :root .selector 
	private static String ieSelectorPrefix = "@media screen and (-ms-high-contrast: active), (-ms-high-contrast: none)";  // _:-ms-fullscreen, :root .selector
	private static String chromeSelectorPrefix = "@supports (-webkit-appearance:none)"; // @media all and (-webkit-min-device-pixel-ratio:0) and (min-resolution: .001dpcm) { .selector {} }
	
	public String convertXpathToCssSelector(String xpath)
	{
		// Example: xpath: /html/body/div/div/div[3]/ul/li[4] 
		// CSS selector:   html > body > div:nth-of-type(1) > div:nth-of-type(1) > div:nth-of-type(3) > ul:nth-of-type(1) > li:nth-of-type(4)
		
		String cssSelector = "";
		String[] xpathArr = xpath.split("/");
		for(int i = 1; i < xpathArr.length; i++)
		{
			String selectorPiece = "";
			if(xpathArr[i].contains("["))
			{
				int index = xpathArr[i].indexOf("[");
				Pattern pattern = Pattern.compile("\\[(.*?)\\]");
				Matcher matcher = pattern.matcher(xpathArr[i]);
				String siblingIndex = "1";
				if (matcher.find()) 
				{
					siblingIndex = matcher.group(1);
				}
				selectorPiece = xpathArr[i].substring(0, index) + ":nth-of-type(" + siblingIndex + ")";
			}
			else
			{
				if(xpathArr[i].equalsIgnoreCase("html") || xpathArr[i].equalsIgnoreCase("body"))
					selectorPiece = xpathArr[i];
				else
					selectorPiece = xpathArr[i] + ":nth-of-type(1)";
			}
			cssSelector = cssSelector + ((!cssSelector.isEmpty()) ? " > " : "") + selectorPiece;
		}
		return cssSelector;
	}
	
	public String getRepairPatch(RootCauseList optimalChromosome)
	{
		// build fixes to be applied
		String fixes = "";
		for(RootCause gene : optimalChromosome.getGenes())
		{
			if(!gene.getProcessedProperties().isEmpty())
			{
				fixes = fixes + "\n\t" + convertXpathToCssSelector(gene.getXpath()) + " {";
				for(String prop : gene.getProcessedProperties())
				{
					String value = gene.getValue(prop);
					String relValue = XbiUtil.convertAbsoluteToRelativeValue(gene.getXpath(), prop, value);
					fixes = fixes + "\n\t\t" + prop + ": " + relValue + " !important;  /* " + value + " */";
				}
				fixes = fixes + "\n\t}";
			}
		}
		
		// create browser specific repair patch
		String repairPatch = "";
		if(Constants.TEST_BROWSER == Browser.FIREFOX)
		{
			repairPatch = firefoxSelectorPrefix + " {";
			repairPatch = repairPatch + fixes;
			repairPatch = repairPatch + "\n}";
		}
		else if(Constants.TEST_BROWSER == Browser.CHROME)
		{
			repairPatch = chromeSelectorPrefix + " {";
			repairPatch = repairPatch + fixes;
			repairPatch = repairPatch + "\n}";
		}
		else // INTERNET_EXPLORER
		{
			repairPatch = ieSelectorPrefix + " {";
			repairPatch = repairPatch + fixes;
			repairPatch = repairPatch + "\n}";
		}
		return repairPatch;
	}
}
