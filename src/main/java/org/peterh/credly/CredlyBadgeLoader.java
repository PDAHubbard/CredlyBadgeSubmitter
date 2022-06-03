package org.peterh.credly;

import java.util.ArrayList;
import java.util.List;

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
		credlyTemplates = new ArrayList<CredlyBadgeTemplate>();

		HttpResponse<JsonNode> response = r.get("/badge_templates");
				
		if (response.isSuccess()) {
			JSONObject o = response.getBody().getObject();
			JSONArray a = o.getJSONArray("data");
			for (int i = 0; i < a.length(); i++) {
				JSONObject o1 = a.getJSONObject(i);
				CredlyBadgeTemplate ct = new CredlyBadgeTemplate(o1.get("id").toString(), o1.get("name").toString());
				credlyTemplates.add(ct);
				log.debug("{}: {} (id: {})", i, o1.get("name"), o1.get("id"));

			}
			log.info("Loaded {} badge templates", credlyTemplates.size());
		} else {
			log.error("REST error reading badge templates: {}", response.getStatusText());
		}
	}

}
