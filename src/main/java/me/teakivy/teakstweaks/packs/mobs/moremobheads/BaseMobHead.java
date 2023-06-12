package me.teakivy.teakstweaks.packs.mobs.moremobheads;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.teakivy.teakstweaks.Main;
import me.teakivy.teakstweaks.utils.ReflectionUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class BaseMobHead implements Listener {
    protected EntityType entity;
    protected String key;
    protected Sound sound;

    protected HashMap<String, String> textures;

    protected static HashMap<String, String> names = new HashMap<>();


    public BaseMobHead(EntityType entity, String key, Sound sound, String texture) {
        this.entity = entity;
        this.key = key;
        this.sound = sound;

        this.textures = new HashMap<>();
        if (texture != null) addHeadTexture("default", getName() + " Head", texture);


        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public BaseMobHead(EntityType entity, String key, Sound sound) {
        this(entity, key, sound, null);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() != entity) return;
        if (!dropHead(event)) return;


        event.getDrops().add(getHead(event));
    }

    public boolean dropHead(EntityDeathEvent event) {
        return MobHeads.dropChance(event.getEntity().getKiller(), Head.getChance(key));
    }

    public ItemStack getHead(EntityDeathEvent event) {
        return createHead(getName(event) + " Head", getTexture(event));
    }

    public ItemStack getHead() {
        return createHead(getName() + " Head", getTexture());
    }

    public String getTexture(EntityDeathEvent event) {
        return textures.get("default");
    }

    public String getTexture() {
        return textures.get("default");
    }

    public String getName(EntityDeathEvent event) {
        return WordUtils.capitalizeFully(entity.toString().toLowerCase().replace("_", " "));
    }

    public String getName() {
        return WordUtils.capitalizeFully(entity.toString().toLowerCase().replace("_", " "));
    }

    @EventHandler
    public void onNoteblockInteract(PlayerInteractEvent event) {
        if (this.sound == null) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (!Objects.requireNonNull(event.getClickedBlock()).getType().equals(Material.NOTE_BLOCK)) return;
        if (event.getPlayer().isSneaking() && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;

        playNoteblock(event.getClickedBlock());
    }

    @EventHandler
    public void onNoteblockPlay(BlockRedstoneEvent event) {
        if (this.sound == null) return;
        if (!event.getBlock().getType().equals(Material.NOTE_BLOCK)) return;
        if (event.getNewCurrent() >= 1 && event.getOldCurrent() == 0) {
            playNoteblock(event.getBlock());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.PLAYER_HEAD) return;
        try {
            if (!compareHeadTextures(this.textures, event.getBlock())) return;

            event.setDropItems(false);
            List<String> headDetails = findTextureName(event.getBlock());
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), createHead(headDetails.get(0), headDetails.get(1)));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    public void playNoteblock(Block block) {
        if (block.getType() != Material.NOTE_BLOCK) return;
        Block above = block.getLocation().add(0, 1, 0).getBlock();
        if (above.getType() != Material.PLAYER_HEAD) return;

        try {
            if (!compareHeadTextures(this.textures, above)) return;
            block.getLocation().getWorld().playSound(block.getLocation(), sound, 1, 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean compareHeadTextures(HashMap<String, String> texture, Block block) throws IllegalAccessException {
        for (String s : texture.values()) {
            if (compareHeadTexture(s, block)) return true;
        }

        return false;
    }

    public void addHeadTexture(String key, String name, String texture) {
        textures.put(key, texture);
        names.put(texture, name);
    }

    public boolean compareHeadTexture(String texture, Block block) throws IllegalAccessException {
        if (block.getType() != Material.PLAYER_HEAD) return false;
        Skull skull = (Skull) block.getState();

        GameProfile profile = (GameProfile) ReflectionUtils.getField(skull.getClass(), "profile").get(skull);
        if (profile == null) return false;

        PropertyMap properties = profile.getProperties();
        if (properties == null) return false;

        Collection<Property> textures = properties.get("textures");
        for (Property property : textures) {
            if (property.getValue().equals(texture)) {
                return true;
            }
        }

        return false;
    }

    public List<String> findTextureName(Block block) throws IllegalAccessException {
        if (block.getType() != Material.PLAYER_HEAD) return null;
        Skull skull = (Skull) block.getState();

        GameProfile profile = (GameProfile) ReflectionUtils.getField(skull.getClass(), "profile").get(skull);
        if (profile == null) return null;

        PropertyMap properties = profile.getProperties();
        if (properties == null) return null;

        Collection<Property> textures = properties.get("textures");
        for (Property property : textures) {
            if (names.containsKey(property.getValue())) {
                return List.of(names.get(property.getValue()), property.getValue());
            }
        }

        return null;
    }

    public ItemStack createHead(String name, String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.YELLOW + name);
        GameProfile profile = new GameProfile(UUID.fromString("fdb5599c-1b14-440e-82df-d69719703d21"), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }


}