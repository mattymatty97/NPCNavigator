package com.mattymatty.NPCNavigator;

import org.bukkit.util.Vector;

public class Utils {
    private static final double SQRT3 = Math.sqrt(3.0);
    static final double SQRT2 = Math.sqrt(2.0);

    static public double approx3dDistance(Vector a, Vector b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        double dz = Math.abs(a.getZ() - b.getZ());

        return
                0.5 * (1 + 1 / (4 * SQRT3))
                        * Math.min((dx + dy + dz) / SQRT3, Math.max(dx, Math.max(dy, dz)))
                ;

    }

    static public double approx2dDistance(Vector a, Vector b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dz = Math.abs(a.getZ() - b.getZ());

        return
                0.5 * (
                        1 + 1 / ((4 - 2 * SQRT2))
                                * Math.min((dx + dz) / SQRT2, Math.max(dx, dz))
                )
                ;
    }

    static public double trueDist3D(Vector a, Vector b) {
        double x = a.getX() - b.getX();
        double y = a.getY()- b.getY();
        double z = a.getZ() - b.getZ();
        return Math.sqrt(x * x + y * y + z * z);
    }

    static public double trueDist2D(Vector a, Vector b) {
        double x = a.getX() - b.getX();
        double z = a.getZ() - b.getZ();
        return Math.sqrt(x * x + z * z);
    }

}
