package org.peterh.credly;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import org.peterh.credly.util.CredlyBadgeTemplate;
import org.peterh.credly.util.RESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

public class SubmitToCredly {

	final static Logger log = LoggerFactory.getLogger(SubmitToCredly.class);
	private RESTClient r;

	public SubmitToCredly(RESTClient r) {
		this.r = r;
	}

	public String buildBadgeRequest(String badgeId, HashMap<String, Object> student) {
		String issued_to_first_name;
		String issued_to_last_name;
		String recipient_email;
		String badge_template_id;
		String issued_at;
		String body;

		issued_to_first_name = "\"" + student.get("issued_to_first_name").toString() + "\"";
		issued_to_last_name = "\"" + student.get("issued_to_last_name").toString() + "\"";
		recipient_email = "\"" + student.get("recipient_email").toString() + "\"";
		badge_template_id = "\"" + badgeId + "\"";
		issued_at = "\""
				+ ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
				+ "\"";

		body = "{\"badge_template_id\":" + badge_template_id + ",\r\n" + "       \"issued_at\":" + issued_at
				+ ",\r\n" + "       \"issued_to_first_name\":" + issued_to_first_name + ",\r\n"
				+ "       \"issued_to_last_name\":" + issued_to_last_name + ",\r\n" + "       \"recipient_email\":"
				+ recipient_email + "}";

		log.debug("{}", body);

		return body;
	}

	public int applyAllBadges(CredlyBadgeTemplate ct, List<HashMap<String, Object>> studentList) {
		int badgesApplied = 0;
		JSONObject responseBody;
		String badgeId;

		for (HashMap<String, Object> student : studentList) {
			HttpResponse<JsonNode> response = r.post("/badges", buildBadgeRequest(ct.getId(), student));
			if (response.isSuccess()) {
				badgesApplied++;
				
				//get badge id and user email
				responseBody = response.getBody().getObject();
				badgeId = responseBody.getJSONObject("data").getString("id");
				System.out.println("Badge awarded: {recipient_email: \""+student.get("recipient_email")+"\", badge_id: \""+badgeId+"\"}");
				
				log.info("Applied badge id '{}' to '{}'", badgeId, student.get("recipient_email"));
				log.debug(response.getBody().toPrettyString());
				
			} else {
				
				log.error("Failed to apply badge to {}: {}", student.get("recipient_email"), response.getBody().getObject().getJSONObject("data").getString("message"));
				log.debug(response.getBody().toPrettyString());
			}

		}
		return badgesApplied;
	}

}
