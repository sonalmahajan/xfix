package xfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xfix.WebDriverSingleton.Browser;

public class Constants
{
	public static boolean RUN_IN_DEBUG_MODE = true;
	public static final String REPAIR_CSS_FILENAME = "repair.css";
	public static String INTERNET_EXPLORER_DRIVER_FILEPATH = "C:/xfix/drivers/IEDriverServer2-53-1.exe";
	public static String CHROME_DRIVER_FILEPATH = "C:/xfix/drivers/chromedriver.exe";
	public static Browser REFERENCE_BROWSER = Browser.FIREFOX;
	public static Browser TEST_BROWSER = Browser.FIREFOX;
	
	public static final List<String> NUMERIC_POSITIVE_PROPERTIES = Arrays.asList("height", "max-height", "max-width", "min-height", 
		"min-width", "width", /*"counter-reset",*/ "padding-bottom", "padding-top", "padding-right", "padding-left", /*"border-spacing",
		"background-position",*/ "border-bottom-width", "border-top-width", "border-left-width", "border-right-width", "outline-width", 
		"font-size", "letter-spacing", "line-height");
	
	public static final List<String> NUMERIC_POSITIVE_NEGATIVE_PROPERTIES = Arrays.asList(/*"counter-increment",*/ "margin-bottom", 
		"margin-top", "margin-right", "margin-left", "bottom", "top", "left", "right", "z-index", "text-indent", /*"vertical-align",*/ 
		"word-spacing");

	// genetic algorithm
    public static final double OPTIMAL_FITNESS_SCORE = 0;
    public static final double INITIAL_FITNESS_SCORE = -1;
    public static int SATURATION_POINT = 10;
    
    // AVM search
    public static final int[] EXPLORATORY_MOVES_ARR = {-1, 1};
    public static final int PATTERN_BASE = 2;
    
    // XBI
    public static int NEIGHBORHOOD_RADIUS = 2;
    public static final List<String> CONTAINS_XBIS = Arrays.asList("PARENTS DIFFER", "TOP-ALIGNMENT", "BOTTOM-ALIGNMENT", "VMID-ALIGNMENT",
    		"VFILL", "HFILL", "CENTER-ALIGNMENT", "LEFT-JUSTIFICATION", "RIGHT-JUSTIFICATION", "MISSING-PARENT-1", "MISSING-PARENT-2");
    public static final List<String> SIBLING_XBIS = Arrays.asList("MISSING-SIBLING-2", "MISSING-SIBLING-1", "TOP-EDGE-ALIGNMENT", 
    		"RIGHT-EDGE-ALIGNMENT", "BOTTOM-EDGE-ALIGNMENT", "LEFT-EDGE-ALIGNMENT", "TOP-BOTTOM", "BOTTOM-TOP", "LEFT-RIGHT", "RIGHT-LEFT");
    
    // Methods
	public static List<String> getAllProperties()
	{
		List<String> allPropertiesList = new ArrayList<String>(NUMERIC_POSITIVE_NEGATIVE_PROPERTIES);
		allPropertiesList.addAll(NUMERIC_POSITIVE_PROPERTIES);
		
		return allPropertiesList;
	}
}
