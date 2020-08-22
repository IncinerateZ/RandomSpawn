package tech.incineratez.randomspawn.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class RLoc {
    private Location loc;
    private int dist = 0;

    public RLoc(Location loc) {
        this.loc = loc;
    }
    public Location getLoc() {
        return loc;
    }
    public int getDistance() {
        return dist;
    }
    public void setDistance(int dist) {
        this.dist = dist;
    }
    public RLoc[] getNeighbors() {
        World world = loc.getWorld();
        RLoc[] res = new RLoc[4];
        res[0] = new RLoc(new Location(world, loc.getX() + 1, loc.getY(), loc.getZ()));
        res[1] = new RLoc(new Location(world, loc.getX(), loc.getY(), loc.getZ() + 1));
        res[2] = new RLoc(new Location(world, loc.getX() - 1, loc.getY(), loc.getZ()));
        res[3] = new RLoc(new Location(world, loc.getX(), loc.getY(), loc.getZ() - 1));
        return res;
    }
    public String toString() {
        return "X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + " , Z: " + loc.getBlockZ();
    }
}
