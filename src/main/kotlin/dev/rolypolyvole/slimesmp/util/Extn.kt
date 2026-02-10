package dev.rolypolyvole.slimesmp.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3

fun BlockPos.highestBlockY(world: ServerLevel): BlockPos {
    val x = x
    val z = z
    val y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, BlockPos(Vec3i(x, 0, z)))
    return BlockPos(this.x, y, this.z)
}

fun BlockPos.toVec3(): Vec3 {
    return Vec3(x.toDouble(), y.toDouble(), z.toDouble())
}