package me.teakivy.teakstweaks.packs.survival.graves;

import me.teakivy.teakstweaks.Main;
import me.teakivy.teakstweaks.packs.items.armoredelytra.ArmoredElytras;
import me.teakivy.teakstweaks.utils.Base64Serializer;
import me.teakivy.teakstweaks.utils.Logger;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class GraveCreator {

    public static Main main = Main.getPlugin(Main.class);

    public static ConfigurationSection config = main.getConfig().getConfigurationSection("packs.graves");

    public static Location findGraveLocation(Location loc) {
        Location ogLoc = loc.clone();

        if (loc.getY() > loc.getWorld().getMaxHeight()) loc.setY(loc.getWorld().getMaxHeight());
        if (loc.getY() < loc.getWorld().getMinHeight()) {
            if (!config.getBoolean("allow-void-graves")) return null;
            loc.setY(loc.getWorld().getMaxHeight());
        }

        if (getTopBlock(loc, loc.getWorld().getMaxHeight()) == null) {
            if (!config.getBoolean("allow-void-graves")) return null;
            loc.setY(loc.getWorld().getMinHeight());

            loc.getBlock().setType(Material.GRASS_BLOCK);

            loc.setY(loc.getY() + 1);
            return loc;
        }

        loc = getNextAir(loc);

        if (loc == null) {
            if (!config.getBoolean("allow-void-graves")) return null;
            loc = new Location(ogLoc.getWorld(), ogLoc.getX(), ogLoc.getWorld().getMinHeight(), ogLoc.getZ());
        }

        Location tb = getTopBlock(loc, loc.getBlockY());
        if (tb != null && tb.getBlockY() < loc.getBlockY()) {
            loc.setY(tb.getY() + 1);
        }

        return loc;
    }

    public static void createGrave(Location location, Player player, int xp) throws IOException {
        Location loc = location.getBlock().getLocation().add(.5, 0, .5);
        ArmorStand as = (ArmorStand) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc.add(0, -1.4, 0), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setInvisible(true);
        as.setHelmet(new ItemStack(Material.STONE_BRICK_WALL));
        as.setInvulnerable(true);
        as.setCustomName(player.getName());
        as.addScoreboardTag("grave");
        as.setCustomNameVisible(true);

        PersistentDataContainer data = as.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Main.getPlugin(Main.class), "grave_owner_uuid");
        data.set(key, PersistentDataType.STRING, player.getUniqueId().toString());

        if (!Boolean.TRUE.equals(location.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            NamespacedKey key2 = new NamespacedKey(Main.getPlugin(Main.class), "grave_owner_items");
            data.set(key2, PersistentDataType.STRING, serializeItems(player));

            NamespacedKey key3 = new NamespacedKey(Main.getPlugin(Main.class), "grave_owner_xp");
            data.set(key3, PersistentDataType.INTEGER, xp);
        }

        if (!config.getBoolean("console-info")) return;

        Logger.log(Logger.LogLevel.INFO, "Created grave for " + ChatColor.GOLD + player.getName() + ChatColor.WHITE + " at " + ChatColor.GOLD + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + ChatColor.WHITE + " in " + ChatColor.GOLD + loc.getWorld().getName());
        int items = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            items += item.getAmount();
        }
        Logger.log(Logger.LogLevel.INFO, "Contains " + ChatColor.GOLD + items + ChatColor.WHITE + " items and " + ChatColor.GOLD + xp + ChatColor.WHITE + " XP");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Logger.log(Logger.LogLevel.INFO, " - " + ChatColor.GOLD + item.getType().name() + ChatColor.WHITE + " x " + ChatColor.GOLD + item.getAmount());
        }
    }

    public static ArrayList<Material> getAirTypes() {
        ArrayList<Material> airTypes = new ArrayList<>();
        airTypes.add(Material.AIR);
        airTypes.add(Material.CAVE_AIR);
        airTypes.add(Material.VOID_AIR);
        airTypes.add(Material.GRASS);
        airTypes.add(Material.TALL_GRASS);
        airTypes.add(Material.SEAGRASS);
        airTypes.add(Material.POPPY);
        airTypes.add(Material.DANDELION);
        airTypes.add(Material.BLUE_ORCHID);
        airTypes.add(Material.ALLIUM);
        airTypes.add(Material.AZURE_BLUET);
        airTypes.add(Material.RED_TULIP);
        airTypes.add(Material.ORANGE_TULIP);
        airTypes.add(Material.WHITE_TULIP);
        airTypes.add(Material.PINK_TULIP);
        airTypes.add(Material.OXEYE_DAISY);
        airTypes.add(Material.CORNFLOWER);
        airTypes.add(Material.LILY_OF_THE_VALLEY);
        airTypes.add(Material.WITHER_ROSE);
        airTypes.add(Material.SUNFLOWER);
        airTypes.add(Material.LILAC);
        airTypes.add(Material.ROSE_BUSH);
        airTypes.add(Material.PEONY);
        airTypes.add(Material.VINE);
        airTypes.add(Material.WARPED_FUNGUS);
        airTypes.add(Material.CRIMSON_FUNGUS);
        airTypes.add(Material.RED_MUSHROOM);
        airTypes.add(Material.BROWN_MUSHROOM);
        airTypes.add(Material.WARPED_ROOTS);
        airTypes.add(Material.NETHER_SPROUTS);
        airTypes.add(Material.CRIMSON_ROOTS);
        airTypes.add(Material.SNOW);
        airTypes.add(Material.LADDER);
        airTypes.add(Material.BAMBOO_SAPLING);
        airTypes.add(Material.BAMBOO);
        airTypes.add(Material.GLOW_LICHEN);
        airTypes.add(Material.FERN);
        airTypes.add(Material.LARGE_FERN);
        return airTypes;
    }

    public static ArrayList<Material> getFluidTypes() {
        ArrayList<Material> fluidTypes = new ArrayList<>();
        fluidTypes.add(Material.WATER);
        fluidTypes.add(Material.LAVA);
        return fluidTypes;
    }

    public static Location getTopBlock(Location location, int max) {
        Location loc = location.clone();
        loc.setY(max);
        while (loc.getY() > loc.getWorld().getMinHeight()) {
            if (!getAirTypes().contains(loc.getBlock().getType())) {
                return loc;
            }
            loc.setY(loc.getY() - 1);
        }
        return null;
    }

    public static Location getNextAir(Location location) {
        Location loc = location.clone();
        while (loc.getY() <= loc.getWorld().getMaxHeight()) {
            if (getAirTypes().contains(loc.getBlock().getType())) {
                return loc;
            }
            loc.setY(loc.getY() + 1);
        }

        return null;
    }


    public static String serializeItems(Player player) throws IOException {
        ArrayList<ItemStack> items = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));

        ArrayList<ItemStack> items2 = new ArrayList<>();

        ArrayList<ItemStack> toRemove = new ArrayList<>();

        for (ItemStack item : items) {
            if (item == null) continue;
            if (!item.getType().equals(Material.ELYTRA)) continue;

            if (!item.hasItemMeta()) continue;
            if (!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(main, "armored_elytra"), PersistentDataType.STRING)) continue;

            items2.add(ArmoredElytras.getB64ChestplateFromArmoredElytra(item));
            items2.add(ArmoredElytras.getB64ElytraFromArmoredElytra(item));

            toRemove.add(item);
        }

        items.addAll(items2);

        items.removeAll(toRemove);

        if (items.isEmpty()) return "";
        StringBuilder serialized = new StringBuilder();
        for (ItemStack item : items) {
            if (item == null) continue;
            String newSerItem = Base64Serializer.itemStackArrayToBase64(new ItemStack[]{item});
            serialized.append(newSerItem);
            serialized.append(" :%-=-%: ");
        }
        if (serialized.length() > " :%-=-%: ".length()) {
            return removeLastChars(serialized.toString(), " :%-=-%: ".length());
        }
        return serialized.toString();
    }

    public static ArrayList<ItemStack> deserializeItems(String serialized) throws IOException {
        ArrayList<ItemStack> items = new ArrayList<>();
        if (serialized.length() < 1) return items;
        for (String s : serialized.split(" :%-=-%: ", -1)) {
            items.add(Base64Serializer.itemStackArrayFromBase64(s)[0]);
        }
        return items;
    }

    public static String removeLastChars(String str, int chars) {
        return str.substring(0, str.length() - chars);
    }

}
