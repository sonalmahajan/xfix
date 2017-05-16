package xfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XbiSearch 
{
	private RootCauseList chromosome;
	private RootCauseList initialChromosome;
	private RootCauseList optimalChromosome;
	
	private String optimalValue;
	private double optimalFitnessScore;
	
	private Map<String, List<OptimalRootCause>> optimalMap;	// <xpath, <prop, val, fitnessScore>>
	
	private static double phase1TimeInSec;
	private static double phase2TimeInSec;
	
	public XbiSearch(RootCauseList chromosome)
	{
		this.chromosome = chromosome.copy();
		this.initialChromosome = chromosome.copy();
		this.optimalMap = new HashMap<String, List<OptimalRootCause>>();
		this.optimalChromosome = chromosome;
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

	public RootCauseList getOptimalChromosome()
	{
		return optimalChromosome;
	}

	public void setOptimalChromosome(RootCauseList optimalChromosome)
	{
		this.optimalChromosome = optimalChromosome;
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
	public static double getPhase1TimeInSec() 
	{
		return phase1TimeInSec;
	}
	public static double getPhase2TimeInSec() 
	{
		return phase2TimeInSec;
	}
	public static void setPhase1TimeInSec(double phase1TimeInSec) {
		XbiSearch.phase1TimeInSec = phase1TimeInSec;
	}

	public static void setPhase2TimeInSec(double phase2TimeInSec) {
		XbiSearch.phase2TimeInSec = phase2TimeInSec;
	}

	public void search()
	{
		optimalFitnessScore = initialChromosome.getGlobalFitnessScore();
		
		long startTime = System.nanoTime();
		processRootCauses();
		long endTime = System.nanoTime();
		System.out.println("\nTotal gene processing (phase 1) time = " + Util.convertNanosecondsToSeconds((endTime - startTime)) + " sec");
		phase1TimeInSec = phase1TimeInSec + Util.convertNanosecondsToSeconds((endTime - startTime));
		
		startTime = System.nanoTime();
		runBestRootCauseCombination();
		endTime = System.nanoTime();
		System.out.println("\nTotal best root cause computation (phase 2) time = " + Util.convertNanosecondsToSeconds((endTime - startTime)) + " sec");
		phase2TimeInSec = phase2TimeInSec + Util.convertNanosecondsToSeconds((endTime - startTime));
		
		System.out.println("\n------------------------------------------------------");
		System.out.println("Computing fitness function of optimal chromosome");
		/*XbiFitnessFunction fitnessFunction = new XbiFitnessFunction();
		fitnessFunction.getFitnessScoreGlobal(optimalChromosome);*/
		System.out.println("Global fitness score = " + optimalChromosome.getGlobalFitnessScore());
		System.out.println("------------------------------------------------------");
		System.out.println("\noptimal map = " + optimalMap);
		System.out.println("\noptimal chromosome = " + optimalChromosome);
	}

	public void runBestRootCauseCombination()
	{
		// ----------------------- COMPUTE BEST ROOT CAUSE COMBO ACROSS ALL GENES ------------------------- //
		System.out.println("\n############################### COMPUTE BEST ROOT CAUSE COMBO ACROSS ALL GENES ###############################");
		
		// get list of candidate root causes
		List<OptimalRootCause> candidateRootCauses = getCandidateRootCauses();
		System.out.println("Candidate root causes = {");
		for(OptimalRootCause og : candidateRootCauses)
		{
			System.out.println("\t" + og);
		}
		System.out.println("}");
		System.out.println("Number of candidate root causes = " + candidateRootCauses.size());
		
		// run genetic algorithm to get best root cause combination
		if(candidateRootCauses.size() > 0)
		{
			char[] binaryStringArr = new char[candidateRootCauses.size()];
			Arrays.fill(binaryStringArr, '0');
			String optimalBinaryChromosome = new String(binaryStringArr);
			double globalFitnessScore = initialChromosome.getGlobalFitnessScore();
			
			// get best gene from individual evaluation
			double minGlobalFitnessScore = globalFitnessScore;
			OptimalRootCause bestCandidateSoFar = candidateRootCauses.get(0);
			int cnt = 0;
			for(OptimalRootCause og : candidateRootCauses)
			{
				if(og.getGlobalFitnessScore() < minGlobalFitnessScore)
				{
					minGlobalFitnessScore = og.getGlobalFitnessScore();
					Arrays.fill(binaryStringArr, '0');
					binaryStringArr[cnt] = '1';
					optimalBinaryChromosome = new String(binaryStringArr);
					bestCandidateSoFar = og;
				}
				// break the tie
				else if(og.getGlobalFitnessScore() == minGlobalFitnessScore)
				{
					// check if current candidate has better local fitness improvement than the best so far
					if(og.getFitnessScoreImprovement() > bestCandidateSoFar.getFitnessScoreImprovement())
					{
						minGlobalFitnessScore = og.getGlobalFitnessScore();
						Arrays.fill(binaryStringArr, '0');
						binaryStringArr[cnt] = '1';
						optimalBinaryChromosome = new String(binaryStringArr);
						bestCandidateSoFar = og;
					}
				}
				cnt++;
			}
			
			System.out.println("Initial binary string = " + optimalBinaryChromosome);
			System.out.println("Initial fitness score = " + minGlobalFitnessScore);
			
			BestCombination bc = new BestCombination(initialChromosome, candidateRootCauses);
			bc.findBestCombination(minGlobalFitnessScore);
			String optimalBinaryChromosomeBestCombo = bc.getOptimalSolution();
			if(optimalBinaryChromosomeBestCombo != null && !optimalBinaryChromosomeBestCombo.isEmpty() && optimalBinaryChromosomeBestCombo.contains("1"))
			{
				// best combination found a better solution
				optimalBinaryChromosome = optimalBinaryChromosomeBestCombo;
				globalFitnessScore = bc.fitnessFunction(optimalBinaryChromosome);
			}
			else
			{
				globalFitnessScore = minGlobalFitnessScore;
			}
			System.out.println("Optimal binary chromosome = " + optimalBinaryChromosome);
			
			// get optimal chromosome
			int i = 0;
			for(OptimalRootCause og : candidateRootCauses)
			{
				if(optimalBinaryChromosome.charAt(i) == '1')
				{
					// set value in chromosome
					RootCause gene = optimalChromosome.getGene(og.getXpath());
					gene.updateValue(og.getProp(), og.getVal());
				}
				i++;
			}
			optimalChromosome.setGlobalFitnessScore(globalFitnessScore);
		}
		
		System.out.println("-----------------------------------------------------------------");
		
	}
	
	public List<OptimalRootCause> getCandidateRootCauses()
	{
		List<OptimalRootCause> candidateRootCausesList = new ArrayList<OptimalRootCause>();
		System.out.println("optimal map = " + optimalMap);
		for(String xpath : optimalMap.keySet())
		{
			// process root causes for rank and time budget
			int rank = 0;
			double prevFitnessScore = -1;
			RootCause g = optimalChromosome.getGene(xpath);
			for(OptimalRootCause og : optimalMap.get(xpath))
			{
				if(prevFitnessScore != og.getFitnessScore())
				{
					prevFitnessScore = og.getFitnessScore();
					rank++;
				}
				
				// add rank
				if(og.isShowingFitnessScoreImprovement())
				{
					g.addRankInFitnessScoreImprovement(og.getProp(), rank);
					
					// add root causes that show fitness score improvement
					candidateRootCausesList.add(og);
				}
				else
				{
					g.addRankInFitnessScoreImprovement(og.getProp(), -2);
				}
				
				// if property in the list of processed properties, then always allow it to run with rank = 1
				if(g.getProcessedProperties().contains(og.getProp()))
				{
					g.addRankInFitnessScoreImprovement(og.getProp(), 1);
				}
				
				// add processing time
				if(g.getTimeRequiredForProcessing(og.getProp()) == -1)
				{
					g.addTimeRequiredForProcessing(og.getProp(), og.getProcessingTime());
				}
			}
			// update time budget
			/*for(String prop : g.getPropValueMap().keySet())
			{
				if(g.getRankInFitnessScoreImprovement(prop) != -1)
				{
					g.addTimeBudget(prop, computeTimeBudgetPercentage(rank, g.getPropValueMap().size(), g.getRankInFitnessScoreImprovement(prop)));
				}
			}*/
		}
		
		return candidateRootCausesList;
	}

	public void processRootCauses()
	{
		int geneCount = 0;
		optimalFitnessScore = initialChromosome.getGlobalFitnessScore();
		
		for(RootCause gene : initialChromosome.getGenes())
		{
			geneCount++;
			RootCauseList tempChromosome = initialChromosome.copy();
			RootCause tempGene = tempChromosome.getGene(gene.getXpath());

			// check if optimalMap contains sibling of the gene
			boolean siblingFound = false;
			System.out.println("Now processing gene " + gene.getXpath());
			EvaluateRootCause evaluateGene = new EvaluateRootCause(initialChromosome, tempChromosome, siblingFound);
			evaluateGene.runGeneSearch(tempGene, geneCount);
			
			optimalMap.putAll(evaluateGene.getOptimalMap());
			if(optimalFitnessScore == Constants.OPTIMAL_FITNESS_SCORE)
			{
				break;
			}
		}
	}
}
