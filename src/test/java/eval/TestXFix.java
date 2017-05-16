package eval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xfix.Constants;
import xfix.Util;
import xfix.XFixConstants;
import xfix.XbiMainIterator;
import xfix.XbiSearch;
import xfix.WebDriverSingleton.Browser;
import xfix.fitness.xbi.XbiFitnessFunction;
import xfix.fitness.xbi.XbiUtil;

public class TestXFix {

	public void runApproach(String[] args)
	{
		// args[0] -> page URL
		// args[1] -> original page location
		// args[2] -> Reference browser
		// args[3] -> Test browser

		// set all static variables to their default values
		XbiUtil.setMatchedNodeXpaths(new HashMap<>());
		XbiFitnessFunction.setJsonRef("");
		XbiFitnessFunction.setGlobalFitnessCalls(0);
		XbiFitnessFunction.setGlobalFitnessTimeInSec(0);
		XbiFitnessFunction.setLocalFitnessCalls(0);
		XbiFitnessFunction.setLocalFitnessTimeInSec(0);
		XbiMainIterator.setGeneration(0);
		XbiSearch.setPhase1TimeInSec(0.0);
		XbiSearch.setPhase2TimeInSec(0.0);
		
		String basepath = new File(args[1]).getParent();
		if(Constants.RUN_IN_DEBUG_MODE)
		{
			try
			{
				System.setOut(new PrintStream(new FileOutputStream(basepath + File.separatorChar + "log_" + System.nanoTime() + ".txt")));
			}
			catch (Exception e)
			{
			}
		}

		Constants.REFERENCE_BROWSER = Browser.valueOf(args[2]);
		Constants.TEST_BROWSER = Browser.valueOf(args[3]);
		
		// set test and oracle paths
		XFixConstants.setOraclePageFullPath(args[0]);
		XFixConstants.setTestPageFullPath(args[0]);
		
		// run search
		long startTime = System.nanoTime();
		XbiMainIterator xmi = new XbiMainIterator();
		xmi.setSubjectBasepath(basepath);
		xmi.setOriginalPageLocation(args[1]);
		xmi.runIterator();
		long endTime = System.nanoTime();
		
		System.out.println("\n--------------------- FINAL RESULTS ---------------------------------");
		System.out.println("# before XBIs = " + xmi.getBeforeXbis());
		System.out.println("# after XBIs = " + xmi.getAfterXbis());
		System.out.println("Reduction in XBIs = " + Math.round(((double)(xmi.getBeforeXbis() - xmi.getAfterXbis()) / (double)xmi.getBeforeXbis()) * 100.0) + "%");
		System.out.println("Total time = " + Util.convertNanosecondsToSeconds(endTime - startTime) + " sec");
		System.out.println("Avg. total time for search for candidate fixes = " + (XbiSearch.getPhase1TimeInSec() / XbiMainIterator.getGeneration()) + " sec");
		System.out.println("Avg. total time for best combination = " + (XbiSearch.getPhase2TimeInSec() / XbiMainIterator.getGeneration()) + " sec");
		System.out.println("No. of fitness calls for search for candidate fixes = " + XbiFitnessFunction.getLocalFitnessCalls());
		System.out.println("Avg. time for fitness call for search for candidate fixes = " + (XbiFitnessFunction.getLocalFitnessTimeInSec() / (double)XbiFitnessFunction.getLocalFitnessCalls()) + " sec");
		System.out.println("No. of fitness calls for best combination = " + XbiFitnessFunction.getGlobalFitnessCalls());
		System.out.println("Avg. time for fitness call for best combination = " + (XbiFitnessFunction.getGlobalFitnessTimeInSec() / (double)XbiFitnessFunction.getGlobalFitnessCalls()) + " sec");
		System.out.println("-------------------------------------------------------------------------------");
	}
	
