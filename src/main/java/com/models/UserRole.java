package com.models;

public enum UserRole {
	ROLE_ADMIN("admin"), ROLE_USER("user");

	private final String role;

	UserRole(String role) {
		this.role = role;
	}

	public static UserRole fromString(String value) {
		for (UserRole v : values())
			if (v.toString().equalsIgnoreCase(value))
				return v;
		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return role;
	}

}
