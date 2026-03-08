package org.gcfarm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class Hex {
   private static final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

   public static String applyColor(String message) {
      for(Matcher matcher = hexPattern.matcher(message); matcher.find(); matcher = hexPattern.matcher(message)) {
         ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
         String before = message.substring(0, matcher.start());
         String after = message.substring(matcher.end());
         message = before + String.valueOf(hexColor) + after;
      }

      return ChatColor.translateAlternateColorCodes('&', message);
   }
}
