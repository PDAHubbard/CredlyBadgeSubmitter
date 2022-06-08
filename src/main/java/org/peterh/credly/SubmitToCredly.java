package org.peterh.credly;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class SubmitToCredly {

	final static Logger log = LoggerFactory.getLogger(SubmitToCredly.class);
	private RESTClient r;

	public SubmitToCredly(RESTClient r) {
		this.r = r;
	}

	public String buildBadgeRequest(String badgeId, HashMap<String, Object> student) {
		String issued_to_first_name, issued_to_last_name, recipient_email, badge_template_id, issued_at;

		issued_to_first_name = "\"" + student.get("issued_to_first_name").toString() + "\"";
		issued_to_last_name = "\"" + student.get("issued_to_last_name").toString() + "\"";
		recipient_email = "\"" + student.get("recipient_email").toString() + "\"";
		badge_template_id = "\"" + badgeId + "\"";
		issued_at = "\""
				+ ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
				+ "\"";

		String body = "{\"badge_template_id\":" + badge_template_id + ",\r\n" + "       \"issued_at\":" + issued_at
				+ ",\r\n" + "       \"issued_to_first_name\":" + issued_to_first_name + ",\r\n"
				+ "       \"issued_to_last_name\":" + issued_to_last_name + ",\r\n" + "       \"recipient_email\":"
				+ recipient_email + "}";

		log.debug("{}", body);

		return body;

	}

	public int applyAllBadges(CredlyBadgeTemplate ct, List<HashMap<String, Object>> studentList) {
		int badgesApplied = 0;

		for (HashMap<String, Object> student : studentList) {
			HttpResponse<JsonNode> response = r.post("/badges", buildBadgeRequest(ct.getId(), student));
			if (response.isSuccess()) {
				badgesApplied++;
				log.info("Applied Badge {} to student email {}", ct.getName(), student.get("recipient_email"));
				log.debug(response.getBody().toPrettyString());
			} else {
				log.error("Failed to apply badge to {}: {}", student.get("recipient_email"), response.getStatusText());
				log.debug(response.getBody().toPrettyString());
			}

		}
		return badgesApplied;
	}

}
