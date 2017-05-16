package xfix;

import java.util.Set;

public class XbiElementRelationship
{
	private String element;
	private String parent;
	private Set<String> children;
	private Set<String> siblings;
	public String getElement()
	{
		return element;
	}
	public void setElement(String element)
	{
		this.element = element;
	}
	public String getParent()
	{
		return parent;
	}
	public void setParent(String parent)
	{
		this.parent = parent;
	}
	public Set<String> getChildren()
	{
		return children;
	}
	public void setChildren(Set<String> children)
	{
		this.children = children;
	}
	public Set<String> getSiblings()
	{
		return siblings;
	}
	public void setSiblings(Set<String> siblings)
	{
		this.siblings = siblings;
	}
	
	public XbiElementRelationship(String element, String parent, Set<String> children, Set<String> siblings)
	{
		this.element = element;
		this.parent = parent;
		this.children = children;
		this.siblings = siblings;
	}
}
