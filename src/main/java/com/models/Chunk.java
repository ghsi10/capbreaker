package com.models;

import java.util.Arrays;
import java.util.UUID;

public class Chunk {
	private UUID uuid;
	private Handshake handshake;
	private String[] commands;

	public Chunk() {
	}

	public Chunk(UUID uuid, Handshake handshake, String[] commands) {

		this.handshake = handshake;
		this.commands = commands;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Handshake getHandshake() {
		return handshake;
	}

	public void setHandshake(Handshake handshake) {
		this.handshake = handshake;
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Chunk)
			return ((Chunk) obj).uuid.equals(uuid);
		return false;
	}

	@Override
	public String toString() {
		return "Chunk [uuid=" + uuid + ", handshake=" + handshake + ", commands=" + Arrays.toString(commands) + "]";
	}
}
