package org.peterh.credly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kong.unirest.Unirest;

public class CredlyBadgeSubmitter {

	private final static String propsFileName = "config.properties";
	private static String organization, basicAuth, baseURL;

	final static Logger log = LoggerFactory.getLogger(CredlyBadgeSubmitter.class);

	public static void main(String[] args) {

		String filename;
		Scanner keyin;
		int i;
		int templateNumber = -1;
		ParseCSVtoMap cp = new ParseCSVtoMap();

		Properties props = new Properties();
		try {
			// props.load(CredlyBadgeSubmitter.class.getClassLoader().getResourceAsStream(propsFileName));
			props.load(new FileReader(new File(".").getCanonicalPath() + File.separator + "config.properties"));

		} catch (FileNotFoundException e) {
			log.error("Unable to find properties file: {}", e.getMessage());
			System.exit(1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			log.error("Unable to read properties file: {}", e.getMessage());
			System.exit(1);
		}

		filename = args.length == 1 ? args[0] : props.getProperty("InputCSV");

		// Prepare the REST client interface
		baseURL = props.getProperty("baseURL");
		organization = props.getProperty("organization");
		basicAuth = props.getProperty("basicAuth");
		RESTClient r;

		try {

			// Parse the .csv file to create a list of 3-key maps (first/last name and
			// email)
			List<HashMap<String, Object>> badgeEarners = cp.parseFile(filename);
			if (badgeEarners.isEmpty()) {
				log.info("No names to process in file {}", filename);
				throw new Exception("No users to process.");
			}

			r = new RESTClient(organization, baseURL, basicAuth);

			// Load the badges
			CredlyBadgeLoader badgeLoader = new CredlyBadgeLoader();
			badgeLoader.loadCredlyBadges(r);
			if (badgeLoader.getCredlyTemplates().isEmpty()) {
				log.error("No Credly Badge Templates found.");
				throw new Exception("No Credly Badge Templates found.");
			}

			// Print the list of badges
			for (i = 0; i < badgeLoader.size(); i++) {
				System.out.printf("%d: %s\n", i, badgeLoader.getCredlyTemplates().get(i).getName());
			}

			while (templateNumber < 0 || templateNumber > i) {
				System.out.printf("Select the badge number to apply ('0'-'%d' or anything else to exit): ",
						badgeLoader.size());
				keyin = new Scanner(System.in);
				templateNumber = keyin.nextInt();
				System.out.printf("Confirm applying badge %s with id %s (Y/N): ",
						badgeLoader.getCredlyTemplates().get(templateNumber).getName(),
						badgeLoader.getCredlyTemplates().get(templateNumber).getId());
				if (keyin.next().compareToIgnoreCase("y") != 0)
					templateNumber = -1;

			}

			log.info("Applying badge {} to {} users", badgeLoader.getCredlyTemplates().get(templateNumber).getName(),
					badgeEarners.size());

			// submit the badges
			SubmitToCredly sm = new SubmitToCredly(r);

			i = sm.applyAllBadges(badgeLoader.getCredlyTemplates().get(templateNumber), badgeEarners);

			System.out.printf("Applied %d badge(s)", i);

		} catch (RestClientException e1) {
			log.error(e1.toString());
		} catch (Exception e) {
			log.error("Error parsing user input: {}", e.toString());
		}

		finally {
			Unirest.shutDown();
		}
	}

}
