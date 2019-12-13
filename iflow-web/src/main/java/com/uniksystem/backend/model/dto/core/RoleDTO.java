package com.uniksystem.backend.model.dto.core;

import java.io.Serializable;

public class RoleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String description;
	private String label;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public RoleDTO(Integer id, String description, String label) {
		super();
		this.id = id;
		this.description = description;
		this.label = label;
	}

	public RoleDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

}
