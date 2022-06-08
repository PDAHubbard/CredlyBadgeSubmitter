package org.peterh.credly;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.peterh.credly.util.MyLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseCSVtoMap {

	static final String fileFormat = "\n*** File {} does not exist or is incorrect. File must by csv with the following header:\n"
			+ "Last Name,First Name,Email\n"
			+ "*** Export this in Thought Industries under Reporting>Events>Download CSV.\n";

	static final String cla = "Enter the .csv filename";


	final static Logger log = LoggerFactory.getLogger(ParseCSVtoMap.class);


	public List<HashMap<String, Object>> parseFile(String fn) {
		String line;

		log.info("Parsing file {}", fn);

		List<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(fn));

			// Check the first line
			line = br.readLine();
			if (line!=null && line.compareTo("Last Name,First Name,Email") == 0) {
				while ((line = br.readLine()) != null) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					log.debug("Read {}", line);
					String[] data = line.split(",");
					map.put("issued_to_last_name", data[0]);
					map.put("issued_to_first_name", data[1]);
					map.put("recipient_email", data[2]);
					mapList.add(map);
				}
			} else {
				log.error(fileFormat, fn);
			}

			br.close();
		} catch (FileNotFoundException e) {
			log.error(fileFormat, fn);
			log.error(e.toString());
		} catch (IOException e) {
			log.error(fileFormat, fn);
			log.error(e.toString());
		} finally {

		}
		log.info("Read {} lines", mapList.size());
		return mapList;

	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * ParseCSVtoMap cp = new ParseCSVtoMap();
	 * 
	 * boolean parsing = true; String filename = args.length == 1 ? args[0] :
	 * "C:\\Users\\Peter Hubbard\\Downloads\\export.csv.csv"; // cp.getFilename();
	 * 
	 * while (parsing) {
	 * 
	 * // parse the file List<HashMap<String, Object>> o = cp.parseFile(filename);
	 * for (HashMap<String, Object> h : o) { log.debug("Email: %s",
	 * h.get("recipient_email")); } // valid?
	 * 
	 * // not valid: // System.out.printf(fileFormat, filename); parsing = false; }
	 * 
	 * }
	 */

}
