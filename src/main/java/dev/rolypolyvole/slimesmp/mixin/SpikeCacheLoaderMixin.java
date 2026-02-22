// src/main/java/dev/rolypolyvole/slimesmp/mixin/SpikeCacheLoaderMixin.java
package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.feature.SpikeFeature$SpikeCacheLoader")
public abstract class SpikeCacheLoaderMixin {

    private static final int RADIUS_ADD = 3;  // was 4

    @Inject(method = "load(Ljava/lang/Long;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void adjustSpikeConfigInputs(Long seedKey, CallbackInfoReturnable<List<SpikeFeature.EndSpike>> cir) {
        List<SpikeFeature.EndSpike> original = cir.getReturnValue();
        List<SpikeFeature.EndSpike> adjusted = new ArrayList<>(original.size());

        for (SpikeFeature.EndSpike s : original) {
            int newHeight = s.getHeight() + 45;
            int newRadius = s.getRadius() + RADIUS_ADD;
            double centerX = s.getCenterX();
            double centerZ = s.getCenterZ();

            var direction = new Vector2d(centerX, centerZ).normalize();
            int newX = (int) Math.round(centerX + new Vector2d(direction).mul(20.0).x);
            int newZ = (int) Math.round(centerZ + new Vector2d(direction).mul(20.0).y);

            adjusted.add(new SpikeFeature.EndSpike(
                    newX,
                    newZ,
                    newRadius,
                    newHeight,
                    s.isGuarded()
            ));
        }

        cir.setReturnValue(adjusted);
    }
}

