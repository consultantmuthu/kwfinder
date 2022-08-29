/**
 * 
 */
package com.slapps.kwfinder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/**
 * @author Muthukumaran
 *
 */
public class SLkwfinder extends JDialog {

	private static String FQCN = SLkwfinder.class.getName();

	class CSVFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getName().toLowerCase().endsWith(".csv"));
		}

		public String getDescription() {
			return "*.csv";
		}
	}

	class TXTFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getName().toLowerCase().endsWith(".txt"));
		}

		public String getDescription() {
			return "*.txt";
		}
	}

	class EXEFileFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getName().toLowerCase().endsWith(".exe"));
		}

		public String getDescription() {
			return "*.exe";
		}
	}

	private static final String CONTENT_SCRAPING_FAILURE = "SCRAPING ERROR !!!!! ";

	private Document connect(String url) throws IOException {
		Document doc = null;
		java.net.Proxy rrproxy = RRProxy.getProxy();
		if (url != null && (url.trim().startsWith("http") 
							|| url.trim().startsWith("www"))) {
			if (driver instanceof ChromeDriver) {
				driver.close();
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("disable-notifications");
				Logit.log(FQCN, "Using WebDriver and jsoup");
				if (rrproxy != null) {
					InetSocketAddress addr = (InetSocketAddress) rrproxy.address();
					String ip = addr.getHostName();
					int port = addr.getPort();
					Proxy proxy = new Proxy();
					proxy.setAutodetect(false);
					proxy.setHttpProxy(ip + ":" + port);
					// proxy.setSslProxy("https_proxy-url:port");
					proxy.setNoProxy("no_proxy-var");
					chromeOptions.setCapability("proxy", proxy);
				}
				driver = new ChromeDriver(chromeOptions);
				driver.manage().window().minimize();
				if (url.trim().startsWith("www")) {
					driver.get("https://"+url);
				} else {
					driver.get(url);
				}
				doc = Jsoup.parse(driver.getPageSource());
			} else {
				if (rrproxy != null) {
					Logit.log(FQCN, "Using Proxy via jsoup");
					doc = Jsoup.connect(url).proxy(rrproxy).userAgent("Chrome/102.0.5005.63").get();
				} else {
					doc = Jsoup.connect(url).userAgent("Chrome/102.0.5005.63").referrer("http://www.google.com").get();

				}
			}
		} else {
			// Test mode - we have files in file system
			File in = new File(url);
			doc = Jsoup.parse(in);
		}
		return doc;
	}

	private WebDriver driver = null;

	private void setDriver(String path) {
		if (path == null || !path.endsWith("chromedriver.exe")) {
			Logit.log(FQCN, "Invalid path for chromedriver " + path);
		} else {
			System.setProperty("webdriver.chrome.driver", path);
			driver = new ChromeDriver();
			driver.manage().window().minimize();
		}
	}

	/**
	 * Rule-1: Element to start with 
	 * 
	 * Rule-2: Element can have minimum one href tag 
	 * 
	 * Rule-3: Element not surrounded with <li> tags
	 * 
	 * @return
	 */
	private Map<String, ArrayList<String>> filterElement(Elements elements, String keyword, String url) {
		Map<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();
		ArrayList<String> scrapedContent = new ArrayList<String>();
		AtomicInteger count = new AtomicInteger(0);
		elements.stream().filter(e -> e.nodeName().startsWith("p")).forEach(e -> {

			scrapedContent.add(e.text());
			if (data.size() == 0) {
				Elements hrefElement = e.getElementsByAttribute("href");
				if (hrefElement.size() > 0) {
					if (!hrefElement.text().toLowerCase().contains(keyword.toLowerCase())) {
						ArrayList<String> value = data.get(url);
						if (value == null) {
							value = new ArrayList<String>();
						}
						value.add(e.text());
						data.put(url, value);
					}
				} else {
					Elements liELements = e.getElementsByTag("li");
					if (liELements.size() == 0) {
						ArrayList<String> value = data.get(url);
						if (value == null) {
							value = new ArrayList<String>();
						}
						/*
						 * String content = "<Paragraph_" + count.incrementAndGet() + ">" + e.text() +
						 * "</Paragraph_" + count.get() + ">";
						 */
						value.add(e.text());
						data.put(url, value);
					}
				}
			}
		});
		if (data.size() == 0) {
			data.put(url,  scrapedContent);
			Logit.log(FQCN, url + " is not matched with any rules !!!! ");
		}
		ArrayList<String> values = data.get(url);
		if (values.size() > 1) {   // We should implement stop word and additional keyword, etc..
			String bigContent = values.stream().max(Comparator.comparing(String::length)).get();
			values.clear();
			values.add(bigContent);
		}
		return data;

		/*
		 * if (element.tagName("p")) { if (element.hasAttr("href")) { data =
		 * element.text(); } else { element.text().split(data) } }
		 */
	}

	public File[] getAllFiles(File directory) {

		File[] fileList = directory.listFiles((dir, name) -> name.endsWith(".csv"));
		return fileList;
	}

	public File selectFile(String selectorName, javax.swing.filechooser.FileFilter filter) {

		File selectedFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setDialogTitle(selectorName);
		// fileChooser.addChoosableFileFilter(new CSVFileFilter());
		this.setAlwaysOnTop(Boolean.TRUE);
		fileChooser.setFileFilter(filter);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
		}
		this.dispose();
		return selectedFile;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
			throws IOException, InterruptedException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		File csvFile = null;
		Logit.log(FQCN, "Starting...");
		SLkwfinder slkwfinder = new SLkwfinder();
		try {
			File proxyFile = slkwfinder.selectFile("Select proxy txt file", slkwfinder.new TXTFileFilter());
			RRProxy.setProxyFile(proxyFile);
		} catch (Exception e) {
			Logit.log(FQCN, Throwables.getStackTraceAsString(e));
		}

		try {
			File chromeFile = slkwfinder.selectFile("Select ChromeDriver Exe file", slkwfinder.new EXEFileFilter());
			if (chromeFile != null)
				slkwfinder.setDriver(chromeFile.getPath());
		} catch (Exception e) {
			Logit.log(FQCN, Throwables.getStackTraceAsString(e));
		}
		try {

			//
			// We should get all CSV file as input. This program
			// can do following,
			//
			// 1. It can read all CSV File and work on it one by one
			// 2. It can take CSV file as input and work on it one by one
			// 3. It can take the url and keyword directly and work on it
			//
			// Basically option - 3 is only for testing
			//
			switch (args.length) {
			case 0:
				/*
				 * File file = new File("."); File[] fileList = slkwfinder.getAllFiles(file);
				 * for (File csvFile : fileList) { CSVFileManager csvFileManager = new
				 * CSVFileManager(csvFile); List<KWData> data = csvFileManager.parse(); for
				 * (KWData kwData : data) { slkwfinder.scrape(kwData); }
				 * csvFileManager.update(data); }
				 */
				// do {
					csvFile = slkwfinder.selectFile("Select CSV File", slkwfinder.new CSVFileFilter());
				// } while (csvFile == null);
				if (csvFile != null) {
					CSVFileManager csvFileManager = new CSVFileManager(csvFile);
					List<KWData> data = csvFileManager.parse();
					for (KWData kwData : data) {
						slkwfinder.scrape(kwData);
						csvFileManager.update(data);
					}
				}
				// csvFileManager.update(data);
				break;
			case 1:
				csvFile = new File(args[0]);
				CSVFileManager csvFileManager = new CSVFileManager(csvFile);
				List<KWData> data = csvFileManager.parse();
				for (KWData kwData : data) {
					slkwfinder.scrape(kwData);
					csvFileManager.update(data);
				}
				break;
			default: 
				if (args != null && args.length >= 2) {
					String keywords = args[0]; // "best weight loss pills";
					int i = 1;
					while (i < args.length && args[i] != null) {
						KWData kwData = new KWData();
						kwData.setURL(args[i]);
						kwData.setKeyword(keywords);
						slkwfinder.scrape(kwData);
						i++;
						Thread.sleep(25000);
					}
				}
				break;
			}
		} catch (Exception e) {
			Logit.log(FQCN, Throwables.getStackTraceAsString(e));
		} finally {
			slkwfinder.shutdown();
		}
		return;
	}

	private void shutdown() {
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

	private void scrape(KWData kwData) {
		boolean connected = Boolean.TRUE;
		Document doc = null;
		if (kwData.getTextToExport() == null || kwData.getTextToExport().trim().isEmpty()) {
				//|| kwData.getTextToExport().contentEquals(CONTENT_SCRAPING_FAILURE)) {
			try {
				doc = connect(kwData.getURL());
			} catch (Exception e) {
				if (e instanceof IOException) {
					if (e.getMessage().contains("403")) {
						Logit.log(FQCN, "Sleep and retry as we have 403");
						RRProxy.changeProxy();
						try {
							Thread.sleep(60000);
							doc = connect(kwData.getURL());
						} catch (Exception e1) {}
					}
				}
				connected = Boolean.FALSE;
				Logit.log(FQCN, Throwables.getStackTraceAsString(e));
			} finally {
				if (connected == Boolean.TRUE) {
					String keyword = kwData.getKeyword();
					if (keyword != null && !keyword.trim().isEmpty()) {
						Elements elements = doc.select("p:contains(" + keyword + ")");
						Map<String, ArrayList<String>> extractedData = filterElement(elements, kwData.getKeyword(),
								kwData.getURL());
						if (extractedData != null && extractedData.size() > 0) {
							ArrayList<String> values = extractedData.get(kwData.getURL());
							for (String value : values) {
								if (!Strings.isNullOrEmpty(value)) {
									kwData.setTextToExport(value);
								}	
							}
						}
						Logit.log(FQCN, kwData.toString());
					}
				} /*else {
					kwData.setTextToExport(CONTENT_SCRAPING_FAILURE);
				}*/
			}
			// Take it easy
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
