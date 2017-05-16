package xfix;

import xfix.fitness.xbi.XbiFitnessFunction;

public class AVM
{
	private EvaluateRootCause evaluateGene;
	private long startTime;
	private String unit;

	public void AVMSearch(EvaluateRootCause evaluateGene, RootCause gene, String prop, String value)
	{
		unit = Util.getUnitFromStringValue(value);
		
		this.evaluateGene = evaluateGene;

		startTime = System.nanoTime();

		runAVM(evaluateGene, gene, prop, value);
	}

	public void runAVM(EvaluateRootCause evaluateGene, RootCause gene, String prop, String value)
	{
		String xpath = gene.getXpath();
		
		boolean improvement, exploratoryImprovement;
		do
		{
			improvement = false;
			do
			{
				exploratoryImprovement = false;
				for (int direction : Constants.EXPLORATORY_MOVES_ARR)
				{
					RootCause gene1 = evaluateGene.getChromosome().getGene(xpath);
					String value1 = gene1.getValue(prop);
					
					if (exploratoryMove(gene1, prop, value1, direction))
					{
						if (evaluateGene.getOptimalFitnessScore() == Constants.OPTIMAL_FITNESS_SCORE)
						{
							break;
						}

						improvement = true;
						exploratoryImprovement = true;

						// do pattern moves in the direction of improvement
						patternMove(gene1, prop, value1, direction);

						// break this cycle of exploratory moves to reestablish a new direction
						break;
					}

					// look up time budget
					int timeElapsedInSec = (int) Util.convertNanosecondsToSeconds(System.nanoTime() - startTime);
					if (gene.getTimeBudget(prop) > -1 && timeElapsedInSec > gene.getTimeBudget(prop))
					{
						System.out.println("---------------------------------------------------------------------------------------------");
						System.out.println("Terminating numeric search as the time budget of " + gene.getTimeBudget(prop) + " sec has elapsed.");
						improvement = false; // to exit out of the outer while
												// loop
						break;
					}
				}
				// look up time budget
				int timeElapsedInSec = (int) Util.convertNanosecondsToSeconds(System.nanoTime() - startTime);
				if (gene.getTimeBudget(prop) > -1 && timeElapsedInSec > gene.getTimeBudget(prop))
				{
					System.out.println("---------------------------------------------------------------------------------------------");
					System.out.println("Terminating numeric search as the time budget of " + gene.getTimeBudget(prop) + " sec has elapsed.");
					improvement = false; // to exit out of the outer while loop
					break;
				}
			} while (exploratoryImprovement);

			// look up time budget
			int timeElapsedInSec = (int) Util.convertNanosecondsToSeconds(System.nanoTime() - startTime);
			if (gene.getTimeBudget(prop) > -1 && timeElapsedInSec > gene.getTimeBudget(prop))
			{
				System.out.println("---------------------------------------------------------------------------------------------");
				System.out.println("Terminating numeric search as the time budget of " + gene.getTimeBudget(prop) + " sec has elapsed.");
				improvement = false; // to exit out of the outer while loop
				break;
			}
		} while (improvement);
	}

	private boolean exploratoryMove(RootCause gene, String prop, String value, int direction)
	{
		XbiFitnessFunction fitnessFunction = new XbiFitnessFunction();
		double currentFitnessScore = evaluateGene.getOptimalFitnessScore();
		String newValue = getNewValue(prop, value, direction);

		// store newValue in the chromosome
		gene.updateValue(prop, newValue);

		System.out.println("In exploratory move with (" + gene.getXpath() + ", " + prop + ", " + newValue + ", " + direction + ")");
		double newFitnessScore = fitnessFunction.getFitnessScoreLocal(evaluateGene.getChromosome(), gene);

		boolean improvement = (newFitnessScore < currentFitnessScore);
		System.out.println(improvement ? "IMPROVEMENT" : "NO IMPROVEMENT");

		// reset the value
		if (!improvement)
		{
			System.out.println("exploratory move resetting " + prop + " value = " + evaluateGene.getOptimalValue() + " and fitness score = " + evaluateGene.getOptimalFitnessScore());
			gene.updateValue(prop, evaluateGene.getOptimalValue());
			evaluateGene.getChromosome().setGlobalFitnessScore(evaluateGene.getOptimalFitnessScore());
		}
		else
		{
			evaluateGene.setOptimalValue(newValue);
			evaluateGene.setOptimalFitnessScore(newFitnessScore);
		}

		return improvement; // true == improvement
	}

	private void patternMove(RootCause gene, String prop, String value, int direction)
	{
		XbiFitnessFunction fitnessFunction = new XbiFitnessFunction();
		double currentFitnessScore = evaluateGene.getOptimalFitnessScore();
		boolean improvement = true;
		int k = Constants.PATTERN_BASE * direction;
		int step = 1;

		while (improvement)
		{
			value = gene.getValue(prop);

			String newValue = getNewValue(prop, value, k);
			// store newValue in the chromosome
			gene.updateValue(prop, newValue);

			System.out.println("In pattern move step " + step + " with (" + gene.getXpath() + ", " + prop + ", " + newValue + ", " + direction + ")");
			double newFitnessScore = fitnessFunction.getFitnessScoreLocal(evaluateGene.getChromosome(), gene);

			improvement = (newFitnessScore < currentFitnessScore);
			System.out.println(improvement ? "IMPROVEMENT" : "NO IMPROVEMENT");

			if (!improvement)
			{
				// reset with the last stored value
				System.out.println("pattern move resetting " + prop + " value = " + evaluateGene.getOptimalValue() + " and fitness score = " + evaluateGene.getOptimalFitnessScore());
				gene.updateValue(prop, evaluateGene.getOptimalValue());
				evaluateGene.getChromosome().setGlobalFitnessScore(evaluateGene.getOptimalFitnessScore());
			}
			else
			{
				k = k * Constants.PATTERN_BASE;
				evaluateGene.setOptimalValue(newValue);
				evaluateGene.setOptimalFitnessScore(newFitnessScore);
			}
			currentFitnessScore = newFitnessScore;

			if (newFitnessScore == Constants.OPTIMAL_FITNESS_SCORE)
			{
				break;
			}

			// look up time budget
			int timeElapsedInSec = (int) Util.convertNanosecondsToSeconds(System.nanoTime() - startTime);
			if (gene.getTimeBudget(prop) > -1 && timeElapsedInSec > gene.getTimeBudget(prop))
			{
				System.out.println("---------------------------------------------------------------------------------------------");
				System.out.println("Terminating numeric search as the time budget of " + gene.getTimeBudget(prop) + " sec has elapsed.");
				break;
			}
			step++;
		}
		System.out.println("total pattern move steps = " + step);
	}

	private String getNewValue(String property, String value, int valueDelta)
	{
		int intValue = Util.getNumbersFromString(value).get(0);
		int intNewValue = intValue + valueDelta;
		String newValue = intNewValue + unit;
		return newValue;
	}
}
