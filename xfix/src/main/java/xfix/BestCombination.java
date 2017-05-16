package xfix;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xfix.fitness.xbi.XbiFitnessFunction;

public class BestCombination
{
	private RootCauseList realChromosome;
	private List<OptimalRootCause> candidateRootCausesChromosome;
	private String optimalSolution;
	private Map<BigInteger, Double> chromosomeFitnessScoreCache;	// <decimal representation of binary string chromosome, fitness score>
	
	private static int MAX_GENERATIONS = 50;
	private static int BINARY_STRING_LENGTH = 16;
	
	public String getOptimalSolution()
	{
		return optimalSolution;
	}

	public Map<BigInteger, Double> getChromosomeFitnessScoreCache() 
	{
		return chromosomeFitnessScoreCache;
	}

	public BestCombination(RootCauseList realChromosome, List<OptimalRootCause> candidateRootCausesChromosome)
	{
		this.chromosomeFitnessScoreCache = new HashMap<>();
		
		this.realChromosome = realChromosome;
		this.candidateRootCausesChromosome = candidateRootCausesChromosome;
		BINARY_STRING_LENGTH = this.candidateRootCausesChromosome.size();
		MAX_GENERATIONS = Math.min((int) (Math.pow(2, BINARY_STRING_LENGTH) - 1), 50);
	}
	
	public BigInteger getDecimalValueForBinaryString(String bin)
	{
		//return BigInteger.parseBigInteger(bin, 2);
		return new BigInteger(bin, 2);
	}
	
	public String getBinaryStringForDecimalValue(BigInteger dec)
	{
		return String.format("%" + BINARY_STRING_LENGTH + "s", dec.toString(2)).replace(' ', '0');
	}
	
	public double fitnessFunction(String binaryChromosome)
	{
		BigInteger decimalValue = getDecimalValueForBinaryString(binaryChromosome);
		if(chromosomeFitnessScoreCache.containsKey(decimalValue))
		{
			System.out.println("Fitness for binary chromosome (cached) = " + binaryChromosome + " = " + chromosomeFitnessScoreCache.get(decimalValue));
			return chromosomeFitnessScoreCache.get(decimalValue);
		}
		// calculate fitness score
		XbiFitnessFunction xff = new XbiFitnessFunction();
		double fitnessScore = xff.getFitnessScoreBestCombination(realChromosome, candidateRootCausesChromosome, binaryChromosome);
		chromosomeFitnessScoreCache.put(decimalValue, fitnessScore);
		System.out.println("Fitness for binary chromosome = " + binaryChromosome + " = " + fitnessScore);
		return fitnessScore;
	}
	
