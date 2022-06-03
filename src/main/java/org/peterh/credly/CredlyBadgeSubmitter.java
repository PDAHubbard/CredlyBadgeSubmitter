package org.peterh.credly;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kong.unirest.Unirest;


public class CredlyBadgeSubmitter {
	
	private final static String propsFileName = "config.properties";
	private static  String organization,basicAuth,baseURL;
	
	final static Logger log = LoggerFactory.getLogger(CredlyBadgeSubmitter.class);
	

	public static void main(String[] args) {
		
		String filename;
		Scanner keyin;
		int i;
		int templateNumber = -1;
		ParseCSVtoMap cp = new ParseCSVtoMap();


		Properties props = new Properties();
		try {
			props.load(CredlyBadgeSubmitter.class.getClassLoader().getResourceAsStream(propsFileName));

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);

		}
		
		filename = args.length == 1 ? args[0] : props.getProperty("InputCSV"); 
		
		//Parse the .csv file to create a list of 3-key maps (first/last name and email)
		List<HashMap<String, Object>> badgeEarners = cp.parseFile(filename);
		if (badgeEarners.isEmpty()) {
			log.error("No names to process in file %s", filename);
			System.exit(1);
		}
		
		//Prepare the REST client interface
		baseURL = props.getProperty("baseURL");
		organization = props.getProperty("organization");
		basicAuth = props.getProperty("basicAuth");
		RESTClient r = new RESTClient(organization, baseURL, basicAuth);
		
		//Load the badges
		CredlyBadgeLoader badgeLoader = new CredlyBadgeLoader();
		badgeLoader.loadCredlyBadges(r);
		if (badgeLoader.getCredlyTemplates().isEmpty()) {
			log.info("No Credly Badge Templates found.");
			System.exit(1);
		}
		
		//Print the list of badges
		for (i=0; i<badgeLoader.size(); i++) {
			System.out.printf("%d: %s\n", i, badgeLoader.getCredlyTemplates().get(i).getName());
		}

		while (templateNumber < 0 || templateNumber > i) {
			System.out.print("Select the badge to apply ('q' to exit): ");
			keyin = new Scanner(System.in);
			try {
				templateNumber = keyin.nextInt();
				System.out.printf("Confirm applying badge %s with id %s (Y/N): ",
						badgeLoader.getCredlyTemplates().get(templateNumber).getName(),
						badgeLoader.getCredlyTemplates().get(templateNumber).getId());
				if (keyin.next().compareToIgnoreCase("y") != 0)
					templateNumber = -1;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		log.info("Applying badge {} to {} users", badgeLoader.getCredlyTemplates().get(templateNumber).getName(), badgeEarners.size());

		//submit the badges
		SubmitToCredly sm = new SubmitToCredly(r);
		
		i = sm.applyAllBadges(badgeLoader.getCredlyTemplates().get(templateNumber), badgeEarners);

		log.info("Applied {} badge(s)", i);

		Unirest.shutDown();
	}

}
