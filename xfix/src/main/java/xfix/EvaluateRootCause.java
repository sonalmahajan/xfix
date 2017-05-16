package xfix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xfix.fitness.xbi.XbiFitnessFunction;

public class EvaluateRootCause
{
	private RootCauseList chromosome;
	private RootCauseList initialChromosome;
	
	private String optimalValue;
	private double optimalFitnessScore;
	
	private Map<String, List<OptimalRootCause>> optimalMap;	// <xpath, <prop, val, fitnessScore>>
	
	private boolean isSiblingAdjustment;
	private RootCauseList initialChromosomeWithoutSiblingAdjustment;
	
	public EvaluateRootCause(RootCauseList initialChromosomeWithoutSiblingAdjustment, RootCauseList chromosome, boolean isSiblingAdjustment)
	{
		this.chromosome = chromosome.copy();
		this.initialChromosome = chromosome.copy();
		this.optimalMap = new HashMap<String, List<OptimalRootCause>>();
		this.isSiblingAdjustment = isSiblingAdjustment;
		this.initialChromosomeWithoutSiblingAdjustment = initialChromosomeWithoutSiblingAdjustment;
	}
	
	public RootCauseList getChromosome()
	{
		return chromosome;
	}

	public void setChromosome(RootCauseList chromosome)
	{
		this.chromosome = chromosome;
	}

	public RootCauseList getInitialChromosome()
	{
		return initialChromosome;
	}

	public void setInitialChromosome(RootCauseList initialChromosome)
	{
		this.initialChromosome = initialChromosome;
	}

	public String getOptimalValue()
	{
		return optimalValue;
	}

	public void setOptimalValue(String optimalValue)
	{
		this.optimalValue = optimalValue;
	}

	public double getOptimalFitnessScore()
	{
		return optimalFitnessScore;
	}

	public void setOptimalFitnessScore(double optimalFitnessScore)
	{
		this.optimalFitnessScore = optimalFitnessScore;
	}

	public Map<String, List<OptimalRootCause>> getOptimalMap()
	{
		return optimalMap;
	}

	public void setOptimalMap(Map<String, List<OptimalRootCause>> optimalMap)
	{
		this.optimalMap = optimalMap;
	}

