package xfix.input.xbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ReadXpertOutput 
{
	private Set<String> reportedElementSetRef;
	private Set<String> reportedElementSetTest;
	private int totalNumberOfXBIsReported;
	private Set<String> xbiStrings;
	private Set<XpertXbi> xpertXbis;
	
	public ReadXpertOutput() 
	{
		reportedElementSetRef = new HashSet<String>();
		reportedElementSetTest = new HashSet<String>();
		xbiStrings = new HashSet<>();
		xpertXbis = new HashSet<>();
	}
	
	public Set<String> getReportedElementSetRef() {
		return reportedElementSetRef;
	}

	public void setReportedElementSetRef(Set<String> reportedElementSetRef) {
		this.reportedElementSetRef = reportedElementSetRef;
	}

	public Set<String> getReportedElementSetTest() {
		return reportedElementSetTest;
	}

	public void setReportedElementSetTest(Set<String> reportedElementSetTest) {
		this.reportedElementSetTest = reportedElementSetTest;
	}
	
	public int getTotalNumberOfXBIsReported() {
		return totalNumberOfXBIsReported;
	}

	public void setTotalNumberOfXBIsReported(int totalNumberOfXBIsReported) {
		this.totalNumberOfXBIsReported = totalNumberOfXBIsReported;
	}

	public Set<String> getXbiStrings()
	{
		return xbiStrings;
	}

	public void setXbiStrings(Set<String> xbis)
	{
		this.xbiStrings = xbis;
	}

	public Set<XpertXbi> getXpertXbis()
	{
		return xpertXbis;
	}

	public void setXpertXbis(Set<XpertXbi> xpertXbis)
	{
		this.xpertXbis = xpertXbis;
	}

	public void readInputFromFile(String reportPath) throws IOException
	{
		if(!new File(reportPath).exists())
			return;
			
		BufferedReader br = new BufferedReader(new FileReader(reportPath));
		String line = "";
		while((line = br.readLine()) != null)
		{
			xbiStrings.add(line);
			
			line = line.replace("\"", "");
			String[] lineArr = line.split(",");
			
			XpertXbi xbi = new XpertXbi();
			xbi.setLabel(lineArr[0]);
			
			String refElement = lineArr[1];
			String testElement = lineArr[2];
			
			cleanseInput(refElement, testElement, xbi);
			totalNumberOfXBIsReported++;
		}
		br.close();
	}
	
	public void readInputFromString(String issues)
	{
		if(issues == null || issues.isEmpty())
			return;
		
		String[] issuesArr = issues.split(System.getProperty("line.separator"));
		for(int i = 0; i < issuesArr.length; i++)
		{
			xbiStrings.add(issuesArr[i]);
			
			issuesArr[i] = issuesArr[i].replace("\"", "");
			String[] lineArr = issuesArr[i].split(",");
			
			XpertXbi xbi = new XpertXbi();
			xbi.setLabel(lineArr[0]);
			
			String refElement = lineArr[1];
			String testElement = lineArr[2];
			
			cleanseInput(refElement, testElement, xbi);
			totalNumberOfXBIsReported++;
		}
	}
	
	private void cleanseInput(String refElement, String testElement, XpertXbi xbi)
	{
		refElement = refElement.replace("(", "");
		refElement = refElement.replace(")", "");
		refElement = refElement.replace("*-*", "");
		refElement = refElement.trim();
		
		testElement = testElement.replace("(", "");
		testElement = testElement.replace(")", "");
		testElement = testElement.replace("*-*", "");
		testElement = testElement.trim();
		
		if(refElement.contains("-/"))
		{
			String[] eles = refElement.split("-/");
			eles[1] = "/" + eles[1];
			reportedElementSetRef.add(eles[0].toLowerCase());
			reportedElementSetRef.add(eles[1].toLowerCase());
			
			xbi.setE1Ref(eles[0].toLowerCase());
			xbi.setE2Ref(eles[1].toLowerCase());
		}
		else if(!refElement.isEmpty())
		{
			refElement = refElement.toLowerCase();
			reportedElementSetRef.add(refElement);
			xbi.setE1Ref(refElement);
			xbi.setE2Ref(refElement);
		}
		
		if(testElement.contains("-/"))
		{
			String[] eles = testElement.split("-/");
			eles[1] = "/" + eles[1];
			reportedElementSetTest.add(eles[0].toLowerCase());
			reportedElementSetTest.add(eles[1].toLowerCase());
			
			xbi.setE1Test(eles[0].toLowerCase());
			xbi.setE2Test(eles[1].toLowerCase());
		}
		else if(!testElement.isEmpty())
		{
			testElement = testElement.toLowerCase();
			reportedElementSetTest.add(testElement);
			xbi.setE1Test(testElement);
			xbi.setE2Test(testElement);
		}
		
		
		xpertXbis.add(xbi);
	}
}
