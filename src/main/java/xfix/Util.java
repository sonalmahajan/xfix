package xfix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Util
{
	public static List<Integer> getNumbersFromString(String string)
	{
		List<Integer> numbers = new ArrayList<Integer>();
		Pattern p = Pattern.compile("-?\\d+");
		Matcher m = p.matcher(string);
		while (m.find())
		{
			numbers.add(Integer.valueOf(m.group()));
		}
		return numbers;
	}
	
	public static String getUnitFromStringValue(String string)
	{
		Pattern p = Pattern.compile("[a-zA-Z%]+");
		Matcher m = p.matcher(string);
		String returnValue = "";
		while (m.find())
		{
			returnValue = m.group();
		}
		return returnValue;
	}
	
	public static double convertNanosecondsToSeconds(long time)
	{
		return (double) time / 1000000000.0;
	}
	
	public static Properties readPropertiesFile(String propertiesFilePath)
	{
		// Read properties file.
		File file = new File(propertiesFilePath);
		Properties properties = new Properties();
		try
		{
			properties.load(new FileInputStream(file));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return properties;
	}
	
	public static void applyNewValues(RootCauseList chromosome, WebDriver d)
	{
		for(RootCause gene : chromosome.getGenes())
		{
			// check which properties have updated values
			for(String prop : gene.getProcessedProperties())
			{
				String value = gene.getValue(prop);
				
				System.out.println("Applying " + prop + " = " + value + " to xpath = " + gene.getXpath());
				
				// modify test page with the new values
				try
				{
					WebElement e = d.findElement(By.xpath(getNormalizedXpath(gene.getXpath())));
					((JavascriptExecutor) d).executeScript("arguments[0].style[arguments[1]] = arguments[2];", e, prop, value);
				}
				catch(NoSuchElementException e)
				{
					System.err.println("Element " + gene.getXpath() + " not found in " + d.getCurrentUrl());
				}
			}
		}
	}
	
	public static String getNormalizedXpath(String xpath)
	{
		String normalizedXpath = xpath;
		
		String pattern1 = "(?<=/)([a-zA-Z0-9]*?)(?=/)";
		Pattern r1 = Pattern.compile(pattern1);
		Matcher m1 = r1.matcher(xpath);
		while (m1.find())
		{
			normalizedXpath = normalizedXpath.replaceFirst(pattern1, m1.group(0) + "[1]");
		}
		
		String pattern2 = "([a-zA-Z0-9]+?)$";
		Pattern r2 = Pattern.compile(pattern2);
		Matcher m2 = r2.matcher(xpath);
		if (m2.find())
		{
			normalizedXpath = normalizedXpath.replaceFirst(pattern2, m2.group(0) + "[1]");
		}
		
		return normalizedXpath;
	}
}
