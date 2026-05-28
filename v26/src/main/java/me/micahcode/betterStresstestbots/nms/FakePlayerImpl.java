package me.micahcode.betterStresstestbots.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class FakePlayerImpl implements IFakePlayer {

    private final ServerPlayer nmsPlayer;
    private final double spawnX, spawnY, spawnZ;
    private double targetX, targetY, targetZ;
    private final Random random = new Random();
    private double speed = 0.1;
    private double radius = 500.0;
    private boolean groundMode = false;

    public FakePlayerImpl(String name, Location spawn, Logger logger) {
        this.spawnX = spawn.getX();
        this.spawnY = spawn.getY();
        this.spawnZ = spawn.getZ();

        MinecraftServer server = ((CraftServer) org.bukkit.Bukkit.getServer()).getServer();
        ServerLevel level = ((CraftWorld) spawn.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);

        nmsPlayer = new ServerPlayer(server, level, profile, ClientInformation.createDefault());

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);

        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        nmsPlayer.connection = new ServerGamePacketListenerImpl(server, connection, nmsPlayer, cookie);

        try {
            server.getPlayerList().placeNewPlayer(connection, nmsPlayer, cookie);
        } catch (Exception e) {
            logger.warning("Failed to place bot " + name + ": " + e.getMessage());
            return;
        }

        nmsPlayer.setGameMode(GameType.CREATIVE);
        nmsPlayer.setNoGravity(true);
        nmsPlayer.snapTo(spawnX, spawnY, spawnZ, 0f, 0f);
        pickNewTarget();
    }

    private void pickNewTarget() {
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = random.nextDouble() * radius;
        targetX = spawnX + Math.cos(angle) * dist;
        targetZ = spawnZ + Math.sin(angle) * dist;
        targetY = groundMode ? spawnY : Math.max(64, Math.min(250, spawnY + random.nextDouble() * 100 + 20));
    }

    @Override
    public void tick() {
        if (nmsPlayer == null || !nmsPlayer.isAlive()) return;
        double dx = targetX - nmsPlayer.getX();
        double dz = targetZ - nmsPlayer.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist < 2.0) {
            pickNewTarget();
            return;
        }

        double nx = dx / horizDist * speed;
        double nz = dz / horizDist * speed;
        double newX = nmsPlayer.getX() + nx;
        double newZ = nmsPlayer.getZ() + nz;
        double newY;

        if (groundMode) {
            newY = getSurfaceY(newX, newZ);
        } else {
            double dy = targetY - nmsPlayer.getY();
            double totalDist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            newY = nmsPlayer.getY() + (dy / totalDist * speed);
        }
        nmsPlayer.snapTo(newX, newY, newZ, nmsPlayer.getYRot(), nmsPlayer.getXRot());
    }

    private double getSurfaceY(double x, double z) {
        try {
            int y = ((ServerLevel) nmsPlayer.level()).getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            return Math.max(y, 60);
        } catch (Exception e) {
            return spawnY;
        }
    }

    @Override
    public void sendChat(String message) {
        if (nmsPlayer == null || !nmsPlayer.isAlive()) return;
        try {
            nmsPlayer.getBukkitEntity().chat(message);
        } catch (Exception ignored) {}
    }

    @Override
    public void teleportTo(Location loc) {
        if (nmsPlayer == null) return;
        double y = groundMode ? getSurfaceY(loc.getX(), loc.getZ()) : loc.getY();
        nmsPlayer.snapTo(loc.getX(), y, loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public void remove() {
        try {
            if (nmsPlayer != null && nmsPlayer.connection != null)
                nmsPlayer.connection.disconnect(Component.literal("Stress bot removed"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setSpeed(double s) {
        this.speed = s;
    }

    @Override
    public void setRadius(double r) {
        this.radius = r;
        pickNewTarget();
    }

    @Override
    public void setGroundMode(boolean g) {
        this.groundMode = g;
        pickNewTarget();
    }

    @Override
    public boolean isAlive() {
        return nmsPlayer != null && nmsPlayer.isAlive();
    }

    @Override
    public String getName() {
        return nmsPlayer != null ? nmsPlayer.getGameProfile().name() : "unknown";
    }
}