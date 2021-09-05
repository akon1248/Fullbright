package com.akon.fullbright;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

public class FullbrightListener extends PacketAdapter {

	private static final ImmutableList<byte[]> LIGHT_DATA;

	static {
		ImmutableList.Builder<byte[]> builder = ImmutableList.builder();
		byte[] byteArr = new byte[2048];
		Arrays.fill(byteArr, (byte)0xFF);
		for (int i = 0; i < 18; i++) {
			builder.add(byteArr);
		}
		LIGHT_DATA = builder.build();
	}

	public FullbrightListener() {
		super(Fullbright.getInstance(), PacketType.Play.Server.LIGHT_UPDATE);
	}

	@Override
	public void onPacketSending(PacketEvent e) {
		if (FullbrightAPI.hasFullbright(e.getPlayer())) {
			e.getPacket().getIntegers().write(3, 0x3FFFF);
			e.getPacket().getIntegers().write(5, 0);
			e.getPacket().getSpecificModifier(List.class).write(1, LIGHT_DATA);
		}
	}

}
