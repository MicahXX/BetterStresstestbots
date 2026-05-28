package me.micahcode.betterStresstestbots.nms;

import org.bukkit.Location;

// these are all the properties a fake player has
public interface IFakePlayer {
    void tick();
    void remove();
    void setSpeed(double speed);
    void setRadius(double radius);
    void setGroundMode(boolean groundMode);
    void teleportTo(Location loc);
    void sendChat(String message);
    boolean isAlive();
    String getName();
}