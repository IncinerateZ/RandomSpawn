package tech.incineratez.randomspawn;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import tech.incineratez.randomspawn.commands.RandomSpawnCommands;
import tech.incineratez.randomspawn.events.RandomTPEvent;
import tech.incineratez.randomspawn.runnables.RetrieverRunnable;
import tech.incineratez.randomspawn.runnables.SearchRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RandomSpawn extends JavaPlugin implements Listener {
    public String MCVERSION;

    public File cooldowns = new File(this.getDataFolder() + "/cooldowns.yml");
    public FileConfiguration customConfig;

    public RandomSpawn() {
        this.customConfig = YamlConfiguration.loadConfiguration(this.cooldowns);
    }

    private BukkitTask task;
    private ArrayList<Integer> taskids = new ArrayList<>();
    public boolean enabled = true;

    private int[] cX, cZ;
    private HashSet<Material> bannedBlocks = new HashSet<>();

    public HashMap<Player, Location> foundLocs = new HashMap<>();
    public HashMap<Player, Boolean> forceTps = new HashMap<>();
    public HashMap<Player, PotionEffect> pEffects = new HashMap<>();

    private int configMaxTries, configMaxDist, configCheckOverride;
    private boolean configForceFind, configForceTp;

    public void onEnable() {
        enabled = true;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        new RandomSpawnCommands(this);
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.saveCustomYml(this.customConfig, this.cooldowns);

        //Save configuration
        configForceFind = this.getConfig().getBoolean("force-find");
        configMaxTries = this.getConfig().getInt("max-tries");
        configMaxDist = this.getConfig().getInt("max-distance");
        configCheckOverride = this.getConfig().getInt("max-check-override");
        configForceTp = this.getConfig().getBoolean("force-tp");

        //Get and store banned blocks
        if (this.getConfig().getBoolean("enable-blacklist")) {
            ArrayList<String> blocks = (ArrayList) this.getConfig().getStringList("blocks");
            for(String block : blocks) {
                bannedBlocks.add(Material.getMaterial(block));
            }
        }

        //Get Borders
        this.cX = new int[] {(int)this.getConfig().getDouble("pos1.x"), (int)this.getConfig().getDouble("pos2.x")};
        this.cZ = new int[] {(int)this.getConfig().getDouble("pos1.z"), (int)this.getConfig().getDouble("pos2.z")};
        twoSort(cX);
        twoSort(cZ);

        //Start runnable to check every second
        task = Bukkit.getScheduler().runTaskTimer(this, new RetrieverRunnable(this), 3L, 3L);

        MCVERSION = Bukkit.getServer().getBukkitVersion().substring(0,4);
    }

    public void onDisable() {
        enabled = false;
        Bukkit.getLogger().info("[RandomSpawn] Disabling Tasks");
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        for(int i : taskids) {
            Bukkit.getScheduler().cancelTask(i);
        }
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getLogger().info("[RandomSpawn] Plugin Disabled!");
    }

    public void tp(Player p, Location loc) {
        RandomTPEvent randomTPEvent = new RandomTPEvent(p, loc);
        Bukkit.getPluginManager().callEvent(randomTPEvent);
        if (!randomTPEvent.isCancelled()) {
            int offset = 0;
            if(MCVERSION.equalsIgnoreCase("1.16")) offset = 1;
            p.setFallDistance(0);
            p.removePotionEffect(PotionEffectType.LEVITATION);
            p.teleport(loc.add(0, offset, 0));
        }
    }

    public void saveCustomYml(FileConfiguration ymlConfig, File ymlFile) {
        try {
            ymlConfig.save(ymlFile);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    private void twoSort(int[] nums) {
        int max = Math.max(nums[0], nums[1]);
        int min = nums[0] + nums[1] - max;
        nums[0] = max;
        nums[1] = min;
    }

    public Location getRandomLocation(World world) {
        Random rand = new Random();

        double Tposx = (rand.nextInt(cX[0] - cX[1] + 1) + cX[1]);
        double Tposz = (rand.nextInt(cZ[0] - cZ[1] + 1) + cZ[1]);

        return new Location(world, Tposx + 0.5, 500.0 ,Tposz + 0.5);
    }

    private void getSuitableLocation(World world, Player p) {
        SearchRunnable runnable = new SearchRunnable(this, world, p, configMaxTries, configMaxDist, configCheckOverride, configForceFind, configForceTp);

        BukkitTask t = Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
        taskids.add(t.getTaskId());
    }

    public boolean isSafeLocation(Location loc) {
        int offset = 0;
        World world = loc.getWorld();
        Block block = world.getBlockAt(world.getHighestBlockAt(loc).getLocation().add(0, -1, 0));
        if(MCVERSION.equalsIgnoreCase("1.16")) offset = 1;
        Location f = block.getLocation().add(0, offset, 0);
        Block fb = world.getBlockAt(f);
        return !bannedBlocks.contains(fb.getType());
    }

    public boolean isInBounds(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return (x <= cX[0] && x >= cX[1] && z <= cZ[0] && z >= cZ[1]);
    }

    public void tpRandom(Player p) {
        World world;
        if (this.getConfig().getString("world").equals("None")) {
            world = p.getWorld();
        } else {
            world = Bukkit.getServer().getWorld(this.getConfig().getString("world"));
        }

        /*
        *   Choose location
        *   If chosen location is not safe, do a bfs around the location for a safe location
        *   If bfs exceeds width of 16, choose another location
        *   If tries exceed 50, return error not found
        */

        Location loc = getRandomLocation(world);

        if (this.getConfig().getBoolean("enable-blacklist")) {
            if(!isSafeLocation(loc)) {
                getSuitableLocation(world, p);
            } else {
                int tY = world.getHighestBlockYAt(loc) + 1;
                loc.setY(tY);
                p.teleport(loc);
            }
        } else {
            int tY = world.getHighestBlockYAt(loc) + 1;
            loc.setY(tY);
            p.teleport(loc);
        }

    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            this.tpRandom(p);
        }

    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void onDeath(EntityDamageEvent e) {
        if (this.getConfig().getBoolean("on-death")) {
            if (e.getEntity() instanceof Player) {
                final Player p = (Player)e.getEntity();
                if (p.getHealth() - e.getFinalDamage() <= 0.0D) {
                    Iterator var3;
                    PotionEffect effect;
                    if (p.getBedSpawnLocation() != null && this.getConfig().getBoolean("bed-spawn")) {
                        e.setDamage(0.0D);
                        p.setHealth(20.0D);
                        p.setSaturation(20.0F);
                        var3 = p.getActivePotionEffects().iterator();

                        while(var3.hasNext()) {
                            effect = (PotionEffect)var3.next();
                            p.removePotionEffect(effect.getType());
                        }

                        p.teleport(p.getBedSpawnLocation());
                        Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                            public void run() {
                                p.setFireTicks(0);
                            }
                        }, 1L);
                    } else {
                        e.setDamage(0.0D);
                        p.setHealth(20.0D);
                        p.setSaturation(20.0F);
                        var3 = p.getActivePotionEffects().iterator();

                        while(var3.hasNext()) {
                            effect = (PotionEffect)var3.next();
                            p.removePotionEffect(effect.getType());
                        }

                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            public void run() {
                                RandomSpawn.this.tpRandom(p);
                                p.setFireTicks(0);
                            }
                        }, 1L);
                    }

                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void onRespawn(PlayerRespawnEvent e) {
        if (!this.getConfig().getBoolean("on-death")) {
            final Player p = e.getPlayer();
            if (p.getBedSpawnLocation() == null || !this.getConfig().getBoolean("bed-spawn")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        RandomSpawn.this.tpRandom(p);
                    }
                }, 1L);
            }

        }
    }
}
