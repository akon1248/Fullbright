package com.akon.fullbright;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@UtilityClass
public class FullbrightAPI {

	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private final NamespacedKey KEY = new NamespacedKey(Fullbright.getInstance(), "fullbright");
	private final Class<?> CHUNK_COORD_CLASS = MinecraftReflection.getMinecraftClass("ChunkCoordIntPair");
	private final Class<?> LIGHT_ENGINE_CLASS = MinecraftReflection.getMinecraftClass("LightEngine");
	private final Class<?> CHUNK_PROVIDER_SERVER_CLASS = MinecraftReflection.getChunkProviderServer();
	private final ConstructorAccessor CHUNK_COORD_CONSTRUCTOR = Accessors.getConstructorAccessor(CHUNK_COORD_CLASS, int.class, int.class);
	private final ConstructorAccessor LIGHT_UPDATE_PACKET_CONSTRUCTOR = Accessors.getConstructorAccessor(PacketType.Play.Server.LIGHT_UPDATE.getPacketClass(), CHUNK_COORD_CLASS, LIGHT_ENGINE_CLASS, boolean.class);
	private final MethodAccessor GET_CHUNK_PROVIDER = Accessors.getMethodAccessor(FuzzyReflection.fromClass(MinecraftReflection.getWorldServerClass()).getMethod(FuzzyMethodContract.newBuilder().returnDerivedOf(CHUNK_PROVIDER_SERVER_CLASS).build()));
	private final MethodAccessor GET_LIGHT_ENGINE = Accessors.getMethodAccessor(FuzzyReflection.fromClass(CHUNK_PROVIDER_SERVER_CLASS).getMethod(FuzzyMethodContract.newBuilder().returnDerivedOf(LIGHT_ENGINE_CLASS).build()));

	public void setFullbright(Player player, boolean fullbright) {
		player.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte)(fullbright ? 1 : 0));
		updateLighting(player);
	}

	public boolean hasFullbright(Player player) {
		return Optional.ofNullable(player.getPersistentDataContainer().get(KEY, PersistentDataType.BYTE)).orElseGet(() -> {
			byte def = Fullbright.getInstance().getConfig().getBoolean("default", true) ? (byte)1 : (byte)0;
			player.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, def);
			return def;
		}) > 0;
	}

	void updateLighting(Player player) {
		Object lightEngine = GET_LIGHT_ENGINE.invoke(GET_CHUNK_PROVIDER.invoke(BukkitUnwrapper.getInstance().unwrapItem(player.getWorld())));
		Stream<PacketContainer> packets = Arrays.stream(player.getWorld().getLoadedChunks())
			.map(chunk -> CHUNK_COORD_CONSTRUCTOR.invoke(chunk.getX(), chunk.getZ()))
			.map(chunkCoord -> PacketContainer.fromPacket(LIGHT_UPDATE_PACKET_CONSTRUCTOR.invoke(chunkCoord, lightEngine, true)));
		EXECUTOR_SERVICE.execute(() -> packets.forEach(packet -> {
			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			}
		}));
	}

}