	public void runRandomSearch(double initialFitnessScore)
	{
		System.out.println("Random search");
		
		char[] allOffBinaryChars = new char [BINARY_STRING_LENGTH];
		Arrays.fill(allOffBinaryChars, '0');
		optimalSolution = new String(allOffBinaryChars);
		
		int iterationCount = 0;
		int saturationCount = 0;
		double prevIterationFitnessScore = initialFitnessScore;
		double minFitnessScore = initialFitnessScore;
		int minFitnessScoreFoundInIteration = 1;
		
		// check if perfect solution has already been found
		if(initialFitnessScore == 0.0)
			return;
		
		while(true)
		{
			System.out.println("Random Search iteration " + (iterationCount + 1));
			
			// get random candidate solution
			String candidate = "";
			if(iterationCount == 0)
			{
				// set binary string with all bits on
				char[] allOnBinaryChars = new char [BINARY_STRING_LENGTH];
				Arrays.fill(allOnBinaryChars, '1');
				candidate = new String(allOnBinaryChars);
			}
			else
			{
				candidate = runRouletteWheelToGetBinaryString();
				if(candidate.isEmpty())
				{
					break;
				}
			}
			
			// check if the candidate meets the required criteria
			double fitnessScore = fitnessFunction(candidate);
			if(fitnessScore < minFitnessScore)
			{
				minFitnessScore = fitnessScore;
				optimalSolution = candidate;
				minFitnessScoreFoundInIteration = iterationCount;
			}
			if(fitnessScore == minFitnessScore)
			{
				// break the ties by selecting binary string representing higher local fitness score improvement
				double totalImprovementOptimalSolution = 0.0;
				double totalImprovementCandidate = 0.0;
				int index = 0;
				for(OptimalRootCause og : candidateRootCausesChromosome)
				{
					if(optimalSolution.charAt(index) == '1')
					{
						totalImprovementOptimalSolution = totalImprovementOptimalSolution + og.getFitnessScoreImprovement();
					}
					if(candidate.charAt(index) == '1')
					{
						totalImprovementCandidate = totalImprovementCandidate + og.getFitnessScoreImprovement();
					}
					index++;
				}
				if(totalImprovementCandidate > totalImprovementOptimalSolution)
				{
					optimalSolution = candidate;
				}
			}
			
			if(prevIterationFitnessScore == fitnessScore)
			{
				saturationCount++;
			}
			else
			{
				saturationCount = 0;
			}
			prevIterationFitnessScore = fitnessScore;
			
			iterationCount++;
			
			// termination conditions
			if(fitnessScore == Constants.OPTIMAL_FITNESS_SCORE || saturationCount >= Constants.SATURATION_POINT || iterationCount >= MAX_GENERATIONS)
			{
				break;
			}
		}
		
		System.out.println("\nOptimal solution found in best combo iteration " + iterationCount);
		System.out.println("Optimal solution first occured in best combo iteration " + minFitnessScoreFoundInIteration);
		System.out.print("Reason = ");
		if(saturationCount >= Constants.SATURATION_POINT)
		{
			System.out.println("Saturation point (" + Constants.SATURATION_POINT + ") reached");
		}
		else if(minFitnessScore == Constants.OPTIMAL_FITNESS_SCORE)
		{
			System.out.println("Optimal fitness score (" + Constants.OPTIMAL_FITNESS_SCORE + ") found");
		}
		else
		{
			System.out.println("Max generations (" + MAX_GENERATIONS + ") reached");
		}
	}
	
	private String runRouletteWheelToGetBinaryString()
	{
		int n = BINARY_STRING_LENGTH;
		double [] weight = new double [n];
		double max_weight = Double.MIN_VALUE;
		int cnt = 0;
		for(OptimalRootCause og : candidateRootCausesChromosome)
		{
			weight[cnt] = og.getFitnessScoreImprovement();
			if(weight[cnt] > max_weight)
			{
				max_weight = weight[cnt];
			}
			cnt++;
		}
		
		boolean isUnique = false;
		int UNIQUE_STRING_MAX_TRIES = 10;
		int uniqueTriesCnt = 0;
		String binaryString = "";
		do
		{
			char[] binaryChars = new char [n];
			Arrays.fill(binaryChars, '0');
			int index = 0;
			boolean notaccepted;
			for (int j = 0; j < n; j++)
			{
				notaccepted = true;
				while (notaccepted)
				{
					index = (int) (n * Math.random());
					if (Math.random() < weight[index] / max_weight)
					{
						notaccepted = false;
					}
				}
				binaryChars[index] = '1';
			}
			binaryString = new String(binaryChars);
			if(!chromosomeFitnessScoreCache.containsKey(getDecimalValueForBinaryString(binaryString)))
			{
				isUnique = true;
			}
			// powerset --> all possible candidates visited
			else if(chromosomeFitnessScoreCache.size() >= (int) (Math.pow(2, BINARY_STRING_LENGTH) - 1))
			{
				System.out.println("Visited all possible binary strings");
				binaryString = "";
				isUnique = true;
				break;
			}
			else if(uniqueTriesCnt >= UNIQUE_STRING_MAX_TRIES)
			{
				System.out.println("All unique tries exhausted");
				isUnique = true;
				break;
			}
			uniqueTriesCnt++;
		}
		while(!isUnique);
		
		return binaryString;
	}
	
	public void findBestCombination(double initialFitnessScore)
	{
		//runGA();
		runRandomSearch(initialFitnessScore);
	}
}