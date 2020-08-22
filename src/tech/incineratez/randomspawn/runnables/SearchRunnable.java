package tech.incineratez.randomspawn.runnables;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import sun.misc.Queue;
import tech.incineratez.randomspawn.RandomSpawn;
import tech.incineratez.randomspawn.utils.ChatUtils;
import tech.incineratez.randomspawn.utils.Pair;
import tech.incineratez.randomspawn.utils.RLoc;

import java.util.HashSet;

public class SearchRunnable implements Runnable {
    private RandomSpawn plugin;
    private World world;
    private Player p;

    private HashSet<Pair> visited = new HashSet<>();

    private int loadCount = 0;
    private int tries = 0;
    private int c = 0;
    private int maxTries, maxDistance, maxOverride;

    private boolean cEnabled = true;
    private boolean stopped = false;
    private boolean forceFind;
    private boolean forceTp;

    private Location pLoc;

    public SearchRunnable(RandomSpawn plugin, World world, Player p, int maxTries, int maxDistance, int maxOverride, boolean forcefind, boolean forcetp) {
        this.plugin = plugin;
        this.world = world;
        this.p = p;
        this.maxTries = maxTries;
        this.maxDistance = maxDistance;
        this.maxOverride = maxOverride;
        this.forceFind = forcefind;
        this.forceTp = forcetp;
        pLoc = p.getLocation();
    }

    private void sendResult(Player p, Location loc, boolean loader, boolean forceTp) {
        if(!loader) {
            Location l = world.getHighestBlockAt(loc).getLocation();
            loc.setY(l.getY());
        } else {
            loadCount++;
        }
        if(loader && loadCount >= 3) return;
        plugin.foundLocs.put(p, loc);
        if(forceTp) plugin.forceTps.put(p, true);
    }

    private void sendEffect(Player p, PotionEffect effect) {
        plugin.pEffects.put(p, effect);
    }

    private void enqueueNeighbors(Queue<RLoc> q, RLoc center) {
        RLoc[] neighbors = center.getNeighbors();
        for(RLoc n : neighbors) {
            if(!plugin.isInBounds(n.getLoc())) continue;
            Pair c = new Pair(n.getLoc().getBlockX(), n.getLoc().getBlockZ());
            if(visited.contains(c)) continue;
            n.setDistance(center.getDistance() + 1);
            q.enqueue(n);
            visited.add(c);
        }
    }

    private void debugHSPrint(HashSet<Pair> arr) {
        String res = "";
        for(Pair p : arr) {
            res += ("[" + p.getA() + ",");
            res += (p.getB() + "] ");
        }
        System.out.println(res);
    }

    private void debugArrayPrint(int[] arr) {
        String res = "";
        for(int i : arr) {
            res += i;
            res += " ";
        }
        System.out.println(res);
    }

    private Location bfsSearch(Queue<RLoc> q) throws InterruptedException {
        while(!q.isEmpty()) {
            c++;
            RLoc next = q.dequeue();
            //Check if dist exceeds max distance
            if(next.getDistance() > maxDistance) {
                return null;
            } else {
                //Check if location is safe
                if(plugin.isSafeLocation(next.getLoc())) {
                    return next.getLoc();
                }
            }
            //enqueue next neighbors
            enqueueNeighbors(q, next);
        }
        return null;
    }

    @Override
    public void run() {
        while(((tries <= maxTries && tries <= maxOverride) || (forceFind && tries <= maxOverride)) && cEnabled) {
            Location center = plugin.getRandomLocation(world);
            RLoc c = new RLoc(center);
            Pair pair = new Pair(c.getLoc().getBlockX(), c.getLoc().getBlockY());
            sendResult(p, center, true, false);
            sendEffect(p, new PotionEffect(PotionEffectType.LEVITATION, 20, 255));
            if(!visited.contains(pair)) {
                if (plugin.isSafeLocation(center)) {
                    p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("teleport")));
                    sendResult(p, center, false, false);
                    return;
                } else {
                    Queue<RLoc> q = new Queue<>();
                    c.setDistance(0);
                    enqueueNeighbors(q, c);
                    try {
                        Location res = bfsSearch(q);
                        if (res != null) {
                            p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("teleport")));
                            sendResult(p, res, false, false);
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            tries++;
            cEnabled = plugin.enabled;
        }
        stopped = true;
        if(!forceTp) {
            sendResult(p, pLoc, false, false);
            p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("not-found").replace("%checks%", "" + c)));
        } else {
            p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("teleport")));
            sendResult(p, plugin.getRandomLocation(world), false, true);
        }
    }
}
