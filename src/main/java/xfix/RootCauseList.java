package xfix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RootCauseList implements Comparable<RootCauseList>
{
	private static final AtomicInteger count = new AtomicInteger(0); 
	private int id;
	private List<RootCause> genes;
	private double globalFitnessScore;

	public RootCauseList()
	{
		this.genes = new ArrayList<RootCause>();
		this.globalFitnessScore = Constants.INITIAL_FITNESS_SCORE;
		this.id = count.incrementAndGet();
	}
	
	public RootCauseList(List<RootCause> genes)
	{
		this.genes = genes;
		this.globalFitnessScore = Constants.INITIAL_FITNESS_SCORE;
		this.id = count.incrementAndGet();
	}

	public List<RootCause> getGenes()
	{
		return genes;
	}

	public void setGenes(List<RootCause> genes)
	{
		this.genes = genes;
	}

	public double getGlobalFitnessScore()
	{
		return globalFitnessScore;
	}

	public void setGlobalFitnessScore(double globalFitnessScore)
	{
		this.globalFitnessScore = globalFitnessScore;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void addGene(RootCause gene)
	{
		genes.add(gene);
	}
	
	public RootCause getGene(String xpath)
	{
		for(RootCause g : genes)
		{
			if(g.getXpath().equalsIgnoreCase(xpath))
			{
				return g;
			}
		}
		return null;
	}
	
	public RootCauseList copy()
	{
		RootCauseList c = new RootCauseList();
		List<RootCause> cg = new ArrayList<RootCause>();
		for(RootCause g : this.getGenes())
		{
			cg.add(g.copy());
		}
		c.setGenes(cg);
		c.setGlobalFitnessScore(this.getGlobalFitnessScore());
		return c;
	}
	
	public void replaceGene(RootCause oldGene, RootCause newGene)
	{
		int index = genes.indexOf(oldGene);
		genes.remove(index);
		genes.add(index, newGene);
	}
	
	public int compareTo(RootCauseList other) 
	{
        if (globalFitnessScore > other.globalFitnessScore) 
        {
            return 1;
        } 
        else if (globalFitnessScore < other.globalFitnessScore) 
        {
            return -1;
        } 
        else 
        {
            return 0;
        }
    }

	@Override
	public String toString()
	{
		return "Chromosome [id=" + id + ", genes=" + genes + ", globalFitnessScore=" + globalFitnessScore + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(globalFitnessScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((genes == null) ? 0 : genes.hashCode());
		result = prime * result + id;
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
		RootCauseList other = (RootCauseList) obj;
		if (Double.doubleToLongBits(globalFitnessScore) != Double.doubleToLongBits(other.globalFitnessScore))
			return false;
		if (genes == null)
		{
			if (other.genes != null)
				return false;
		}
		else if (!genes.equals(other.genes))
			return false;
		if (id != other.id)
			return false;
		return true;
	}
}
