package sk.ppmscan.application.util;

import java.util.Random;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class WebClientUtil {
	
	private static BrowserVersion[] browsers = new BrowserVersion[] { BrowserVersion.CHROME, BrowserVersion.EDGE,
			BrowserVersion.FIREFOX_38, BrowserVersion.INTERNET_EXPLORER_11 };
	
	public static WebClient createWebClient(BrowserVersion browser) throws Exception {
		WebClient client = new WebClient(browser);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		return client;
	}

	public static WebClient createWebClient() throws Exception {
		WebClient client = new WebClient(browsers[new Random().nextInt(browsers.length)]);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		return client;
	}

//	public static WebClient createWebClient(String login, String password) throws Exception {
//		WebClient client = createWebClient();
//
//		HtmlPage page = client.getPage(LOGIN_URL);
//
//		HtmlInput inputUsername = page.getFirstByXPath("//input[@id='username']");
//		if (inputUsername == null) {
//			throw new Exception("Username field not found");
//		}
//
//		HtmlInput inputPassword = page.getFirstByXPath("//input[@type='password']");
//		if (inputPassword == null) {
//			throw new Exception("Password field not found");
//		}
//
//		inputUsername.setValueAttribute(login);
//		inputPassword.setValueAttribute(password);
//
//		// get the enclosing form
//		HtmlForm loginForm = inputPassword.getEnclosingForm();
//
//		// submit the form
//		page = client.getPage(loginForm.getWebRequest(null));
//
//		page = client.getPage("https://ppm.powerplaymanager.com/en/home.html");
//
//		LOGGER.info("After login, title text: " + page.getTitleText());
//		if (!"User account".equals(page.getTitleText())) {
//			throw new Exception("Most likely not logged in");
//		}
//
//		LOGGER.info("Logged in!");
//
//		// returns the cookies filled client :)
//		return client;
//	}


}
