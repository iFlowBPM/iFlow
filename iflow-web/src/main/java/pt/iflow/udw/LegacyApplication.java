package pt.iflow.udw;

import java.util.ArrayList;

public class LegacyApplication {
	private boolean active;
	private String description;
	private float id;
	private String label;
	ArrayList<Object> legacyUsers = new ArrayList<Object>();
	private String logo;
	ArrayList<Object> menus = new ArrayList<Object>();
	private String type;

	// Getter Methods

	public boolean getActive() {
		return active;
	}

	public String getDescription() {
		return description;
	}

	public float getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getLogo() {
		return logo;
	}

	public String getType() {
		return type;
	}

	// Setter Methods

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(float id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public void setType(String type) {
		this.type = type;
	}
}
