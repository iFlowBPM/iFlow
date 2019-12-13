package com.uniksystem.backend.model.dto.core;

import java.util.Set;

public class UnikUserMeDTO {

    private Integer id;

    private String username;

    private UnikUserDetailDTO detail;

    private Set<RoleDTO> roles;

	public UnikUserMeDTO(Integer id, String username, UnikUserDetailDTO detail, Set<RoleDTO> roles) {
		super();
		this.id = id;
		this.username = username;
		this.detail = detail;
		this.roles = roles;
	}

	public UnikUserMeDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UnikUserDetailDTO getDetail() {
		return detail;
	}

	public void setDetail(UnikUserDetailDTO detail) {
		this.detail = detail;
	}

	public Set<RoleDTO> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleDTO> roles) {
		this.roles = roles;
	}

}
