package xfix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;


public class WebDriverSingleton
{
	private static WebDriverSingleton instance = null;
	private static Map<Browser, WebDriver> driverMap = null;
	
	public enum Browser
	{
		FIREFOX, INTERNET_EXPLORER, CHROME;
	}
	
	private WebDriverSingleton(Browser[] browsers)
	{
		for (int i = 0; i < browsers.length; i++)
		{
			switch (browsers[i]) 
			{
	            case FIREFOX:
	            	openFirefox();
	                break;
	                
	            case INTERNET_EXPLORER:
	            	openInternetExplorer();
	                break;
	                
	            case CHROME:
	            	openChrome();
	                break;    
			}
		}
	}

	private void openFirefox()
	{
		WebDriver driver = new FirefoxDriver();
		driver.manage().window().maximize();
		driverMap.put(Browser.FIREFOX, driver);
	}
	
	private void openInternetExplorer()
	{
		System.setProperty("webdriver.ie.driver", Constants.INTERNET_EXPLORER_DRIVER_FILEPATH);
		WebDriver driver = new InternetExplorerDriver();
		driver.manage().window().maximize();
		driverMap.put(Browser.INTERNET_EXPLORER, driver);
	}
	
	private void openChrome()
	{
		File pathtoBinary = new File("C:\\Users\\xfix\\Downloads\\Chrome64_51.0.2704.84\\chrome.exe");
		System.setProperty("webdriver.chrome.driver", Constants.CHROME_DRIVER_FILEPATH);
		ChromeOptions options = new ChromeOptions();
		options.setBinary(pathtoBinary);
		options.addArguments("--start-maximized");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		driverMap.put(Browser.CHROME, driver);
	}
	
	public static void openBrowsers(Browser[] browsers)
	{
		if(instance == null)
		{
			driverMap = new HashMap<WebDriverSingleton.Browser, WebDriver>();
			instance = new WebDriverSingleton(browsers);
		}
		else
		{
			for (int i = 0; i < browsers.length; i++)
			{
				if(!driverMap.containsKey(browsers[i]))
				{
					new WebDriverSingleton(new Browser[] {browsers[i]});
				}
			}
		}
	}
	
	public static WebDriver getDriver(Browser browser)
	{
		openBrowsers(new Browser[] {browser});
		return driverMap.get(browser);
	}
	
	public static void closeAllOpenBrowsers()
	{
		if(instance != null)
		{
			for(WebDriver driver : driverMap.values())
			{
				if(driver instanceof InternetExplorerDriver)
					driver.quit();
				else
					driver.close();
			}
			driverMap = new HashMap<>();
			instance = null;
		}
	}
	
	public static void closeBrowser(Browser browser)
	{
		if(driverMap.containsKey(browser))
		{
			if(browser.toString().equalsIgnoreCase(Browser.INTERNET_EXPLORER.name()))
				driverMap.get(browser).quit();
			else
				driverMap.get(browser).close();
			driverMap.remove(browser);
		}
	}
	
	public static void loadPage(String htmlFileFullPath, Browser browser)
	{
		openBrowsers(new Browser[] {browser});
		String urlString = htmlFileFullPath;
		if(!urlString.contains("http://"))
		{
			urlString = "file:///" + urlString;
		}
		try
		{
			driverMap.get(browser).manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			driverMap.get(browser).get(urlString);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static File getScreenshot(Browser browser)
	{
		WebDriver driver = getDriver(browser);
		
		if(browser.name().equalsIgnoreCase("CHROME"))
		{
			Screenshot ashotScreenshot = new AShot()
			  .shootingStrategy(ShootingStrategies.viewportPasting(100))
			  .takeScreenshot(driver);
			BufferedImage img = ashotScreenshot.getImage();
			File tempScreenshotFile = null;
			try
			{
				tempScreenshotFile = File.createTempFile("tempScreenshot", ".png");
				ImageIO.write(img, "png", tempScreenshotFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			// reload the page to eliminate any effects of scrolling
			String code = "window.scrollTo(0, 0)";
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript(code);
			
			return tempScreenshotFile;
		}
		else
		{
			File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			return screenshotFile;
		}
	}
}