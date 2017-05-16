package xfix;

public class OptimalRootCause implements Comparable<OptimalRootCause>
{
	private String xpath;
	private String prop;
	private String val;
	private double localFitnessScore;
	private double initialLocalFitnessScore;
	private int processingTime;
	private boolean isShowingLocalFitnessScoreImprovement;
	private double fitnessScoreImprovement;
	private double globalFitnessScore;
	private double initialGlobalFitnessScore;

	public OptimalRootCause(String xpath, String prop, String val, double localFitnessScore, double initialLocalFitnessScore, 
			int processingTime, boolean isShowingLocalFitnessScoreImprovement, double initialGlobalFitnessScore, double globalFitnessScore)
	{
		this.xpath = xpath;
		this.prop = prop;
		this.val = val;
		this.localFitnessScore = localFitnessScore;
		this.initialLocalFitnessScore = initialLocalFitnessScore;
		this.processingTime = processingTime;
		this.isShowingLocalFitnessScoreImprovement = isShowingLocalFitnessScoreImprovement;
		this.globalFitnessScore = globalFitnessScore;
		this.initialGlobalFitnessScore = initialGlobalFitnessScore;
		
		// calculate fitness score improvement value
		double localFitnessImprovement = (initialLocalFitnessScore - localFitnessScore) / initialLocalFitnessScore;
		this.fitnessScoreImprovement = localFitnessImprovement;
	}
	
	public String getXpath()
	{
		return xpath;
	}

	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}

	public String getProp()
	{
		return prop;
	}

	public void setProp(String prop)
	{
		this.prop = prop;
	}

	public String getVal()
	{
		return val;
	}

	public void setVal(String val)
	{
		this.val = val;
	}

	public double getFitnessScore()
	{
		return localFitnessScore;
	}

	public void setFitnessScore(double fitnessScore)
	{
		this.localFitnessScore = fitnessScore;
	}

	public double getInitialFitnessScore()
	{
		return initialLocalFitnessScore;
	}

	public void setInitialFitnessScore(double initialFitnessScore)
	{
		this.initialLocalFitnessScore = initialFitnessScore;
	}

	public int getProcessingTime()
	{
		return processingTime;
	}

	public void setProcessingTime(int processingTime)
	{
		this.processingTime = processingTime;
	}

	public boolean isShowingFitnessScoreImprovement()
	{
		return isShowingLocalFitnessScoreImprovement;
	}

	public void setShowingFitnessScoreImprovement(boolean isShowingFitnessScoreImprovement)
	{
		this.isShowingLocalFitnessScoreImprovement = isShowingFitnessScoreImprovement;
	}

	public double getFitnessScoreImprovement()
	{
		return fitnessScoreImprovement;
	}

	public void setFitnessScoreImprovement(double fitnessScoreImprovement)
	{
		this.fitnessScoreImprovement = fitnessScoreImprovement;
	}

	public double getLocalFitnessScore()
	{
		return localFitnessScore;
	}

	public void setLocalFitnessScore(double localFitnessScore)
	{
		this.localFitnessScore = localFitnessScore;
	}

	public double getInitialLocalFitnessScore()
	{
		return initialLocalFitnessScore;
	}

	public void setInitialLocalFitnessScore(double initialLocalFitnessScore)
	{
		this.initialLocalFitnessScore = initialLocalFitnessScore;
	}

	public boolean isShowingLocalFitnessScoreImprovement()
	{
		return isShowingLocalFitnessScoreImprovement;
	}

	public void setShowingLocalFitnessScoreImprovement(boolean isShowingLocalFitnessScoreImprovement)
	{
		this.isShowingLocalFitnessScoreImprovement = isShowingLocalFitnessScoreImprovement;
	}

	public double getGlobalFitnessScore()
	{
		return globalFitnessScore;
	}

	public void setGlobalFitnessScore(double globalFitnessScore)
	{
		this.globalFitnessScore = globalFitnessScore;
	}

	public double getInitialGlobalFitnessScore()
	{
		return initialGlobalFitnessScore;
	}

	public void setInitialGlobalFitnessScore(double initialGlobalFitnessScore)
	{
		this.initialGlobalFitnessScore = initialGlobalFitnessScore;
	}

	public int compareTo(OptimalRootCause other) 
	{
		return new Double(localFitnessScore).compareTo(other.localFitnessScore);
    }
	
	@Override
	public String toString()
	{
		return "<" + xpath + ", " + prop + ", " + val + ", " + localFitnessScore + ", " + initialLocalFitnessScore + ", " + (isShowingLocalFitnessScoreImprovement ? "improvement" : "no improvement") + ", " + fitnessScoreImprovement + ">";
	}
}