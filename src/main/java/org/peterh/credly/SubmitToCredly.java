package org.peterh.credly;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.peterh.credly.util.MyLog;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class SubmitToCredly {

	private static final MyLog log = new MyLog();
	private RESTClient r;

	public SubmitToCredly(RESTClient r) {
		this.r=r;
	}

	public String  buildBadgeRequest(String badgeId, HashMap<String, Object> student) {
		String issued_to_first_name, issued_to_last_name, recipient_email, badge_template_id, issued_at;

		issued_to_first_name = "\""+student.get("issued_to_first_name").toString()+"\"";
		issued_to_last_name = "\""+student.get("issued_to_last_name").toString()+"\"";
		recipient_email = "\""+student.get("recipient_email").toString()+"\"";
		badge_template_id = "\""+badgeId+"\"";
		issued_at = "\""+ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)+"\"";

		String body = "{\"badge_template_id\":"+badge_template_id+",\r\n"
				+ "       \"issued_at\":"+issued_at+",\r\n"
				+ "       \"issued_to_first_name\":"+issued_to_first_name+",\r\n"
				+ "       \"issued_to_last_name\":"+issued_to_last_name+",\r\n"
				+ "       \"recipient_email\":"+recipient_email+"}";

		log.debug("%s\n", body);

		return body;

	}

	public int applyAllBadges(CredlyBadgeTemplate ct, List<HashMap<String, Object>> studentList) {
		int badgesApplied=0;

		for (HashMap<String, Object> student : studentList) {
			HttpResponse<JsonNode> response = r.post("/badges",buildBadgeRequest(ct.getId(), student));
			if (response.isSuccess()) {
				badgesApplied++;
				log.info("Applied Badge %s to student email %s", ct.getName(), student.get("recipient_email"));
			} else
			{
				log.info("Failed to apply badge: %s", response.getStatusText());
			}

		}
		return badgesApplied;
	}


}
