/*
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package mario182.dynwarp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import mario182.dynwarp.converter.FormatConverter;
import mario182.dynwarp.converter.V1ToV2Converter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class Main extends JavaPlugin implements CommandExecutor{

    public final static String VER = "0.31";
    public final static char SEPERATOR = 'と';
    public final static char GROUPSEPERATOR = '共';
    public final static String FILEHEAD = "#DynWarp by mario182 - File format v2";
    public final static String FORMAT = "#warpname"+SEPERATOR+"dynmapname"+SEPERATOR+"worldname"+SEPERATOR+"groups"+SEPERATOR+"x-coord"+SEPERATOR+"y-coord"+SEPERATOR+"z-coord"+SEPERATOR+"yaw"+SEPERATOR+"pitch"+SEPERATOR+"permission";
    public final static ArrayList<Warp> warps = new ArrayList<>(100);
    public final static FormatConverter[] converters;
    public static Server server;
    public static DynmapAPI d;
    public static File warpfile;
    private boolean load = true;

    static {
        ArrayList<FormatConverter> c = new ArrayList<>(15);
        c.add(new V1ToV2Converter());
        converters = c.toArray(new FormatConverter[c.size()]);
    }

    @Override
    public void onEnable() {
        server = getServer();
        Plugin p = getServer().getPluginManager().getPlugin("dynmap");
        if (p!=null && p.isEnabled()){
            d = (DynmapAPI)p;
            getLogger().info("Found dynmap: "+d.getDynmapVersion());
        }else{
            throw new RuntimeException("dynmap not enabled.");
        }
        if (!getDataFolder().exists()){
            getDataFolder().mkdir();
        }
        warpfile = new File(getDataFolder(), "warps.txt");
        if (!warpfile.exists()){
            try{
                warpfile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(warpfile));
                bw.write(FILEHEAD);
                bw.newLine();
                bw.write(FORMAT);
                bw.newLine();
                bw.flush();
                bw.close();
                getLogger().info("Created new empty warp file.");
            }catch(IOException e){
                getLogger().log(Level.SEVERE, "Error creating warp file.", e);
            }
        }
        load();
        for (Warp w : warps){
            addLocation(w);
        }

        initCommand("dynwarp", this);
        initCommand("warp", this);
        initCommand("warpgroup", this);
        initCommand("createwarp", this);
        initCommand("deletewarp", this);
        initCommand("warplist", this);
        initCommand("grouplist", this);
        load = false;
        getLogger().info("DynWarp "+VER+" enabled.");
    }

    @Override
    public boolean onCommand(CommandSender cs, Command c, String label, String[] args) {
        if (!(cs instanceof Player)){
            cs.sendMessage("This won't work from console.");
            return true;
        }
        Player p = (Player)cs;
        if (label.equals("dynwarp")){
            cs.sendMessage("DynWarp "+VER+" by mario182.");
            cs.sendMessage("/warp <target> - Warps to target.");
            cs.sendMessage("/warplist [groupname] - Lists all warps/warps of a group.");
            cs.sendMessage("/grouplist - Lists all warp groups.");
            cs.sendMessage("/createwarp <target> [displayname] [permission] - Creates warp \"target\". If a permission is given, it will be required to use this warp.");
            cs.sendMessage("/deletewarp <target> - Removes warp \"target\".");
            cs.sendMessage("/warpgroup <target> <add/del> <groupname> - Adds/Removes \"target\" from \"groupname\".");
        }else if (label.equals("createwarp") || label.equals("addwarp")){
            if (cs.hasPermission("dynwarp.create")){
                if (args.length==0 || args.length > 3){
                    cs.sendMessage("Usage: /"+label+" <warpname> [displayname] [permission]");
                }else{
                    if (!warpNameExists(args[0])){
                        Location l = p.getLocation();
                        Warp newwarp = new Warp(args[0], args.length>=2?args[1]:args[0], p.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getYaw(), l.getPitch(), args.length>=3?args[2]:null);
                        if (!warps.contains(newwarp)){
                            warps.add(newwarp);
                            addLocation(newwarp);
                            d.sendBroadcastToWeb("DynWarp", "Added warp \""+args[0]+"\".");
                            cs.sendMessage("Successfully added \""+args[0]+"\"");
                            if (args.length>=2){
                                cs.sendMessage("Warp displays on dynmap as \""+args[1]+"\"");
                            }
                            if (args.length>=3){
                                cs.sendMessage("Warp requires permission \""+args[2]+"\"");
                            }
                        }else{
                            cs.sendMessage("Another warp at this location already exists.");
                        }
                    }else{
                        cs.sendMessage("Warpname already exists.");
                    }
                }
            }else{
                cs.sendMessage("No permission. Required permission \"dynwarp.create\".");
            }
        }else if(label.equals("deletewarp") || label.equals("removewarp")){
            if (cs.hasPermission("dynwarp.delete")){
                if (args.length==1){
                    if (warpNameExists(args[0])){
                        Iterator<Warp> i = warps.iterator();
                        while (i.hasNext()){
                            Warp w = i.next();
                            if (w.getName().equals(args[0])){
                                i.remove();
                                removeLocation(w);
                            }
                        }
                        d.sendBroadcastToWeb("DynWarp", "Removed warp \""+args[0]+"\".");
                        cs.sendMessage("Removed warp \""+args[0]+"\".");
                    }else{
                        cs.sendMessage("Warp does not exist.");
                    }
                }else{
                    cs.sendMessage("Usage: /"+label+" <warpname>");
                }
            }else{
                cs.sendMessage("No permission. Required permission \"dynwarp.delete\".");
            }
        }else if(label.equals("warplist") || label.equals("listwarps")){
            StringBuilder sb = new StringBuilder(warps.size()*24);
            Iterator<Warp> i = warps.iterator();
            int total=0, allowed=0;
            while(i.hasNext()){
                Warp w = i.next();
                if (args.length==1){
                    if (!w.getGroups().contains(args[0])){
                        continue;
                    }
                }
                if (w.getPermission()!=null && !w.getPermission().isEmpty()){
                    if (!cs.hasPermission(w.getPermission()) && !cs.hasPermission("dynwarp.ignorepermissions")){
                        sb.append(ChatColor.RED);
                    }else{
                        allowed++;
                        sb.append(ChatColor.GREEN);
                    }
                }else{
                    allowed++;
                }
                total++;
                sb.append(w.getName());
                if (i.hasNext()){
                    sb.append(ChatColor.WHITE).append(", ");
                }
            }
            cs.sendMessage(sb.toString());
            cs.sendMessage(total+" warp(s) total"+(args.length==1?" in the \""+args[0]+"\" group":"")+", of which you can access "+allowed+".");
        }else if(label.equals("warp")){
            if (cs.hasPermission("dynwarp.warp")){
                if (args.length==1){
                    if (warpNameExists(args[0])){
                        Warp w = null;
                        for (Warp w1 : warps){
                            if (w1.getName().equals(args[0])){
                                w = w1;
                                break;
                            }
                        }
                        if (getServer().getWorld(w.getWorld())!=null){
                            boolean allowed = true;
                            if (w.getPermission()!=null && !w.getPermission().isEmpty()){
                                if (!cs.hasPermission(w.getPermission()) && !cs.hasPermission("dynwarp.ignorepermissions")){
                                    allowed = false;
                                }
                            }
                            if (allowed){
                                p.teleport(w.toLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }else{
                                cs.sendMessage("No permission for that warp. Required: \""+w.getPermission()+"\"");
                            }
                        }else{
                            cs.sendMessage("Warp is in a world that does not exist / is not loaded.");
                        }
                    }else{
                        cs.sendMessage("Warp does not exist.");
                    }
                }else{
                    cs.sendMessage("Usage: /"+label+" <warpname>");
                }
            }else{
                cs.sendMessage("No permission. Required permission \"dynwarp.warp\".");
            }
        }else if(label.equals("grouplist") || label.equals("listgroups")){
            HashSet<String> hs = new HashSet<>(warps.size());
            for (Warp w : warps){
                hs.addAll(w.getGroups());
            }
            StringBuilder sb = new StringBuilder(hs.size()*24);
            Iterator<String> i = hs.iterator();
            while (i.hasNext()){
                sb.append(i.next());
                if (i.hasNext()){
                    sb.append(", ");
                }
            }
            cs.sendMessage("Available warp groups: ");
        }else if(label.equals("warpgroups") || label.equals("warpgroup") || label.equals("groupwarps")){
            if (args.length==3){
                if (warpNameExists(args[0])){
                    if (args[1].equals("add")){
                        if (cs.hasPermission("dynwarp.groups.add")){
                            Warp w = getWarp(args[0]);
                            if (!w.getGroups().contains(args[2])){
                                w.getGroups().add(args[2]);
                                cs.sendMessage("Group \""+args[2]+"\" added to warp \""+args[0]+"\".");
                                save();
                            }else{
                                cs.sendMessage("Warp is already in this group.");
                            }
                        }else{
                            cs.sendMessage("No permission. Required permission \"dynwarp.groups.add\".");
                        }
                    }else if(args[1].equals("del")){
                        if (cs.hasPermission("dynwarp.groups.del")){
                            Warp w = getWarp(args[0]);
                            if (w.getGroups().contains(args[2])){
                                w.getGroups().remove(args[2]);
                                cs.sendMessage("Group \""+args[2]+"\" removed from warp \""+args[0]+"\".");
                                save();
                            }else{
                                cs.sendMessage("Warp is not in this group.");
                            }
                        }else{
                            cs.sendMessage("No permission. Required permission \"dynwarp.groups.del\".");
                        }
                    }else{
                        cs.sendMessage("Usage: /"+label+" <warpname> <add/del> <groupname>");
                    }
                }else{
                    cs.sendMessage("Warp name does not exist.");
                }
            }else{
                cs.sendMessage("Usage: /"+label+" <warpname> <add/del> <groupname>");
            }
        }
        return true;
    }

    public static boolean warpNameExists(String s){
        for (Warp w : warps){
            if (w.getName().equals(s)){
                return true;
            }
        }
        return false;
    }

    private void addLocation(Warp w) {
        MarkerAPI l = d.getMarkerAPI();
        MarkerSet ms;
        if (l.getMarkerSet("dynwarp.markerset")==null){
            ms = l.createMarkerSet("dynwarp.markerset", "Warps", null, false);
            ms.setHideByDefault(false);
            ms.setLabelShow(true);
            ms.setMinZoom(0);
            ms.setLayerPriority(0);
        }else{
            ms = l.getMarkerSet("dynwarp.markerset");
        }
        Marker m = ms.createMarker("dynwarp_"+w.getName(), "Warp: "+w.getDynmapname(), w.getWorld(), w.getX(), w.getY(), w.getZ(), l.getMarkerIcon(MarkerIcon.DEFAULT), false);
        if (m == null){
            throw new RuntimeException("Failed to create marker");
        }
        save();
    }

    private void removeLocation(Warp w) {
        MarkerAPI l = d.getMarkerAPI();
        MarkerSet ms = l.getMarkerSet("dynwarp.markerset");
        if (ms!=null){
            for(Marker m : ms.getMarkers()){
                if (m.getMarkerID().equals("dynwarp_"+w.getName())){
                    m.deleteMarker();
                }
            }
        }
        save();
    }

    private void initCommand(String cmd, CommandExecutor ce) {
        PluginCommand c = getCommand(cmd);
        if (c!=null){
            if (c.getPlugin()==this){
                c.setExecutor(ce);
                getLogger().fine("Registered command: \"/"+cmd+"\"");
            }else{
                getLogger().severe("Failed to register command \"/"+cmd+"\": Command already blocked by "+c.getPlugin().getName());
            }
        }else{
            getLogger().severe("Failed to register command \"/"+cmd+"\": getCommand returned null.");
            if (cmd.equals("warp")){
                getLogger().warning("If you are using CommandBook, please set \"low-priority-command-registration\" in CommandBook's config.yml file to true, or disable the \"warps\" component.");
            }
        }
    }

    private void save() {
        if (load){ return; }
        getLogger().info("Saving warps...");
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(warpfile));
            bw.write(FILEHEAD);
            bw.newLine();
            bw.write(FORMAT);
            bw.newLine();
            Iterator<Warp> i = warps.iterator();
            while (i.hasNext()){
                Warp w = i.next();
                StringBuilder groups = new StringBuilder(w.getGroups().size()*24);
                Iterator<String> i1 = w.getGroups().iterator();
                while (i1.hasNext()){
                    String s = i1.next();
                    groups.append(s);
                    if (i1.hasNext()){
                        groups.append(GROUPSEPERATOR);
                    }
                }
                bw.write(w.getName()+SEPERATOR+w.getDynmapname()+SEPERATOR+w.getWorld()+SEPERATOR+groups.toString()+SEPERATOR+w.getX()+SEPERATOR+w.getY()+SEPERATOR+w.getZ()+SEPERATOR+w.getYaw()+SEPERATOR+w.getPitch()+SEPERATOR+(w.getPermission()!=null?w.getPermission():""));
                if (i.hasNext()){
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();
            getLogger().info("Warps saved.");
        }catch(IOException e){
            getLogger().log(Level.SEVERE, "Failed to save warps.", e);
        }
    }

    private void load() {
        getLogger().info("Loading warps...");
        warps.clear();
        try{
            LineNumberReader lr = new LineNumberReader(new FileReader(warpfile));
            String s;
            while ((s = lr.readLine()) != null){
                if (s.startsWith("#")){
                    for (FormatConverter c : converters){
                        if (s.equals(c.getHeader())){
                            getLogger().info("Converting warp file to "+c.getVersion()+"...");
                            if (c.convert(getLogger(), warpfile)){
                                lr.close();
                                load();
                                return;
                            }else{
                                getLogger().severe("Failed to convert warp file!");
                            }
                        }
                    }
                    continue;
                }
                String[] a = s.split(String.valueOf(SEPERATOR));
                try{
                    String perm = null;
                    if (a.length>=10){
                        perm = a[9];
                    }
                    Warp w = new Warp(a[0], a[1], a[2], Integer.parseInt(a[4]), Integer.parseInt(a[5]), Integer.parseInt(a[6]), Float.parseFloat(a[7]), Float.parseFloat(a[8]), perm);
                    String[] groups = a[3].split(String.valueOf(GROUPSEPERATOR));
                    Collections.addAll(w.getGroups(), groups);
                    if (warpNameExists(w.getName())){
                        throw new Exception("Warp "+w.getName()+" already exists!");
                    }
                    warps.add(w);
                }catch(Exception e){
                    getLogger().severe("Failed to parse warp at line #"+lr.getLineNumber()+": "+e.toString());
                }
            }
            lr.close();
            getLogger().info("Warps loaded.");
        }catch(IOException e){
            getLogger().log(Level.SEVERE, "Failed to load warps.", e);
        }
    }

    public static Warp getWarp(String name) {
        for (Warp w : warps){
            if (w.getName().equals(name)){
                return w;
            }
        }
        return null;
    }
}
