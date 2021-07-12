package me.teakivy.vanillatweaks;

import me.teakivy.vanillatweaks.Commands.*;
import me.teakivy.vanillatweaks.Commands.TabCompleter.chTab;
import me.teakivy.vanillatweaks.Commands.TabCompleter.duraPingTab;
import me.teakivy.vanillatweaks.Commands.TabCompleter.homeTab;
import me.teakivy.vanillatweaks.Commands.TabCompleter.vtTab;
import me.teakivy.vanillatweaks.CraftingTweaks.CraftingRegister;
import me.teakivy.vanillatweaks.Events.UpdateJoinAlert;
import me.teakivy.vanillatweaks.Packs.AntiCreeperGreif.AntiCreeper;
import me.teakivy.vanillatweaks.Packs.AntiEndermanGrief.AntiEnderman;
import me.teakivy.vanillatweaks.Packs.AntiGhastGrief.AntiGhast;
import me.teakivy.vanillatweaks.Packs.CauldronConcrete.ConcreteConverter;
import me.teakivy.vanillatweaks.Packs.CoordsHud.DisplayHud;
import me.teakivy.vanillatweaks.Packs.CountMobDeaths.CountDeaths;
import me.teakivy.vanillatweaks.Packs.DoubleShulkerShells.DoubleShulkers;
import me.teakivy.vanillatweaks.Packs.DragonDrops.DragonDrops;
import me.teakivy.vanillatweaks.Packs.DurabilityPing.DuraPing;
import me.teakivy.vanillatweaks.Packs.MoreMobHeads.MobHeads;
import me.teakivy.vanillatweaks.Packs.MultiplayerSleep.MultiplayerSleep;
import me.teakivy.vanillatweaks.Packs.PlayerHeadDrops.HeadDrop;
import me.teakivy.vanillatweaks.Packs.SilenceMobs.Silencer;
import me.teakivy.vanillatweaks.Packs.SpectatorConduitPower.ConduitPower;
import me.teakivy.vanillatweaks.Packs.SpectatorNightVision.NightVision;
import me.teakivy.vanillatweaks.Packs.UnlockAllRecipes.UnlockRecipes;
import me.teakivy.vanillatweaks.Packs.VillagerDeathMessages.VillagerDeath;
import me.teakivy.vanillatweaks.Packs.WanderingTrades.Trades;
import me.teakivy.vanillatweaks.Packs.XPManagement.XPManagement;
import me.teakivy.vanillatweaks.Utils.ConfigUpdater.ConfigUpdater;
import me.teakivy.vanillatweaks.Utils.DataManager.DataManager;
import me.teakivy.vanillatweaks.Utils.Logger.Logger;
import me.teakivy.vanillatweaks.Utils.Metrics;
import me.teakivy.vanillatweaks.Utils.UpdateChecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    String[] packList = {"player-head-drops", "double-shulker-shells", "dragon-drops", "silence-mobs", "anti-creeper-grief", "anti-enderman-grief", "anti-ghast-grief", "nether-portal-coords", "coords-hud", "spectator-night-vision", "spectator-conduit-power", "kill-boats", "more-mob-heads", "multiplayer-sleep", "unlock-all-recipes", "cauldron-concrete", "real-time-clock", "villager-death-messages", "wandering-trades", "tpa", "spawn", "homes", "durability-ping"};

    public static ArrayList<UUID> chEnabled = new ArrayList<>();
    public Boolean newVersionAvaliable = false;
    public String latestVTVersion;

    public DataManager data;

    @Override
    public void onEnable() {

        // Metrics

        Metrics metrics = new Metrics(this, 12001);

        // Update Config.yml
        if (this.getConfig().getInt("config.version") < this.getConfig().getDefaults().getInt("config.version")) {
            try {
                ConfigUpdater.update(this, "config.yml", new File(this.getDataFolder(), "config.yml"), Collections.emptyList());
                this.reloadConfig();
                System.out.println("[VT] Updated Config to Version: " + this.getConfig().getInt("config.version"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Update Checker
        getServer().getPluginManager().registerEvents(new UpdateJoinAlert(), this);

        String latestVersion = null;
        try {
            latestVersion = new UpdateChecker(this, 94021).getLatestVersion();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            Logger.log(Logger.LogLevel.ERROR, ChatColor.RED + "Could not get latest version!");
        }
        String thisVersion = this.getDescription().getVersion();
        if (!thisVersion.equalsIgnoreCase(latestVersion)) {
            Logger.log(Logger.LogLevel.WARNING, "[VT] Vanilla Tweaks has an update!\nPlease update to the latest version (" + latestVersion + ")\n" + ChatColor.YELLOW + "https://www.spigotmc.org/resources/vanilla-tweaks.94021/");
            newVersionAvaliable = true;
            latestVTVersion = latestVersion;
        }


        // Crafting Tweaks
        CraftingRegister.register();

        // Data Manager
        this.data = new DataManager(this);
        data.saveDefaultConfig();

        // Commands
        this.getCommand("vt").setExecutor(new vtCommand());
        this.getCommand("portal").setExecutor(new portalCommand());
        this.getCommand("ch").setExecutor(new chCommand());
        this.getCommand("nv").setExecutor(new nvCommand());
        this.getCommand("cp").setExecutor(new cpCommand());
        this.getCommand("killboat").setExecutor(new KillBoatsCommand());
        this.getCommand("test").setExecutor(new testCommand());
        this.getCommand("rtc").setExecutor(new rtcCommand());
        this.getCommand("spawn").setExecutor(new SpawnCommand());
        this.getCommand("tpa").setExecutor(new tpaCommand());
        this.getCommand("home").setExecutor(new HomeCommand());
        this.getCommand("duraping").setExecutor(new duraPingCommand());

//        this.getCommand("armorstand").setExecutor(new ArmorstandCommand());

        // Tab Completer
        this.getCommand("vt").setTabCompleter(new vtTab());
        this.getCommand("ch").setTabCompleter(new chTab());
        this.getCommand("home").setTabCompleter(new homeTab());
        this.getCommand("duraping").setTabCompleter(new duraPingTab());

        // Config
        this.saveDefaultConfig();

        boolean displayedFirstSpace = false;
        for (String pack : packList) {
            if (this.getConfig().getBoolean("packs." + pack + ".enabled")) {
                if (!displayedFirstSpace) System.out.println("");
                displayedFirstSpace = true;

                System.out.println("[VT] " + getPackName(pack) + " Enabled!");
            }
        }
        if (displayedFirstSpace) System.out.println("");


        // Packs
        getServer().getPluginManager().registerEvents(new HeadDrop(), this);
        getServer().getPluginManager().registerEvents(new DoubleShulkers(), this);
        getServer().getPluginManager().registerEvents(new DragonDrops(), this);
        getServer().getPluginManager().registerEvents(new Silencer(), this);
        getServer().getPluginManager().registerEvents(new AntiCreeper(), this);
        getServer().getPluginManager().registerEvents(new AntiGhast(), this);
        getServer().getPluginManager().registerEvents(new DisplayHud(), this);
        getServer().getPluginManager().registerEvents(new AntiEnderman(), this);
        getServer().getPluginManager().registerEvents(new NightVision(), this);
        getServer().getPluginManager().registerEvents(new ConduitPower(), this);
        getServer().getPluginManager().registerEvents(new MobHeads(), this);
        getServer().getPluginManager().registerEvents(new MultiplayerSleep(), this);
        getServer().getPluginManager().registerEvents(new ConcreteConverter(), this);
        getServer().getPluginManager().registerEvents(new VillagerDeath(), this);
        getServer().getPluginManager().registerEvents(new Trades(), this);
        getServer().getPluginManager().registerEvents(new XPManagement(), this);
        getServer().getPluginManager().registerEvents(new UpdateJoinAlert(), this);
        getServer().getPluginManager().registerEvents(new UnlockRecipes(), this);
        getServer().getPluginManager().registerEvents(new CountDeaths(), this);

        getServer().getPluginManager().registerEvents(new DuraPing(), this);




        // Coords HUD
        for (String uuid : data.getConfig().getStringList("chEnabled")) {
            chEnabled.add(UUID.fromString(uuid));
        }

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (this.getConfig().getBoolean("packs.coords-hud.enabled")) {
                for (UUID uuid : chEnabled) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;
                    if (player.isOnline())
                        DisplayHud.showHud(player);
                }
            }
        }, 0L, 1L);


        // Plugin startup logic
        System.out.println("[VT] Vanilla Tweaks Started!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("[VT] Vanilla Tweaks Shutting Down...");

        List<String> list = new ArrayList<>();
        for (UUID uuid : chEnabled) {
            list.add(uuid.toString());
        }
        data.getConfig().set("chEnabled", list);
        try {
            data.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getPackName(String pack) {
        if (pack.equals("player-head-drops")) {
            return "Player Head Drops";
        }
        if (pack.equals("double-shulker-shells")) {
            return "Double Shulker Shells";
        }
        if (pack.equals("dragon-drops")) {
            return "Dragon Drops";
        }
        if (pack.equals("silence-mobs")) {
            return "Silence Mobs";
        }
        if (pack.equals("anti-creeper-grief")) {
            return "Anti Creeper Grief";
        }
        if (pack.equals("anti-enderman-grief")) {
            return "Anti Enderman Grief";
        }
        if (pack.equals("anti-ghast-grief")) {
            return "Anti Ghast Grief";
        }
        if (pack.equals("nether-portal-coords")) {
            return "Nether Portal Coords";
        }
        if (pack.equals("coords-hud")) {
            return "Coords HUD";
        }
        if (pack.equals("spectator-night-vision")) {
            return "Spectator Night Vision";
        }
        if (pack.equals("spectator-conduit-power")) {
            return "Spectator Conduit Power";
        }
        if (pack.equals("kill-boats")) {
            return "Kill Boats";
        }
        if (pack.equals("more-mob-heads")) {
            return "More Mob Heads";
        }
        if (pack.equals("multiplayer-sleep")) {
            return "Multiplayer Sleep";
        }
        if (pack.equals("unlock-all-recipes")) {
            return "Unlock All Recipes";
        }
        if (pack.equals("cauldron-concrete")) {
            return "Cauldron Concrete";
        }
        if (pack.equals("real-time-clock")) {
            return "Real Time CLock";
        }
        if (pack.equals("villager-death-messages")) {
            return "Villager Death Messages";
        }
        if (pack.equals("wandering-trades")) {
            return "Wandering Trades";
        }
        if (pack.equals("tpa")) {
            return "TPA";
        }
        if (pack.equals("homes")) {
            return "Homes";
        }
        if (pack.equals("spawn")) {
            return "Spawn";
        }
        if (pack.equals("durability-ping")) {
            return "Durability Ping";
        }
        return pack;
    }
}
