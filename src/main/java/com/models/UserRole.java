package com.models;

public enum UserRole {
	ADMIN("ROLE_ADMIN"), USER("ROLE_USER");

	private String role;

	UserRole(String role) {
		this.role = role;
	}

	public static UserRole fromString(String text) {
		if (text != null)
			for (UserRole userRole : UserRole.values())
				if (userRole.toString().equalsIgnoreCase(text))
					return userRole;
		return null;
	}

	@Override
	public String toString() {
		return role;
	}

}
