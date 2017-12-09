package com.models;

import java.util.Arrays;

public class ChunkData {

	private Handshake handshake;
	private String[] commands;

	public ChunkData() {
	}

	public ChunkData(Handshake hs, String[] commands) {
		this.handshake = hs;
		this.commands = commands;
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
		if (obj instanceof ChunkData) {
			ChunkData chunkData = (ChunkData) obj;
			if (chunkData.handshake.equals(handshake) && chunkData.commands.equals(commands))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "ChunkData [hs=" + handshake + ", commands=" + Arrays.toString(commands) + "]";
	}
}
