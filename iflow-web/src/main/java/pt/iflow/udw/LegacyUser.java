package pt.iflow.udw;

import java.util.ArrayList;

public class LegacyUser {
	private boolean active;
	private float id;
	LegacyApplication LegacyApplicationObject;
	ArrayList<Object> unikUsers = new ArrayList<Object>();
	private String username;

	// Getter Methods

	public boolean getActive() {
		return active;
	}

	public float getId() {
		return id;
	}

	public LegacyApplication getLegacyApplication() {
		return LegacyApplicationObject;
	}

	public String getUsername() {
		return username;
	}

	// Setter Methods

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setId(float id) {
		this.id = id;
	}

	public void setLegacyApplication(LegacyApplication legacyApplicationObject) {
		this.LegacyApplicationObject = legacyApplicationObject;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
