package xfix.input.xbi;

public class XpertXbi 
{
	private String label;
	private String e1Ref;
	private String e2Ref;
	private String e1Test;
	private String e2Test;
	
	public String getLabel() 
	{
		return label;
	}
	public void setLabel(String label) 
	{
		this.label = label;
	}
	public String getE1Ref() 
	{
		return e1Ref;
	}
	public void setE1Ref(String e1Ref) 
	{
		this.e1Ref = e1Ref;
	}
	public String getE2Ref() 
	{
		return e2Ref;
	}
	public void setE2Ref(String e2Ref) 
	{
		this.e2Ref = e2Ref;
	}
	public String getE1Test() 
	{
		return e1Test;
	}
	public void setE1Test(String e1Test) 
	{
		this.e1Test = e1Test;
	}
	public String getE2Test() 
	{
		return e2Test;
	}
	public void setE2Test(String e2Test) 
	{
		this.e2Test = e2Test;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e1Ref == null) ? 0 : e1Ref.hashCode());
		result = prime * result + ((e1Test == null) ? 0 : e1Test.hashCode());
		result = prime * result + ((e2Ref == null) ? 0 : e2Ref.hashCode());
		result = prime * result + ((e2Test == null) ? 0 : e2Test.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XpertXbi other = (XpertXbi) obj;
		if (e1Ref == null) {
			if (other.e1Ref != null)
				return false;
		} else if (!e1Ref.equals(other.e1Ref))
			return false;
		if (e1Test == null) {
			if (other.e1Test != null)
				return false;
		} else if (!e1Test.equals(other.e1Test))
			return false;
		if (e2Ref == null) {
			if (other.e2Ref != null)
				return false;
		} else if (!e2Ref.equals(other.e2Ref))
			return false;
		if (e2Test == null) {
			if (other.e2Test != null)
				return false;
		} else if (!e2Test.equals(other.e2Test))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "XpertXbi [label=" + label + ", e1Ref=" + e1Ref + ", e2Ref=" + e2Ref + ", e1Test=" + e1Test + ", e2Test="
				+ e2Test + "]";
	}
}
