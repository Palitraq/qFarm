package org.gcfarm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gcfarm extends JavaPlugin implements Listener {
   HashMap<String, Double> works = new HashMap();
   Map<String, List> select = new HashMap();
   FileConfiguration data;
   private FileConfiguration blocks;
   public List<?> regions;
   private Logger log;

   public FileConfiguration getData() {
      return this.data;
   }

   private void loadFiles() {
      this.saveDefaultConfig();
      this.getServer().getPluginManager().registerEvents(this, this);
      this.log = this.getLogger();
      this.saveResource("regions.yml", false);
      String var10002 = String.valueOf(this.getDataFolder());
      File datafile = new File(var10002 + File.separator + "regions.yml");
      if (!datafile.exists()) {
         try {
            datafile.createNewFile();
         } catch (IOException var5) {
            System.out.println(var5.getMessage());
         }
      }

      this.data = YamlConfiguration.loadConfiguration(datafile);
      this.saveResource("blocks.yml", false);
      var10002 = String.valueOf(this.getDataFolder());
      File block = new File(var10002 + File.separator + "blocks.yml");
      if (!block.exists()) {
         try {
            block.createNewFile();
         } catch (IOException var4) {
            System.out.println(var4.getMessage());
         }
      }

      this.blocks = YamlConfiguration.loadConfiguration(block);
      this.regions = this.getConfig().getList("regions");
      this.reloadConfig();
   }

   public void onEnable() {
      this.loadFiles();
      Bukkit.getLogger().log(Level.INFO, "[GCFarm] Enabled gcfarm");
   }

   private void saveData() {
      try {
         FileConfiguration var10000 = this.data;
         String var10003 = String.valueOf(this.getDataFolder());
         var10000.save(new File(var10003 + File.separator + "regions.yml"));
      } catch (IOException var2) {
         System.out.println(var2.getMessage());
      }

   }

   public void onDisable() {
   }

   private void sendFormat(CommandSender p, String msg) {
      p.sendMessage(ChatColor.translateAlternateColorCodes('&', Hex.applyColor(msg)));
   }

   private void startJob(Player p) {
      if (p.hasPermission("gcfarm.use")) {
         if (this.works.containsKey(p.getName())) {
            this.sendFormat(p, this.getConfig().getString("messages.already"));
         } else {
            this.works.put(p.getName(), 0.0D);
            this.sendFormat(p, this.getConfig().getString("messages.start"));
            if (this.getConfig().getBoolean("use_titles")) {
               String title = this.getConfig().getString("title_start");
               String subtitle = this.getConfig().getString("subtitle_start");
               p.sendTitle(title.replaceAll("(&([a-f0-9]))", "§$2"), subtitle.replaceAll("(&([a-f0-9]))", "§$2"));
            }
         }
      }

   }

   private void stopJob(Player p) {
      if (!this.works.containsKey(p.getName())) {
         this.sendFormat(p, this.getConfig().getString("messages.notalready"));
      } else {
         Iterator var2 = this.getConfig().getStringList("sallary_commands").iterator();

         String subtitle;
         while(var2.hasNext()) {
            subtitle = (String)var2.next();
            Server var10000 = Bukkit.getServer();
            ConsoleCommandSender var10001 = Bukkit.getConsoleSender();
            String var10002 = subtitle.replace("{player}", p.getName());
            String amount = String.valueOf(this.works.get(p.getName()));
            var10000.dispatchCommand(var10001, var10002.replace("{amount}", amount));
         }

         this.sendFormat(p, this.getConfig().getString("messages.stop"));
         if (this.getConfig().getBoolean("use_titles")) {
            this.getLogger().log(Level.INFO, "[GCFarm] title stop");
            String title = String.format(this.getConfig().getString("title_stop"), this.works.get(p.getName()));
            subtitle = String.format(this.getConfig().getString("subtitle_stop"), this.works.get(p.getName()));
            p.sendTitle(title.replaceAll("(&([a-f0-9]))", "§$2"), subtitle.replaceAll("(&([a-f0-9]))", "§$2"));
         }

         this.works.remove(p.getName());
      }

   }

   public boolean onCommand(CommandSender p, Command cmd, String str, String[] args) {
      if (!cmd.getName().equalsIgnoreCase("farm")) {
         return false;
      } else {
         if (args.length == 0) {
            this.sendFormat(p, this.getConfig().getString("messages.usage"));
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            this.sendFormat(p, this.getConfig().getString("messages.help"));
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            if (!p.hasPermission("gcfarm.use")) {
               this.sendFormat(p, this.getConfig().getString("messages.no_perm"));
               return false;
            }

            if (this.works.containsKey(p.getName())) {
               this.stopJob((Player)p);
            } else {
               this.startJob((Player)p);
            }
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            if (!p.hasPermission("gcfarm.use")) {
               this.sendFormat(p, this.getConfig().getString("messages.no_perm"));
               return false;
            }

            this.stopJob((Player)p);
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("reload") && p.hasPermission("gcfarm.reload")) {
            this.loadFiles();
            this.sendFormat(p, this.getConfig().getString("messages.reload"));
         }

         String var10001;
         Location loc;
         if (args.length == 1 && args[0].equalsIgnoreCase("pos1")) {
            if (!p.hasPermission("gcfarm.select")) {
               this.sendFormat(p, this.getConfig().getString("messages.no_perm"));
               return false;
            }

            loc = p.getServer().getPlayer(p.getName()).getLocation();
            List cord = new ArrayList();
            if (cord.size() < 5) {
               for(int i = 0; i < 6; ++i) {
                  cord.add((Object)null);
               }
            }

            cord.set(0, loc.getX());
            cord.set(1, loc.getY());
            cord.set(2, loc.getZ());
            this.select.put(p.getName(), cord);
            var10001 = String.valueOf(cord.get(0));
            p.sendMessage("Marked pos1 at: X:" + var10001 + " Y:" + String.valueOf(cord.get(1)) + " Z:" + String.valueOf(cord.get(2)));
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("pos2")) {
            if (!p.hasPermission("gcfarm.select")) {
               this.sendFormat(p, this.getConfig().getString("messages.no_perm"));
               return false;
            }

            loc = p.getServer().getPlayer(p.getName()).getLocation();
            List cord = (List)this.select.get(p.getName());
            cord.set(3, loc.getX());
            cord.set(4, loc.getY());
            cord.set(5, loc.getZ());
            this.select.put(p.getName(), cord);
            var10001 = String.valueOf(cord.get(3));
            p.sendMessage("Marked pos2 at: X:" + var10001 + " Y:" + String.valueOf(cord.get(4)) + " Z:" + String.valueOf(cord.get(5)));
         }

         if (args.length >= 1 && args[0].equalsIgnoreCase("create")) {
            if (args.length > 1) {
               if (p.hasPermission("gcfarm.create")) {
                  if (this.data.getString("regions." + args[1]) != null) {
                     this.sendFormat(p, this.getConfig().getString("messages.exists"));
                     return false;
                  }

                  this.data.set("regions." + args[1] + ".pos1.x", Math.floor((Double)((List)this.select.get(p.getName())).get(0)));
                  this.data.set("regions." + args[1] + ".pos1.y", Math.floor((Double)((List)this.select.get(p.getName())).get(1)));
                  this.data.set("regions." + args[1] + ".pos1.z", Math.floor((Double)((List)this.select.get(p.getName())).get(2)));
                  this.data.set("regions." + args[1] + ".pos2.x", Math.floor((Double)((List)this.select.get(p.getName())).get(3)));
                  this.data.set("regions." + args[1] + ".pos2.y", Math.floor((Double)((List)this.select.get(p.getName())).get(4)));
                  this.data.set("regions." + args[1] + ".pos2.z", Math.floor((Double)((List)this.select.get(p.getName())).get(5)));
                  World w = Bukkit.getServer().getPlayer(p.getName()).getWorld();
                  this.data.set("regions." + args[1] + ".world", w.getName());
                  this.saveData();
                  this.sendFormat(p, this.getConfig().getString("messages.create_success"));
               }
            } else {
               this.sendFormat(p, this.getConfig().getString("messages.create_usage"));
            }
         }

         return true;
      }
   }

   @EventHandler
   public void blockBreak(BlockBreakEvent e) {
      String regionName = Helper.getPlayerRegion(e.getBlock().getLocation());

      if (regionName != null) {
         String regionWorld = this.data.getString("regions." + regionName + ".world");
         if (regionWorld == null || !regionWorld.equals(e.getBlock().getLocation().getWorld().getName())) {
            return;
         }
      }

      if (!this.works.containsKey(e.getPlayer().getName()) && regionName != null && !e.getPlayer().hasPermission("gcfarm.bypass.regions")) {
         this.sendFormat(e.getPlayer(), this.getConfig().getString("messages.cant_break"));
         e.setCancelled(true);
         return;
      }

      if (regionName == null) {
         if (!this.works.containsKey(e.getPlayer().getName())) {
            return;
         }
         this.sendFormat(e.getPlayer(), this.getConfig().getString("messages.region_error"));
         e.setCancelled(true);
         return;
      }

      if (!this.works.containsKey(e.getPlayer().getName())) {
         return;
      }

      final Block block = e.getBlock();
      final Material mat = block.getType();

      if (this.blocks.getConfigurationSection("blocks") == null) {
         return;
      }

      Iterator<String> var5 = this.blocks.getConfigurationSection("blocks").getKeys(false).iterator();
      boolean found = false;

      while(var5.hasNext()) {
         String m = var5.next();
         Material blockMaterial = Material.getMaterial(m);
         if (blockMaterial == null) {
            blockMaterial = Material.getMaterial("LEGACY_" + m);
         }

         if (blockMaterial != null && block.getType().equals(blockMaterial)) {
            found = true;
            final BlockData data = block.getBlockData();
            block.setType(Material.AIR);
            double num = (double)Math.round(((Double)this.works.get(e.getPlayer().getName()) + this.blocks.getDouble("blocks." + m + ".cost")) * 100.0D) / 100.0D;
            this.works.put(e.getPlayer().getName(), num);
            this.sendFormat(e.getPlayer(), String.format(this.getConfig().getString("messages.earn"), this.works.get(e.getPlayer().getName())));
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 3.0F);
            e.setCancelled(true);

            int respawnTime = this.blocks.getInt("blocks." + m + ".respawn");
            Bukkit.getScheduler().runTaskLater(this, () -> {
               block.setType(mat);
               block.setBlockData(data);
            }, (long)respawnTime * 20);
            return;
         }
      }

      if (!found) {
         e.setCancelled(true);
      }
   }

   @EventHandler
   public void onChange(EntityChangeBlockEvent e) {
      String regionName = Helper.getPlayerRegion(e.getBlock().getLocation());
      if (regionName == null) {
         return;
      }
      
      String regionWorld = this.data.getString("regions." + regionName + ".world");
      if (regionWorld == null) {
         return;
      }
      
      if (!regionWorld.equals(e.getBlock().getLocation().getWorld().getName())) {
         return;
      }
      
      if (e.getEntityType().equals(EntityType.PLAYER)) {
         Player p = (Player)e.getEntity();
         if (!this.works.containsKey(p.getName()) && !p.hasPermission("gcfarm.bypass.regions")) {
            this.sendFormat(p, this.getConfig().getString("messages.cant_break"));
            e.setCancelled(true);
         }
      }
   }
}