	public void runGeneSearch(RootCause gene, int geneCount)
	{
		if(!gene.isProcess())
		{
			System.out.println("no process: " + gene.getXpath() + " -> " + gene.isProcess());
			return;
		}
		
		//optimalFitnessScore = initialChromosome.getFitnessScore();
		XbiFitnessFunction fitnessFunction = new XbiFitnessFunction();
		optimalFitnessScore = fitnessFunction.getFitnessScoreLocal(initialChromosome, gene);
		
		double initialFitnessScoreWithoutSiblingAdjustment = 0.0;
		if(isSiblingAdjustment)
		{
			// initial fitness score 
			initialFitnessScoreWithoutSiblingAdjustment = fitnessFunction.getFitnessScoreLocal(initialChromosomeWithoutSiblingAdjustment, gene);
			System.out.println("Evaluate gene " + gene.getXpath() +" initial fitness score without sibling adjustment = " + initialFitnessScoreWithoutSiblingAdjustment);
		}
		
		double initialFitnessScore = optimalFitnessScore;
		System.out.println("Evaluate gene " + gene.getXpath() +" initial fitness score = " + initialFitnessScore);
		
		int rootCauseCount = 1;
		for(String prop : gene.getPropValueMap().keySet())
		{
			if(gene.getRankInFitnessScoreImprovement(prop) == -2)	// ignore the property as it is (soft) removed from the gene
			{
				System.out.println("\n--------------------------------- skipping -------------------------------------------");
				System.out.println("++++++ Gen" + XbiMainIterator.getGeneration() +", Gene " + geneCount +" of " + initialChromosome.getGenes().size() +": start processing RC <" + gene.getXpath() + ", " + prop + ", " + gene.getValue(prop) +"> "
						+ "(" + (rootCauseCount++) + " of " + gene.getPropValueMap().size() + ") ++++++");
				System.out.println("++++++ rank = " + gene.getRankInFitnessScoreImprovement(prop) + ", time budget = " + gene.getTimeBudget(prop) + 
						" sec, time reqd for first run = " + gene.getTimeRequiredForProcessing(prop) + " sec ++++++");
				System.out.println("---------------------------------------------------------------------------------------------");
				continue;
			}
			
			System.out.println("\n---------------------------------------------------------------------------------------------");
			System.out.println("++++++ Gen" + XbiMainIterator.getGeneration() +", Gene " + geneCount +" of " + initialChromosome.getGenes().size() +": start processing RC <" + gene.getXpath() + ", " + prop + ", " + gene.getValue(prop) +"> "
					+ "(" + (rootCauseCount++) + " of " + gene.getPropValueMap().size() + ") ++++++");
			System.out.println("++++++ rank = " + gene.getRankInFitnessScoreImprovement(prop) + ", time budget = " + gene.getTimeBudget(prop) + 
					" sec, time reqd for first run = " + gene.getTimeRequiredForProcessing(prop) + " sec ++++++");
			System.out.println("---------------------------------------------------------------------------------------------");
			long startTime = System.nanoTime();
			
			String value = gene.getValue(prop);

			chromosome = initialChromosome.copy();
			//optimalFitnessScore = initialChromosome.getFitnessScore();
			optimalFitnessScore = initialFitnessScore;
			
			//optimalFitnessScore = chromosome.getFitnessScore();
			optimalValue = value;

			AVM na = new AVM();
			na.AVMSearch(this, chromosome.getGene(gene.getXpath()), prop, value);
			
			// update optimal map
			long endTime = System.nanoTime();
			
			List<OptimalRootCause> oGeneList = new ArrayList<OptimalRootCause>();
			if(optimalMap.containsKey(gene.getXpath()))
			{
				oGeneList = optimalMap.get(gene.getXpath());
			}

			// if sibling adjustment, use the actual initial fitness score
			if(isSiblingAdjustment)
			{
				initialFitnessScore = initialFitnessScoreWithoutSiblingAdjustment;
			}
			
			double initialGlobalFitnessScore = initialChromosome.getGlobalFitnessScore();
			double globalFitnessScore = initialChromosome.getGlobalFitnessScore();
			boolean isImproved = false;
			//if(optimalFitnessScore < initialFitnessScore)
			// improvement is greater than 1%
			if(((initialFitnessScore - optimalFitnessScore) / initialFitnessScore) > 0.01)
			{
				RootCauseList copy = initialChromosome.copy();
				copy.getGene(gene.getXpath()).updateValue(prop, optimalValue);
				globalFitnessScore = fitnessFunction.getFitnessScoreGlobal(copy);
				System.out.println("Initial global fitness score = " + initialGlobalFitnessScore);
				System.out.println("New global fitness score = " + globalFitnessScore);
				isImproved = true;
			}
			
			OptimalRootCause oGene = new OptimalRootCause(gene.getXpath(), prop, optimalValue, optimalFitnessScore, initialFitnessScore, 
					(int) Math.ceil(Util.convertNanosecondsToSeconds(endTime - startTime)), isImproved, 
					initialGlobalFitnessScore, globalFitnessScore);
			oGeneList.add(oGene);
			optimalMap.put(gene.getXpath(), oGeneList);
			
			System.out.println("++++++ time required for processing RC <" + gene.getXpath() + ", " + prop + "> = " + Util.convertNanosecondsToSeconds(endTime - startTime) + " sec ++++++");
			System.out.println("---------------------------------------------------------------------------------------------\n");
			
			if(optimalFitnessScore == Constants.OPTIMAL_FITNESS_SCORE || globalFitnessScore == Constants.OPTIMAL_FITNESS_SCORE)
			{
				break;
			}
		}
	}
}