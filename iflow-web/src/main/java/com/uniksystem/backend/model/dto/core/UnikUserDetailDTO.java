package com.uniksystem.backend.model.dto.core;

import java.io.Serializable;
import java.time.LocalDate;

public class UnikUserDetailDTO implements Serializable {

  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public UnikUserDetailDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnikUserDetailDTO(String firstName, String lastName, String email, LocalDate birthday, String mobilePhone) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.birthday = birthday;
		this.mobilePhone = mobilePhone;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private String firstName;

   
    private String lastName;

    
    private String email;

    
    private LocalDate birthday;

    private String mobilePhone;

}
