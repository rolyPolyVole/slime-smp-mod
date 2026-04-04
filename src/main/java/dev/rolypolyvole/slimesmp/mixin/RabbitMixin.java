package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Rabbit.class)
public abstract class RabbitMixin extends Animal {

    @Shadow
    protected abstract void setVariant(Rabbit.Variant variant);

    protected RabbitMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private Rabbit self() {
        return (Rabbit) ((Object) this);
    }

    @Unique
    private AttributeInstance attribute(Holder<Attribute> attribute) {
        return Objects.requireNonNull(self().getAttribute(attribute));
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void addCustomAttributes(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Rabbit self = self();
        double random = Math.random();

        double health = (1.0 + random) * self.getMaxHealth();
        double stepHeight = (1.0 + random) * self.maxUpStep();
        double scale = random + 1.0;
        double speed = 0.3 + random * 0.05;

        attribute(Attributes.MAX_HEALTH).setBaseValue(health);
        attribute(Attributes.STEP_HEIGHT).setBaseValue(stepHeight);
        attribute(Attributes.SCALE).setBaseValue(scale);
        attribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);

        self.setHealth(self.getMaxHealth());

        if (Math.random() < 0.01) {
            this.setVariant(Rabbit.Variant.EVIL);
        }
    }

}
