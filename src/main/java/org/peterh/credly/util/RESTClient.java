package org.peterh.credly.util;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class RESTClient {

	private String organization, baseUrl, basicAuth;

	public RESTClient(String org, String bUrl, String bAuth) throws RestClientException {
		
		if (bUrl==null || bUrl.length()<1)
			throw new RestClientException("Base URL was null or not specified, check the 'baseURL' property.");
		
		this.organization = org;
		this.baseUrl = bUrl;
		this.basicAuth = bAuth;

		Unirest.config().defaultBaseUrl(this.baseUrl);
	}

	public void shutDown() {
		Unirest.shutDown();
	}

	public HttpResponse<JsonNode> get(String path) {
		return Unirest.get("/organizations/" + organization + path).basicAuth(basicAuth, "").asJson();
	}

	public HttpResponse<JsonNode> post(String path, String body) {
		return Unirest.post("/organizations/" + organization + path).header("Content-Type", "application/json")
				.basicAuth(basicAuth, "").body(body).asJson();
	}

}
