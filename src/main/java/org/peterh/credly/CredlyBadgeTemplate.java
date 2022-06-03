package org.peterh.credly;

public class CredlyBadgeTemplate {
	private String id;
	private String badgeName;
	
	public CredlyBadgeTemplate() {}
	
	public CredlyBadgeTemplate(String id, String badgeName) {
		this.id=id;
		this.badgeName=badgeName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return badgeName;
	}

	public void setName(String badgeName) {
		this.badgeName = badgeName;
	}
	
	

}
