package xfix.fitness.xbi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HtmlDomTree
{
	private Node<HtmlElement> root;

	public Node<HtmlElement> getRoot()
	{
		return root;
	}

	public void setRoot(Node<HtmlElement> root)
	{
		this.root = root;
	}

	public Node<HtmlElement> searchHtmlDomTreeByXpath(String xpath)
	{
		Queue<Node<HtmlElement>> q = new LinkedList<Node<HtmlElement>>();
		q.add(this.root);
		
		while(!q.isEmpty())
		{
			Node<HtmlElement> node = q.remove();
			if(node.getData().getXpath().equalsIgnoreCase(xpath))
			{
				return node;
			}
			if (node.getChildren() != null)
			{
				for (Node<HtmlElement> child : node.getChildren())
				{
					q.add(child);
				}
			}
		}
		return null;
	}
	
	public void preOrderTraversalRTree()
	{
		preOrderTraversalRTree(this.root);
	}
	
	private void preOrderTraversalRTree(Node<HtmlElement> node)
	{
		if (node == null)
		{
			return;
		}
		System.out.println(node.getData().getTagName() + ": " + node.getData());
		if (node.getChildren() != null)
		{
			for (Node<HtmlElement> child : node.getChildren())
			{
				preOrderTraversalRTree(child);
			}
		}
	}
	
	public void buildHtmlDomTreeFromJson(String json)
	{
		Map<Integer, Node<HtmlElement>> idNodeMap = new HashMap<>(); 
		
		JSONArray arrDom = new JSONArray(json.trim());
		for (int i = 0; i < arrDom.length(); i++) 
		{
			JSONObject nodeData = arrDom.getJSONObject(i);
			int type = nodeData.getInt("type");
			if(type == 1)
			{
				HtmlElement htmlElement = new HtmlElement();
				JSONArray data = nodeData.getJSONArray("coord");
				for (int i1 = 0; i1 < data.length(); i1++)
				{
					if(!NumberUtils.isNumber(data.get(i1).toString()))
					{
						data.put(i1, 0);
					}
				}
				int x = data.getInt(0);
				int y = data.getInt(1);
				int w = data.getInt(2) - data.getInt(0);
				int h = data.getInt(3) - data.getInt(1);
						
				htmlElement.setX(x);
				htmlElement.setY(y);
				htmlElement.setWidth(w);
				htmlElement.setHeight(h);
				
				htmlElement.setXpath(nodeData.getString("xpath").toLowerCase());
				
				if(nodeData.getString("xpath").equalsIgnoreCase("/html/body"))	// <body> is the root element
				{
					this.root = new Node<HtmlElement>(null, htmlElement);
					idNodeMap.put(nodeData.getInt("nodeid"), root);
				}
				else
				{
					// get parent
					if(nodeData.getInt("pid") >= 0)
					{
						if(idNodeMap.containsKey(nodeData.getInt("pid")))
						{
							Node<HtmlElement> parent = idNodeMap.get(nodeData.getInt("pid"));
							Node<HtmlElement> node = new Node<HtmlElement>(parent, htmlElement);
							idNodeMap.put(nodeData.getInt("nodeid"), node);
						}
					}
				}
			}
		}
	}
}
