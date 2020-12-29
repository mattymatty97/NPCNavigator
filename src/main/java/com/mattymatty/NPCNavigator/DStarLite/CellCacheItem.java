package com.mattymatty.NPCNavigator.DStarLite;
import com.mattymatty.NPCNavigator.DStarLite.State;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

import java.util.Arrays;

public class CellCacheItem {
    public BlockData data = new BlockData();

    public static class BlockData {
        public String[] blockId={"","","",""};
        public boolean Passable;
        public boolean canJump;
        public boolean canFall;
        public boolean isDoor;
        public boolean isStair;
        public boolean isSlab;
        public BlockFace stairFace;
        public Stairs.Shape stairShape;
    }

    public CellCacheItem update(State u, World world){

        Block[] b = {
                world.getBlockAt(u.x,u.y+2,u.z),
                world.getBlockAt(u.x,u.y+1,u.z),
                world.getBlockAt(u.x,u.y,u.z),
                world.getBlockAt(u.x,u.y-1,u.z),
        };

        Material[] m = new Material[4];

        for(int i=0; i<4;i++){
            m[i] = b[i].getType();
            data.blockId[i] = m[i].getKey().toString();
        }

        data.Passable = ( b[2].isPassable() ||  (m[2].data == Door.class) ) &&
                ( b[1].isPassable() ||  (m[1].data == Door.class) ) &&
                ( m[3].isSolid() 	&& !(m[3].data == Door.class) );

        data.canFall = ( isPassable() && b[0].isPassable() );

        data.isStair = m[3].data == Stairs.class;
        data.isSlab = ( m[3].data == Slab.class && ( ((Slab)b[3].getBlockData()).getType() == Slab.Type.BOTTOM ) );

        data.canJump = ( canFall() );   //TODO: check for max y


        data.stairFace = null;
        data.stairShape = null;

        if(isStair()){
            Stairs s = (Stairs) b[3].getBlockData();
            if(s.getHalf() == Bisected.Half.BOTTOM) {
                data.stairFace = s.getFacing();
                data.stairShape = s.getShape();
            }else
                data.isStair = false;
        }

        return this;
    }

    public boolean isPassable(){
        return data.Passable;
    }

    public boolean canJump(){
        return data.canJump;
    }

    public boolean canFall(){
        return data.canFall;
    }

    public boolean isDoor(){
        return data.isDoor;
    }

    public boolean isStair(){
        return data.isStair;
    }

    public boolean isSlab(){
        return data.isSlab;
    }

    public String stairDirection(){
        return data.stairFace.name() + ":" + data.stairShape.name();
    }

    public boolean canClimb(int dx, int dy, int dz){
        if(!isStair() || dy==0)
            return false;

        if(dx>0 && dz==0){ //+X East
            return ( data.stairFace == BlockFace.EAST) ||
            (data.stairFace == BlockFace.SOUTH && data.stairShape == Stairs.Shape.OUTER_LEFT) ||
            (data.stairFace == BlockFace.NORTH && data.stairShape == Stairs.Shape.OUTER_RIGHT);
        }else if(dx<0 && dz==0){ //-X West
            return ( data.stairFace == BlockFace.WEST) ||
            (data.stairFace == BlockFace.SOUTH && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
            (data.stairFace == BlockFace.NORTH && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }else if(dx==0 && dz>0){ //+Z South
            return ( data.stairFace == BlockFace.SOUTH) ||
            (data.stairFace == BlockFace.WEST && data.stairShape == Stairs.Shape.OUTER_LEFT) ||
            (data.stairFace == BlockFace.EAST && data.stairShape == Stairs.Shape.OUTER_RIGHT);
        }else if(dx==0 && dz<0){ //-Z South
            return ( data.stairFace == BlockFace.NORTH) ||
            (data.stairFace == BlockFace.WEST && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
            (data.stairFace == BlockFace.EAST && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }else if(dx>0 && dz>0){ //+X+Z South-East
            return  (data.stairFace == BlockFace.EAST && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
                    (data.stairFace == BlockFace.SOUTH && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }else if(dx<0 && dz>0){ //-X+Z South-West
            return  (data.stairFace == BlockFace.SOUTH && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
                    (data.stairFace == BlockFace.WEST && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }else if(dx>0 && dz<0){ //+X-Z North-East
            return  (data.stairFace == BlockFace.NORTH && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
                    (data.stairFace == BlockFace.EAST && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }else if(dx<0 && dz<0){ //-X-Z North-West
            return  (data.stairFace == BlockFace.WEST && data.stairShape == Stairs.Shape.OUTER_RIGHT) ||
                    (data.stairFace == BlockFace.NORTH && data.stairShape == Stairs.Shape.OUTER_LEFT);
        }
        return false;
    }

    public String[] blockId(){
        return Arrays.copyOf(data.blockId,4);
    }
}
