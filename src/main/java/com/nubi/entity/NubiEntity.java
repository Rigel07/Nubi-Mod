package com.nubi.entity;

import com.nubi.init.ModBiomes;
import com.nubi.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Nubi: nube voladora, tierna y juguetona.
 * - Blanca de día (colores rosa/azul/amarillo solo en el bioma Mundo Nubi).
 * - Gris y hostil durante las tormentas (si no está domesticada).
 * - Da pequeñas descargas al jugador cercano.
 * - Carga a otros mobs.
 * - Suelta "gota de humedad mágica" tras mucho tiempo al sol.
 * - Se doma con un cubo de agua y se monta con el lazo.
 */
public class NubiEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(NubiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STORMY =
            SynchedEntityData.defineId(NubiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LAZO =
            SynchedEntityData.defineId(NubiEntity.class, EntityDataSerializers.BOOLEAN);

    /** Ticks al sol necesarios para soltar una gota (6000 ticks = 5 minutos). */
    public static final int SUN_TICKS_FOR_DROP = 6000;

    private int sunTicks;
    private int shockCooldown;
    private int carryTicks;

    public NubiEntity(EntityType<? extends NubiEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FLYING_SPEED, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkNubiSpawnRules(EntityType<NubiEntity> type, LevelAccessor level,
                                              MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.canSeeSky(pos);
    }

    // ------------------------------------------------------------------ datos

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_STORMY, false);
        this.entityData.define(DATA_LAZO, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant().getId());
        tag.putInt("SunTicks", sunTicks);
        tag.putBoolean("HasLazo", hasLazo());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(NubiVariant.byId(tag.getInt("Variant")));
        sunTicks = tag.getInt("SunTicks");
        setLazo(tag.getBoolean("HasLazo"));
    }

    public NubiVariant getVariant() { return NubiVariant.byId(this.entityData.get(DATA_VARIANT)); }
    public void setVariant(NubiVariant variant) { this.entityData.set(DATA_VARIANT, variant.getId()); }
    public boolean isStormy() { return this.entityData.get(DATA_STORMY); }
    public void setStormy(boolean stormy) { this.entityData.set(DATA_STORMY, stormy); }
    public boolean hasLazo() { return this.entityData.get(DATA_LAZO); }
    public void setLazo(boolean lazo) { this.entityData.set(DATA_LAZO, lazo); }

    // ------------------------------------------------------------------ spawn

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data,
                                        @Nullable CompoundTag tag) {
        Holder<Biome> biome = level.getBiome(this.blockPosition());
        // Colores especiales SOLO en el bioma Mundo Nubi. En el resto del mapa: blancas.
        if (biome.is(ModBiomes.MUNDO_NUBI)) {
            setVariant(NubiVariant.randomColored(this.random));
        } else {
            setVariant(NubiVariant.WHITE);
        }
        // Que aparezcan flotando un poco por encima del suelo
        if (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) {
            int up = 2 + this.random.nextInt(8);
            AABB moved = this.getBoundingBox().move(0.0D, up, 0.0D);
            if (level.noCollision(this, moved)) {
                this.setPos(this.getX(), this.getY() + up, this.getZ());
            }
        }
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    // ------------------------------------------------------------------ IA

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StayGoal(this));
        this.goalSelector.addGoal(2, new StormZapGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.5F, true));
        this.goalSelector.addGoal(4, new CarryMobGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, e -> this.isStormy()));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (isStormy() && this.random.nextInt(6) == 0) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
            return;
        }
        serverTick();
    }

    private void serverTick() {
        // Tormenta -> gris y hostil (solo si no está domesticada)
        boolean stormy = !isTame() && this.level().isThundering();
        if (stormy != isStormy()) {
            setStormy(stormy);
            if (!stormy) setTarget(null);
        }

        // Mucho tiempo al sol -> gota de humedad mágica
        BlockPos pos = this.blockPosition();
        if (this.level().isDay() && !this.level().isRaining() && this.level().canSeeSky(pos)) {
            if (++sunTicks >= SUN_TICKS_FOR_DROP) {
                sunTicks = 0;
                this.spawnAtLocation(ModItems.MAGIC_MOISTURE_DROP.get());
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.4F);
            }
        }

        // Pequeñas descargas "de juego" al jugador más cercano
        if (!isStormy() && !isTame()) {
            if (shockCooldown > 0) {
                shockCooldown--;
            } else if (this.tickCount % 20 == 0 && this.random.nextInt(6) == 0) {
                Player player = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 3.0D,
                        EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                if (player != null) {
                    zap(player, 1.0F, true);
                    shockCooldown = 100;
                }
            }
        }

        // Soltar al mob que lleva encima tras un rato
        if (carryTicks > 0 && this.isVehicle()) {
            if (--carryTicks == 0) this.ejectPassengers();
        }
    }

    /** Descarga eléctrica: partículas + sonido + daño. */
    public void zap(LivingEntity target, float damage, boolean playful) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY(0.5D), target.getZ(), 14, 0.3D, 0.4D, 0.3D, 0.1D);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, playful ? 0.15F : 0.3F, 2.0F);
        }
        if (playful) {
            target.hurt(this.damageSources().magic(), damage);
        } else {
            target.hurt(this.damageSources().mobAttack(this), damage);
        }
    }

    // ------------------------------------------------------------------ interacción

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Domesticar con agua
        if (!isTame()) {
            if (stack.is(Items.WATER_BUCKET) && !isStormy()) {
                if (!this.level().isClientSide) {
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(false);
                    this.level().broadcastEntityEvent(this, (byte) 7); // corazones
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }

        if (!isOwnedBy(player)) {
            return super.mobInteract(player, hand);
        }

        // Ponerle el lazo para poder montarla
        if (stack.is(ModItems.MAGIC_LEAD.get()) && !hasLazo()) {
            if (!this.level().isClientSide) {
                setLazo(true);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Agachado + clic: quedarse quieta / seguir
        if (player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                setOrderedToSit(!isOrderedToSit());
                this.navigation.stop();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Montar
        if (hasLazo() && !player.isPassenger()) {
            if (!this.level().isClientSide) {
                carryTicks = 0;
                this.ejectPassengers();
                setOrderedToSit(false);
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (hasLazo()) this.spawnAtLocation(ModItems.MAGIC_LEAD.get());
    }

    // ------------------------------------------------------------------ montar

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (hasLazo() && isTame() && this.getFirstPassenger() instanceof Player player && isOwnedBy(player)) {
            return player;
        }
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight() * 0.8D;
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity rider = this.getControllingPassenger();
        if (this.isAlive() && rider != null) {
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(rider.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float strafe = rider.xxa * 0.5F;
            float forward = rider.zza;
            if (forward <= 0.0F) forward *= 0.25F;
            // Mirar arriba/abajo mientras avanzas = subir/bajar
            double vertical = -Math.sin(Math.toRadians(rider.getXRot())) * Math.max(rider.zza, 0.0F);

            if (this.isControlledByLocalInstance()) {
                this.setSpeed(0.3F);
                super.travel(new Vec3(strafe, vertical, forward));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
            this.calculateEntityAnimation(false);
            this.tryCheckInsideBlocks();
        } else {
            super.travel(travelVector);
        }
    }

    // ------------------------------------------------------------------ varios

    @Override
    public boolean isNoGravity() { return this.isAlive(); }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.6F;
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ALLAY_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ALLAY_DEATH; }
    @Override protected float getSoundVolume() { return 0.5F; }

    // ================================================================== goals

    /** Domesticada y "quedarse quieta": flota en el sitio. */
    static class StayGoal extends Goal {
        private final NubiEntity nubi;

        StayGoal(NubiEntity nubi) {
            this.nubi = nubi;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override public boolean canUse() { return nubi.isTame() && nubi.isOrderedToSit() && !nubi.isVehicle(); }

        @Override public void start() { nubi.getNavigation().stop(); }

        @Override
        public void tick() {
            nubi.getMoveControl().setWantedPosition(nubi.getX(), nubi.getY(), nubi.getZ(), 0.0D);
        }
    }

    /** Durante la tormenta, ataca con descargas a quien se acerque. */
    static class StormZapGoal extends Goal {
        private final NubiEntity nubi;
        private int cooldown;

        StormZapGoal(NubiEntity nubi) {
            this.nubi = nubi;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = nubi.getTarget();
            return nubi.isStormy() && target != null && target.isAlive();
        }

        @Override public boolean canContinueToUse() { return canUse(); }

        @Override
        public void tick() {
            LivingEntity target = nubi.getTarget();
            if (target == null) return;
            nubi.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = nubi.distanceToSqr(target);
            if (distSqr > 2.25D) {
                nubi.getMoveControl().setWantedPosition(target.getX(), target.getEyeY(), target.getZ(), 1.2D);
            }
            if (cooldown > 0) cooldown--;
            if (cooldown <= 0 && distSqr < 6.25D) {
                cooldown = 30;
                nubi.zap(target, 3.0F, false);
            }
        }
    }

    /** Se acerca a un mob pequeño, lo sube encima y lo pasea un rato. */
    static class CarryMobGoal extends Goal {
        private final NubiEntity nubi;
        private Mob candidate;

        CarryMobGoal(NubiEntity nubi) {
            this.nubi = nubi;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        private boolean isCarryable(Mob mob) {
            return mob != nubi
                    && !(mob instanceof NubiEntity)
                    && mob.isAlive()
                    && !mob.isPassenger()
                    && !mob.isVehicle()
                    && mob.getBbWidth() <= 1.5F
                    && mob.getBbHeight() <= 2.0F
                    && !(mob instanceof TamableAnimal tamable && tamable.isTame());
        }

        @Override
        public boolean canUse() {
            if (nubi.isVehicle() || nubi.isStormy() || nubi.isOrderedToSit()) return false;
            if (nubi.getRandom().nextInt(200) != 0) return false;
            List<Mob> list = nubi.level().getEntitiesOfClass(Mob.class,
                    nubi.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), this::isCarryable);
            if (list.isEmpty()) return false;
            candidate = list.get(nubi.getRandom().nextInt(list.size()));
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return candidate != null && candidate.isAlive() && !candidate.isPassenger()
                    && !nubi.isVehicle() && nubi.distanceToSqr(candidate) < 400.0D;
        }

        @Override
        public void tick() {
            if (candidate == null) return;
            nubi.getLookControl().setLookAt(candidate, 30.0F, 30.0F);
            if (nubi.distanceToSqr(candidate) > 2.5D) {
                nubi.getMoveControl().setWantedPosition(candidate.getX(), candidate.getY() + 1.0D, candidate.getZ(), 1.0D);
            } else if (candidate.startRiding(nubi, true)) {
                nubi.carryTicks = 400 + nubi.getRandom().nextInt(400);
                candidate = null;
            }
        }

        @Override public void stop() { candidate = null; }
    }
}
