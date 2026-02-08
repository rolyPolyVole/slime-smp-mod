package dev.rolypolyvole.slimesmp.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3

fun Vec3.highestBlockY(world: ServerLevel): Vec3 {
    val x = x.toInt()
    val z = z.toInt()
    val y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, BlockPos(Vec3i(x, 0, z)))
    return Vec3(this.x, y.toDouble(), this.z)
}