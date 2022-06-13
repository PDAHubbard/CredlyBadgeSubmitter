package org.peterh.credly;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.peterh.credly.util.RESTClient;
import org.peterh.credly.util.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.ParseException;

import kong.unirest.Unirest;

public class CredlyBadgeSubmitter {

	private static final String propsFileName = "config.properties";
	private static String organization, basicAuth, baseURL;
	private static RESTClient restClient;

	private static Boolean dryrun;
	private static Boolean revoke;
	private static String inputFilename;
	private static final String versionNumber = "0.1";

	private static List<HashMap<String, Object>> badgeEarners;
	private static CredlyBadgeLoader badgeLoader;
	private static int badgeTemplateNumber;

	private static final void parseCommandLine(String[] args) {
		Options options = new Options();
		options.addOption("v", false, "Display version number.");
		options.addOption("h", false, "Display help");
		options.addOption("d", "dryrun", false, "Dry run (do not submit badges)");
		options.addOption("f", "file", true, "The CSV file to read");
		options.addOption("r", "revoke", false, "Revoke badges listed in the CSV file");

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("h") || !line.hasOption("f")) {
				String header = "Parse a CSV file and either submit Credly badges or revoke a list of already submitted badges.";

				String footer = "Report issues to peter.hubbard@mongodb.com, including the 'logback.log' file.";

				HelpFormatter helpf = new HelpFormatter();
				helpf.printHelp("CredlyBadgeSubmitter", header, options, footer, true);
				System.exit(0);
			} else {
				inputFilename = line.getOptionValue("f");
			}

			if (line.hasOption("d")) {
				log.debug("Dryrun mode");
				dryrun = true;
			} else {
				dryrun = false;
			}

			if (line.hasOption("r")) {
				log.debug("Revoking badges.");
				revoke = true;
			} else {
				revoke = false;
			}

		} catch (ParseException exp) {
			System.err.println("Could not parse command line: " + exp.getMessage());
			log.error(exp.toString());
			System.exit(1);
		}
	}

	private static final void parseProperties() {
		Properties props = new Properties();
		try {
			props.load(new FileReader(new File(".").getCanonicalPath() + File.separator + propsFileName));
			// Prepare the REST client interface
			baseURL = props.getProperty("baseURL");
			organization = props.getProperty("organization");
			basicAuth = props.getProperty("basicAuth");
		} catch (IOException e) {
			log.error("Unable to read properties file: {}", e.toString());
			System.exit(1);
		} catch (Exception e) {
			log.error("Unable to read properties file: {}", e.toString());
			System.exit(1);
		}
	}

	private static void parseInputFile() {
		ParseCSV cp = new ParseCSV();
		// Parse the .csv file to create a list of 3-key maps (first/last name and
		// email)
		badgeEarners = cp.parseFile(inputFilename);
		if (badgeEarners.isEmpty()) {
			log.info("No names to process in file {}", inputFilename);
			System.exit(1);
		}
	}

	private static void initRestClient() throws RestClientException {

		restClient = new RESTClient(organization, baseURL, basicAuth);
	}

	private static void loadBadgeTemplates() {
		// Load the badges
		badgeLoader = new CredlyBadgeLoader();
		badgeLoader.loadCredlyBadges(restClient);

	}

	private static void getBadgeNumberToApply() {
		Scanner keyin;
		int i;

		badgeTemplateNumber = -1;

		if (badgeLoader.getCredlyTemplates().isEmpty()) {
			log.error("No Credly Badge Templates found.");
			System.out.printf("Failed, no badge templates found.");
		} else {
			System.out.printf("done. Found %d badge templates.%n", badgeLoader.size());
			for (i = 0; i < badgeLoader.size(); i++) {
				System.out.printf("%d: %s%n", i, badgeLoader.getCredlyTemplates().get(i).getName());
			}

			while (badgeTemplateNumber < 0 || badgeTemplateNumber > i) {
				System.out.printf("Select the badge number to apply ('0'-'%d' or anything else to quit): ",
						badgeLoader.size() - 1);
				keyin = new Scanner(System.in);
				badgeTemplateNumber = keyin.nextInt();
				System.out.printf("Confirm applying badge %s with id %s (Y/N): ",
						badgeLoader.getCredlyTemplates().get(badgeTemplateNumber).getName(),
						badgeLoader.getCredlyTemplates().get(badgeTemplateNumber).getId());
				if (keyin.next().compareToIgnoreCase("y") != 0)
					badgeTemplateNumber = -1;

			}
		}
	}

	private static final Logger log = LoggerFactory.getLogger(CredlyBadgeSubmitter.class);

	public static void main(String[] args) {

		int numBadgesSubmitted = -1;

		// parse CLAs
		parseCommandLine(args);

		// prepare
		log.debug("Version Number: {}", versionNumber);
		System.out.printf("CredlyBadgeSubmitter version %s, dryrun mode %b, revoke mode %b%nGetting list of badges...",
				versionNumber, dryrun, revoke);

		// get properties
		parseProperties();

		// load and parse CSV
		parseInputFile();

		// Init REST client
		try {
			initRestClient();
		} catch (RestClientException e) {
			log.error("Could not initiate REST connection: {}", e.toString());
			System.exit(1);
		}

		// load credly badge templates
		loadBadgeTemplates();

		// print list of badges and get user input
		getBadgeNumberToApply();

		// submit badges
		if (badgeTemplateNumber > -1) {
			log.info("Applying badge {} to {} users",
					badgeLoader.getCredlyTemplates().get(badgeTemplateNumber).getName(), badgeEarners.size());

			SubmitToCredly sm;
			try {
				sm = new SubmitToCredly(restClient);
				numBadgesSubmitted = dryrun ? -1
						: sm.applyAllBadges(badgeLoader.getCredlyTemplates().get(badgeTemplateNumber), badgeEarners);
			} catch (IOException e) {
				log.error("Could not prepare output file for writing: {}", e.toString());
			}
		}

		System.out.printf("Applied %d badge(s)", numBadgesSubmitted);

		Unirest.shutDown();

	}

}
