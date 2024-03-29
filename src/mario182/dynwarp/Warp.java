/* 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package mario182.dynwarp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
        
public class Warp {

    private String name;
    private String dynmapname;
    private String world;
    private ArrayList<String> groups;
    private int x;
    private int y;
    private int z;
    private float yaw;
    private float pitch;
    private String permission;

    public Warp(String name, String dynmapname, String world, int x, int y, int z, float yaw, float pitch, String permission) {
        this.name = name;
        this.dynmapname = dynmapname;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.permission = permission;
        this.groups = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDynmapname() {
        return dynmapname;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getPermission() {
        return permission;
    }

    public Location toLocation(){
        return new Location(Main.server.getWorld(world), x, y, z, yaw, pitch);
    }
    
    public List<String> getGroups(){
        return groups;
    }

    @Override
    public String toString() {
        return "Warp{" + "name=" + name + ", dynmapname=" + dynmapname + ", world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + ", permission=" + permission + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.world);
        hash = 29 * hash + this.x;
        hash = 29 * hash + this.y;
        hash = 29 * hash + this.z;
        hash = 29 * hash + Float.floatToIntBits(this.yaw);
        hash = 29 * hash + Float.floatToIntBits(this.pitch);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Warp other = (Warp) obj;
        if (!Objects.equals(this.world, other.world)) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }

}