	public static void main(String[] args) 
	{
		String[] benjaminlees = {"http://localhost:8080/xfix/benjaminlees/index.html", 
				"C:/xfix/subjects/benjaminlees/index.html", 
				"CHROME", "FIREFOX"};
		
		String[] bitcoin = {"http://localhost:8080/xfix/bitcoin/index.html", 
				"C:/xfix/subjects/bitcoin/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] eboss = {"http://localhost:8080/xfix/eboss/index.html",
				"C:/xfix/subjects/eboss/index.html", 
				"INTERNET_EXPLORER", "FIREFOX"};
		
		String[] equilibriumfans = {"http://localhost:8080/xfix/equilibriumfans/www.equilibriumfans.com/index.html", 
				"C:/xfix/subjects/equilibriumfans/www.equilibriumfans.com/index.html", 
				"CHROME", "FIREFOX"};
		
		String[] grantabooks = {"http://localhost:8080/xfix/grantabooks/index.html", 
				"C:/xfix/subjects/grantabooks/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] henrycountyohio = {"http://localhost:8080/xfix/henrycountyohio/index.html", 
				"C:/xfix/subjects/henrycountyohio/index.html", 
				"INTERNET_EXPLORER", "FIREFOX"};
		
		String[] hotwirehotel = { 
				"http://localhost:8080/xfix/hotwirehotel/index.html", 
				"C:/xfix/subjects/hotwirehotel/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] incredibleindia = {"http://localhost:8080/xfix/incredibleindia/incredibleindia.org/index.html", 
				"C:/xfix/subjects/incredibleindia/incredibleindia.org/index.html", 
				"INTERNET_EXPLORER", "FIREFOX"};
		
		String[] leris = {"http://localhost:8080/xfix/leris/clear.uconn.edu/leris/index.html", 
				"C:/xfix/subjects/leris/clear.uconn.edu/leris/index.html", 
				"FIREFOX", "CHROME"};
		
		String[] minix3 = {"http://localhost:8080/xfix/minix3/index.html", 
				"C:/xfix/subjects/minix3/index.html", 
				"INTERNET_EXPLORER", "CHROME", "C:/xfix/subjects/minix3", ""};
		
		String[] newark = {"http://localhost:8080/xfix/newark/index.html",
				"C:/xfix/subjects/newark/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] ofa = {"http://localhost:8080/xfix/ofa/www.ofa.org/index.html", 
				"C:/xfix/subjects/ofa/www.ofa.org/index.html", 
				"INTERNET_EXPLORER", "CHROME"};
		
		String[] pma = {"http://localhost:8080/xfix/pma/index.html",
				"C:/xfix/subjects/pma/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] stephenhunt = {"http://localhost:8080/xfix/stephenhunt/index.html", 
				"C:/xfix/subjects/stephenhunt/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		String[] wit = {"http://localhost:8080/xfix/wit/index.html", 
				"C:/xfix/subjects/wit/index.html", 
				"FIREFOX", "INTERNET_EXPLORER"};
		
		List<String[]> subjects = new ArrayList<String[]>();
//		subjects.add(benjaminlees);
//		subjects.add(bitcoin);
//		subjects.add(grantabooks);
//		subjects.add(eboss); 
//		subjects.add(equilibriumfans);
//		subjects.add(henrycountyohio);
//		subjects.add(hotwirehotel);
		subjects.add(incredibleindia);
//		subjects.add(leris);
//		subjects.add(minix3);
//		subjects.add(newark);
//		subjects.add(ofa);
//		subjects.add(pma);
//		subjects.add(stephenhunt);
//		subjects.add(wit);
		
		for(String[] runXFixArgs : subjects)
		{
			int NUMBER_OF_RUNS = 1;
			for(int i = 0; i < NUMBER_OF_RUNS; i++)
			{
				String subject = runXFixArgs[0];
				System.err.println(subject + " --> run " + (i+1) + " of " + NUMBER_OF_RUNS);
				TestXFix testXfix = new TestXFix();
				testXfix.runApproach(runXFixArgs);
			}
		}
	}
}
