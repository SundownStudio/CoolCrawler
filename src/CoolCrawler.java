import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class CoolCrawler {

	private final static int MAX_URLS = 500;
	private final static int MAX_EMAILS = 50;
	private final static String QUIT = "q";
	private final static String TXT_INSTR = "\nPlease enter a DOMAIN or press " + QUIT + " to quit.";
	private final static String TXT_INVALID = "Invalid domain format (format must be \'xxx.xxx' example: mit.edu)";
	private final static String TXT_LD_BRWSR = "Starting browser please wait a sec..\n";

	private final Pattern emailRegex = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

	private final Model model;

	private final WebDriver browser;

	private final String domain;

	private final static FirefoxProfile profile;
	static {
		profile = new FirefoxProfile();
		profile.setPreference("permissions.default.stylesheet", 2);
		profile.setPreference("permissions.default.image", 2);
		profile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);
	}

	public CoolCrawler(final String domain) {
		this.domain = domain;
		this.model = new Model(MAX_EMAILS, MAX_URLS);

		browser = new FirefoxDriver(profile);
		browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	public void run() {

		model.addURL(formatUrl(domain));

		while (model.hasMoreURLsToVisit() && model.getNumEmails() < MAX_EMAILS) {
			String url = model.getNextDestination();
			System.out.println("Now Crawling: " + url);
			browser.get(url);
			extractUrls();
			extractEmails();
		}

		System.out.println(model.toString());
		model.clear();
		browser.close();
	}

	private void extractUrls() {
		if (model.getHistorySize() == MAX_URLS)
			return;

		List<WebElement> links = browser.findElements(By.tagName("a"));

		for (WebElement we : links) {
			String url = null;
			try {
				url = we.getAttribute("href");
			} catch (StaleElementReferenceException e) {
				e.printStackTrace();
				break;
			}

			if (url != null) {
				url = formatUrl(url);

				if (compareToDomain(url) && model.addURL(url)) {
					System.out.println("-URL ADDED: " + url);
					if (model.getHistorySize() == MAX_URLS)
						break;
				}
			}
		}
	}

	private void extractEmails() {
		Matcher matcher = emailRegex.matcher(browser.getPageSource());

		while (matcher.find()) {
			String email = matcher.group();
			if (model.addEmail(email)) {
				System.out.println("-EMAIL ADDED: " + email);
				if (model.getNumEmails() == MAX_EMAILS)
					break;
			}
		}
	}

	private String formatUrl(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			if (url.startsWith("//"))
				return "http:" + url;
			return "http://" + url;
		}
		return url;
	}

	private boolean compareToDomain(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

		String host = uri.getHost();
		if (host != null && !uri.getRawSchemeSpecificPart().startsWith("//mailto"))
			return (host.startsWith("www.") || host.startsWith("web.")) ? host.substring(4).equals(domain)
					: host.equals(domain);
		return false;

	}

	public static void main(String[] args) {

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		Pattern domainRegex = Pattern.compile("[a-zA-Z0-9-]+\\.[a-zA-Z0-9-]+");

		while (true) {
			System.out.println(TXT_INSTR);
			String input = "";

			try {
				input = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (input.equalsIgnoreCase(QUIT))
				break;

			if (!domainRegex.matcher(input).matches()) {
				System.out.println(TXT_INVALID);
			} else {
				System.out.println(TXT_LD_BRWSR);
				new CoolCrawler(input).run();
			}
		}
	}
}