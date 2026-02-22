package dev.rolypolyvole.slimesmp.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class CustomEndSpikes {
    private CustomEndSpikes() {}

    private static final int START_DEPTH = 5;              // requested
    private static final float MAX_LEAN_PER_Y = 0.22f;      // tune
    private static final float TIP_STOP_RADIUS = 0.62f;     // stop when it would become a needle
    private static final int CAGE_RADIUS = 4;

    public static final Set<Vec3> CRYSTAL_LOCATIONS = new HashSet<>();

    public static void place(ServerLevelAccessor level, RandomSource ignored, SpikeConfiguration cfg, SpikeFeature.EndSpike spike) {
        RandomSource sr = spikeRandom(level, spike);

        int cx = spike.getCenterX();
        int cz = spike.getCenterZ();

        int startY = findStartY(level, cx, cz, START_DEPTH);

        // Use the (possibly adjusted) spike height from the cache loader.
        int topY = Mth.clamp(spike.getHeight(), level.getMinY(), level.getMaxY() - 1);

        int baseRadius = spike.getRadius();

        float[] lean = outwardLean(cx, cz, MAX_LEAN_PER_Y, sr);
        float leanX = lean[0];
        float leanZ = lean[1];

        TopInfo top = placeSpire(level, sr, cx, cz, startY, topY, baseRadius, leanX, leanZ);

        // top.topY is the bedrock Y; crystal sits one above it
        BlockPos crystalPos = spawnCrystalAndTop(level, sr, cfg, top.topX, top.topY + 1, top.topZ);

        if (spike.isGuarded() && crystalPos != null) {
            placeHollowIronSphere(level, crystalPos, CAGE_RADIUS);
            clearInsideSphere(level, crystalPos, CAGE_RADIUS - 1);
        }
    }

    private static RandomSource spikeRandom(ServerLevelAccessor level, SpikeFeature.EndSpike spike) {
        long worldSeed = level.getLevel().getSeed();
        long x = spike.getCenterX();
        long z = spike.getCenterZ();
        long h = worldSeed
                ^ (x * 341873128712L)
                ^ (z * 132897987541L)
                ^ (spike.isGuarded() ? 0x9E3779B97F4A7C15L : 0L);
        return RandomSource.create(h);
    }

    private static float[] outwardLean(int cx, int cz, float maxLeanPerY, RandomSource sr) {
        float len = Mth.sqrt((float)(cx * cx + cz * cz));
        float nx = len == 0 ? 0 : cx / len;
        float nz = len == 0 ? 0 : cz / len;

        float leanX = nx * maxLeanPerY;
        float leanZ = nz * maxLeanPerY;

        float wobble = (sr.nextFloat() - 0.5f) * (maxLeanPerY * 0.25f);
        leanX += -nz * wobble;
        leanZ +=  nx * wobble;

        return new float[]{leanX, leanZ};
    }

    private static int findStartY(ServerLevelAccessor level, int x, int z, int depth) {
        int minY = level.getMinY();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        MutableBlockPos pos = new MutableBlockPos();
        while (y > minY) {
            pos.set(x, y, z);
            if (level.getBlockState(pos).is(Blocks.END_STONE)) {
                return Math.max(minY, y - depth);
            }
            y--;
        }
        return minY;
    }

    private static TopInfo placeSpire(
            ServerLevelAccessor level,
            RandomSource sr,
            int tipX,
            int tipZ,
            int startY,
            int requestedTopY,
            int baseRadius,
            float leanX,
            float leanZ
    ) {
        MutableBlockPos pos = new MutableBlockPos();

        int height = Math.max(1, requestedTopY - startY);

        // taper curve parameters
        final float p = 2.4f;

        int finalTopY = requestedTopY;
        int finalTopX = tipX;
        int finalTopZ = tipZ;

        for (int y = startY; y <= requestedTopY; y++) {
            float t = (y - startY) / (float) height;

            // lean so that the tip lands at (tipX,tipZ)
            float cxf = tipX - (requestedTopY - y) * leanX;
            float czf = tipZ - (requestedTopY - y) * leanZ;

            int spineX = Mth.floor(cxf);
            int spineZ = Mth.floor(czf);

            // radius function; approaches 0 near the top
            float r = baseRadius * (float) Math.pow(1.0f - t, p);

            // when we'd become a needle, stop early and place bedrock as the cap
            if (r <= TIP_STOP_RADIUS) {
                finalTopY = y;
                finalTopX = spineX;
                finalTopZ = spineZ;
                break;
            }

            // place core/spine
            pos.set(spineX, y, spineZ);
            level.setBlock(pos, pickSpikeBlock(sr, y, requestedTopY), 2);

            int minX = (int) Math.floor(cxf - r - 1);
            int maxX = (int) Math.ceil (cxf + r + 1);
            int minZ = (int) Math.floor(czf - r - 1);
            int maxZ = (int) Math.ceil (czf + r + 1);

            float r2 = r * r;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    float dx = (x + 0.5f) - cxf;
                    float dz = (z + 0.5f) - czf;
                    if (dx * dx + dz * dz <= r2) {
                        pos.set(x, y, z);
                        level.setBlock(pos, pickSpikeBlock(sr, y, requestedTopY), 2);
                    }
                }
            }
        }

        // Reserve the very top as a single bedrock cap (no mixed blocks)
        pos.set(finalTopX, finalTopY, finalTopZ);
        level.setBlock(pos, pickSpikeBlock(sr, 1, 1), 2);
        level.setBlock(pos.below(1), pickSpikeBlock(sr, 1, 1), 2);
        level.setBlock(pos.below(2), pickSpikeBlock(sr, 1, 1), 2);
        level.setBlock(pos.below(3), pickSpikeBlock(sr, 1, 1), 2);
        level.setBlock(pos.above(), Blocks.BEDROCK.defaultBlockState(), 2);

        return new TopInfo(finalTopX, finalTopY + 1, finalTopZ);
    }

    private static BlockState pickSpikeBlock(RandomSource sr, int y, int topY) {
        if (sr.nextFloat() < 0.2f * y / topY) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        return Blocks.OBSIDIAN.defaultBlockState();
    }

    private static BlockPos spawnCrystalAndTop(ServerLevelAccessor level, RandomSource sr, SpikeConfiguration cfg, int x, int y, int z) {
        EndCrystal crystal = EntityType.END_CRYSTAL.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (crystal == null) return null;

        crystal.setBeamTarget(null);
        crystal.setInvulnerable(false);
        crystal.snapTo(x + 0.5, y, z + 0.5, sr.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(crystal);

        CRYSTAL_LOCATIONS.add(crystal.position());

        BlockPos p = crystal.blockPosition();
        // bedrock is already the top block; we place fire at crystal position only
        level.setBlock(p, Blocks.SOUL_FIRE.defaultBlockState(), 2);
        return p;
    }

    private static void placeHollowIronSphere(ServerLevelAccessor level, BlockPos center, int radius) {
        MutableBlockPos pos = new MutableBlockPos();
        int r2 = radius * radius;
        int inner2 = (radius - 1) * (radius - 1);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 <= r2 && d2 >= inner2) {
                        pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void clearInsideSphere(ServerLevelAccessor level, BlockPos center, int radius) {
        MutableBlockPos pos = new MutableBlockPos();
        int r2 = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 <= r2) {
                        pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        if (!pos.equals(center)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    private record TopInfo(int topX, int topY, int topZ) {}
}
