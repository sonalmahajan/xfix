package xfix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RootCause implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String xpath;
	private Map<String, String> propValueMap;
	private Set<String> processedProperties;
	
	private Map<String, Integer> timeBudgetMap;	// <prop, % of time of earlier processing>
	private Map<String, Integer> timeRequiredForProcessingMap;	// <prop, time required for processing without any time budget>
	private Map<String, Integer> rankInFitnessScoreImprovement;
	
	private boolean isProcess;
	
	public RootCause()
	{
		this.propValueMap = new HashMap<String, String>();
		this.processedProperties = new HashSet<String>();
		
		this.timeBudgetMap = new HashMap<String, Integer>();
		this.timeRequiredForProcessingMap = new HashMap<String, Integer>();
		this.rankInFitnessScoreImprovement = new HashMap<String, Integer>();
	}
	
	public String getXpath()
	{
		return xpath;
	}
	
	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}
	
	public Map<String, String> getPropValueMap()
	{
		return propValueMap;
	}
	
	public void setPropValueMap(Map<String, String> propValueMap)
	{
		this.propValueMap = propValueMap;
	}
	
	public Set<String> getProcessedProperties()
	{
		return processedProperties;
	}

	public void setProcessedProperties(Set<String> processedProperties)
	{
		this.processedProperties = processedProperties;
	}

	public void setTimeBudgetMap(Map<String, Integer> timeBudgetMap)
	{
		this.timeBudgetMap = timeBudgetMap;
	}

	public void setTimeRequiredForProcessingMap(Map<String, Integer> timeRequiredForProcessingMap)
	{
		this.timeRequiredForProcessingMap = timeRequiredForProcessingMap;
	}

	public void setRankInFitnessScoreImprovement(Map<String, Integer> rankInFitnessScoreImprovement)
	{
		this.rankInFitnessScoreImprovement = rankInFitnessScoreImprovement;
	}
	
	public boolean isProcess()
	{
		return isProcess;
	}

	public void setProcess(boolean isProcess)
	{
		this.isProcess = isProcess;
	}

	public int getTimeBudget(String prop)
	{
		if(timeBudgetMap.get(prop) == null)
			return -1;
		else
			return timeBudgetMap.get(prop); 
	}

	public void addTimeBudget(String prop, int timePercentage)
	{
		int allowedTime = (int) ((double)(timePercentage * this.timeRequiredForProcessingMap.get(prop))/100.0);
		this.timeBudgetMap.put(prop, allowedTime);
	}

	public int getTimeRequiredForProcessing(String prop)
	{
		if(timeRequiredForProcessingMap.get(prop) == null)
			return -1;
		else
			return timeRequiredForProcessingMap.get(prop);
	}

	public void addTimeRequiredForProcessing(String prop, int timeInSec)
	{
		this.timeRequiredForProcessingMap.put(prop, timeInSec);
	}

	public int getRankInFitnessScoreImprovement(String prop)
	{
		if(rankInFitnessScoreImprovement.get(prop) == null)
			return -1;
		else
			return rankInFitnessScoreImprovement.get(prop);
	}

	public void addRankInFitnessScoreImprovement(String prop, int rank)
	{
		this.rankInFitnessScoreImprovement.put(prop, rank);
	}

	public void addProperty(String property, String value)
	{
		processedProperties.add(property);
		propValueMap.put(property, value);
	}
	
	public void removeProperty(String property)
	{
		processedProperties.remove(property);
		propValueMap.remove(property);
	}
	
	public void updateValue(String property, String value)
	{
		processedProperties.add(property);
		propValueMap.put(property, value);
	}
	
	public String getValue(String property)
	{
		return propValueMap.get(property);
	}
	
	public void clearProcessedProperties()
	{
		processedProperties.clear();
	}
	
	public RootCause copy()
	{
		RootCause g = new RootCause();
		g.setPropValueMap(new HashMap<String, String>(this.getPropValueMap()));
		g.setProcessedProperties(new HashSet<String>(this.getProcessedProperties()));
		g.xpath = this.xpath;
		
		g.setRankInFitnessScoreImprovement(this.rankInFitnessScoreImprovement);
		g.setTimeBudgetMap(this.timeBudgetMap);
		g.setTimeRequiredForProcessingMap(this.timeRequiredForProcessingMap);
		g.setProcess(this.isProcess);
		return g;
	}
	
	public void addExplicitProperties(Set<String> properties)
	{
		WebDriver d = WebDriverSingleton.getDriver(Constants.TEST_BROWSER);
		
		List<String> allProps = Constants.getAllProperties();
		for(String prop : properties)
		{
			if(!allProps.contains(prop))
			{
				continue;
			}
			
			// if property not already in the map, add it
			if(!this.propValueMap.containsKey(prop))
			{
				WebElement e = d.findElement(By.xpath(this.xpath));
				String value = e.getCssValue(prop);
				if(Constants.NUMERIC_POSITIVE_NEGATIVE_PROPERTIES.contains(prop) || 
						Constants.NUMERIC_POSITIVE_PROPERTIES.contains(prop))
				{
					if(value.equalsIgnoreCase("auto") || value.equalsIgnoreCase("none") || value.equalsIgnoreCase("normal"))
					{
						// set some default value, 0px
						value = "0px";
						
						if(prop.equalsIgnoreCase("max-width") || prop.equalsIgnoreCase("min-width"))
						{
							value = e.getSize().width + "px";
						}
						else if(prop.equalsIgnoreCase("max-height") || prop.equalsIgnoreCase("min-height"))
						{
							value = e.getSize().height + "px";
						}
						else if(prop.equalsIgnoreCase("line-height"))
						{
							value = "15px";
						}
					}
				}
				addProperty(prop, value);
			}
		}
		
		clearProcessedProperties();
	}

	@Override
	public String toString() {
		return "Gene [xpath=" + xpath + ", propValueMap=" + propValueMap + ", processedProperties="
				+ processedProperties + ", timeBudgetMap=" + timeBudgetMap + ", timeRequiredForProcessingMap="
				+ timeRequiredForProcessingMap + ", rankInFitnessScoreImprovement=" + rankInFitnessScoreImprovement
				+ ", isProcess=" + isProcess + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processedProperties == null) ? 0 : processedProperties.hashCode());
		result = prime * result + ((propValueMap == null) ? 0 : propValueMap.hashCode());
		result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RootCause other = (RootCause) obj;
		if (processedProperties == null)
		{
			if (other.processedProperties != null)
				return false;
		}
		else if (!processedProperties.equals(other.processedProperties))
			return false;
		if (propValueMap == null)
		{
			if (other.propValueMap != null)
				return false;
		}
		else if (!propValueMap.equals(other.propValueMap))
			return false;
		if (xpath == null)
		{
			if (other.xpath != null)
				return false;
		}
		else if (!xpath.equals(other.xpath))
			return false;
		return true;
	}
}
