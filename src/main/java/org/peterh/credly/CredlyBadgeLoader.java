package org.peterh.credly;

import java.util.ArrayList;
import java.util.List;

import org.peterh.credly.util.CredlyBadgeTemplate;
import org.peterh.credly.util.RESTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class CredlyBadgeLoader {

	final static Logger log = LoggerFactory.getLogger(CredlyBadgeLoader.class);
	private List<CredlyBadgeTemplate> credlyTemplates;
	
	public int size() {
		return credlyTemplates.size();
	}

	public List<CredlyBadgeTemplate> getCredlyTemplates() {
		return credlyTemplates;
	}

	public void setCredlyTemplates(List<CredlyBadgeTemplate> credlyTemplates) {
		this.credlyTemplates = credlyTemplates;
	}

	/*
	 * Calling this method assumes that the Unirest baseURL has been configured
	 * already.
	 */
	public void loadCredlyBadges(RESTClient r) {
		credlyTemplates = new ArrayList<>();

		HttpResponse<JsonNode> response = r.get("/badge_templates");
		JSONObject o = response.getBody().getObject();
		
				
		if (response.isSuccess()) {
			JSONArray a = o.getJSONArray("data");
			for (int i = 0; i < a.length(); i++) {
				JSONObject o1 = a.getJSONObject(i);
				CredlyBadgeTemplate ct = new CredlyBadgeTemplate(o1.get("id").toString(), o1.get("name").toString());
				credlyTemplates.add(ct);
				
				log.debug("Badge number {}: {} (id: {})", i, o1.get("name"), o1.get("id"));
			}
			log.info("Loaded {} badge templates", credlyTemplates.size());
		} else {
			JSONObject o2 = o.getJSONObject("data");
			String message = o2.getString("message");
			log.error("REST error reading badge templates: {}: {}", response.getStatusText(), message);
			log.debug(response.getBody().toPrettyString());
		}
	}

}
