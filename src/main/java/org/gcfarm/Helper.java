package org.gcfarm;

import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

public class Helper {
   public static String getPlayerRegion(Location p) {
      Gcfarm plugin = (Gcfarm)Gcfarm.getProvidingPlugin(Gcfarm.class);
      FileConfiguration data = plugin.data;
      Iterator var3 = data.getConfigurationSection("regions").getKeys(false).iterator();

      Vector vec2;
      String s;
      Vector vec1;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         s = (String)var3.next();
         double x1 = data.getDouble("regions." + s + ".pos1.x");
         double y1 = data.getDouble("regions." + s + ".pos1.y");
         double z1 = data.getDouble("regions." + s + ".pos1.z");
         double x2 = data.getDouble("regions." + s + ".pos2.x");
         double y2 = data.getDouble("regions." + s + ".pos2.y");
         double z2 = data.getDouble("regions." + s + ".pos2.z");
         double minX;
         double maxX;
         if (x1 < x2) {
            minX = x1;
            maxX = x2;
         } else {
            minX = x2;
            maxX = x1;
         }

         double minY;
         double maxY;
         if (y1 < y2) {
            minY = y1;
            maxY = y2;
         } else {
            minY = y2;
            maxY = y1;
         }

         double minZ;
         double maxZ;
         if (z1 < z2) {
            minZ = z1;
            maxZ = z2;
         } else {
            minZ = z2;
            maxZ = z1;
         }

         Location loc1 = new Location(p.getWorld(), minX, minY, minZ);
         Location loc2 = new Location(p.getWorld(), maxX, maxY, maxZ);
         vec1 = loc1.toVector();
         vec2 = loc2.toVector();
      } while(!p.toVector().isInAABB(vec1, vec2));

      return s;
   }
}
