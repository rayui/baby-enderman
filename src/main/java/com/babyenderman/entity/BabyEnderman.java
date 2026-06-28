package com.babyenderman.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A baby Enderman companion.
 *
 * <p>Walks like an Enderman (but never teleports and can't hurt the player), does cute
 * little jumps, picks up grass/dirt/stone/sand blocks, and can be tamed with three ender
 * pearls. Once tamed it rides on the player's shoulder and occasionally gifts blocks.</p>
 */
public class BabyEnderman extends TamableAnimal {
    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE =
            SynchedEntityData.defineId(BabyEnderman.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_PERCHED =
            SynchedEntityData.defineId(BabyEnderman.class, EntityDataSerializers.BOOLEAN);

    /** Blocks the baby Enderman is allowed to pick up and carry. */
    public static boolean isCarriable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.SAND);
    }

    private static final Block[] GIFT_BLOCKS = {Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE, Blocks.SAND};

    private static final int EMERALDS_TO_TAME = 3;
    private int tameProgress;
    /** Cooldown so it doesn't gift on every single tick while riding. */
    private int giftCooldown = 200;

    public BabyEnderman(EntityType<? extends BabyEnderman> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerWhenTamedGoal());
        // Untamed: follow a player holding emeralds (green gems for its green eyes!).
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, stack -> stack.is(Items.EMERALD), false));
        this.goalSelector.addGoal(4, new TakeBlockGoal());
        this.goalSelector.addGoal(5, new LeaveBlockGoal());
        this.goalSelector.addGoal(6, new CuteJumpGoal());
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARRY_STATE, Optional.empty());
        builder.define(DATA_PERCHED, false);
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(state));
    }

    public @Nullable BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRY_STATE).orElse(null);
    }

    // ----- Taming, riding & interaction --------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (stack.is(Items.EMERALD)) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    this.tameProgress++;
                    // Audible "nom" so you know it took the emerald.
                    this.playSound(SoundEvents.GENERIC_EAT.value(), 0.7F, 1.5F);
                    if (this.tameProgress >= EMERALDS_TO_TAME) {
                        this.tame(player);
                        this.setPersistenceRequired();
                        this.navigation.stop();
                        this.level().broadcastEntityEvent(this, (byte) 7); // big purple-heart burst
                        player.displayClientMessage(
                                Component.literal("Baby Enderman tamed! Right-click it with an empty hand to ride your shoulder.")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                                true);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // purple sparkle puff
                        player.displayClientMessage(
                                Component.literal("Baby Enderman likes the emerald… (" + this.tameProgress + "/" + EMERALDS_TO_TAME + ")")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                                true);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        // Tamed: only the owner can boss it around.
        if (this.isOwnedBy(player)) {
            if (player.isSecondaryUseActive()) {
                // sneak-click toggles sitting (and gets it off your shoulder)
                if (!this.level().isClientSide()) {
                    this.setPerched(false);
                    boolean sit = !this.isOrderedToSit();
                    this.setOrderedToSit(sit);
                    this.setInSittingPose(sit);
                    this.navigation.stop();
                }
                return InteractionResult.SUCCESS;
            }

            if (stack.isEmpty()) {
                // empty-hand click: hop onto / off the shoulder
                if (!this.level().isClientSide()) {
                    if (this.isPerched()) {
                        this.setPerched(false);
                        player.displayClientMessage(
                                Component.literal("Baby Enderman hops down.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    } else {
                        this.setOrderedToSit(false);
                        this.setInSittingPose(false);
                        this.setPerched(true);
                        this.giftCooldown = 200; // first present ~10s after it settles in
                        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.4F, 1.8F);
                        player.displayClientMessage(
                                Component.literal("Baby Enderman hops onto your shoulder!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    /**
     * "Perched" = sitting on the owner's shoulder. Players can't be ridden as a vehicle
     * (their entity type isn't serializable), so instead we switch off the AI and glue the
     * mob to the owner's shoulder every tick (see {@link #tick()}).
     */
    public boolean isPerched() {
        return this.entityData.get(DATA_PERCHED);
    }

    public void setPerched(boolean perched) {
        this.entityData.set(DATA_PERCHED, perched);
        this.setNoAi(perched);
        this.setNoGravity(perched);
        this.noPhysics = perched;
        if (perched) {
            this.getNavigation().stop();
        } else {
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
    }

    /** While perched, other entities shouldn't be able to shove it off your shoulder. */
    @Override
    public boolean isPushable() {
        return !this.isPerched() && super.isPushable();
    }

    /**
     * The real fix for "the shoulder pet pushes the player": {@code pushEntities()} runs on the
     * client too (even for NoAi mobs), and there the baby pushes the local player based on the
     * PLAYER's pushability — the only guard is {@code noPhysics}, which is server-only/unsynced and
     * therefore false on the client. The perched flag IS synced, so skipping the push here stops it
     * on both sides.
     */
    @Override
    protected void pushEntities() {
        if (this.isPerched()) {
            return;
        }
        super.pushEntities();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() || !this.isPerched()) {
            return;
        }

        // Glue the baby to the owner's shoulder.
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof Player ownerPlayer) || owner.isRemoved() || owner.isSpectator()
                || owner.level() != this.level() || this.distanceToSqr(owner) > 400.0) {
            this.setPerched(false);
            return;
        }

        float yaw = ownerPlayer.yBodyRot;
        double rad = Math.toRadians(yaw);
        double sideways = 0.32; // toward the owner's right shoulder
        double ox = -Math.cos(rad) * sideways;
        double oz = -Math.sin(rad) * sideways;
        double yOff = Math.max(0.6, ownerPlayer.getEyeHeight() - 0.35); // ~shoulder height, follows sneaking
        this.setPos(ownerPlayer.getX() + ox, ownerPlayer.getY() + yOff, ownerPlayer.getZ() + oz);
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.setYRot(ownerPlayer.getYRot());
        this.yBodyRot = ownerPlayer.yBodyRot;
        this.setYHeadRot(ownerPlayer.getYHeadRot());
        this.fallDistance = 0.0F;

        // Occasionally hand the owner a present.
        if (--this.giftCooldown <= 0) {
            this.giveGift(ownerPlayer);
            this.giftCooldown = 400 + this.random.nextInt(400); // ~20-40s between gifts
        }
    }

    private void giveGift(Player owner) {
        Block block = GIFT_BLOCKS[this.random.nextInt(GIFT_BLOCKS.length)];
        ItemStack gift = new ItemStack(block);
        if (!owner.getInventory().add(gift)) {
            owner.drop(gift, false);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6, this.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
            serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.6, this.getZ(), 8, 0.2, 0.3, 0.2, 0.3);
        }
        this.playSound(SoundEvents.ENDERMAN_AMBIENT, 0.3F, 1.6F);
    }

    /**
     * Purple sparkles every time it's fed; a bigger burst of purple hearts when it's tamed.
     * (event 7 -> success/tamed, event 6 -> a feed that didn't finish taming yet.)
     */
    @Override
    protected void spawnTamingParticles(boolean success) {
        int sparkles = success ? 12 : 6;
        for (int i = 0; i < sparkles; i++) {
            // enderman-purple sparkle on every feed
            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                    this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1);
        }
        if (success) {
            // ...plus a shower of hearts when it finally becomes yours
            for (int i = 0; i < 7; i++) {
                this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
            }
        }
    }

    // ----- Persistence -------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        BlockState carried = this.getCarriedBlock();
        if (carried != null) {
            output.store("CarriedBlockState", BlockState.CODEC, carried);
        }
        output.putInt("TameProgress", this.tameProgress);
        output.putBoolean("Perched", this.isPerched());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setCarriedBlock(input.read("CarriedBlockState", BlockState.CODEC).filter(s -> !s.isAir()).orElse(null));
        this.tameProgress = input.getIntOr("TameProgress", 0);
        this.setPerched(input.getBooleanOr("Perched", false));
    }

    // ----- Misc passive-mob plumbing ----------------------------------------

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null; // baby Endermen don't breed
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return !this.isTame() && !this.isPersistenceRequired();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F; // quiet & shy
    }

    // ----- Goals -------------------------------------------------------------

    /** Follow the owner around once tamed (but not while sitting or riding). */
    private class FollowOwnerWhenTamedGoal extends net.minecraft.world.entity.ai.goal.FollowOwnerGoal {
        FollowOwnerWhenTamedGoal() {
            super(BabyEnderman.this, 1.1, 5.0F, 2.0F);
        }
    }

    /** Every now and then, do a happy little hop. */
    private class CuteJumpGoal extends Goal {
        private int cooldown;

        CuteJumpGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (BabyEnderman.this.isPerched() || BabyEnderman.this.isOrderedToSit() || !BabyEnderman.this.onGround()) {
                return false;
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            return BabyEnderman.this.getRandom().nextInt(reducedTickDelay(120)) == 0;
        }

        @Override
        public void start() {
            Vec3 v = BabyEnderman.this.getDeltaMovement();
            BabyEnderman.this.setDeltaMovement(v.x, 0.42, v.z);
            this.cooldown = reducedTickDelay(60);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Pick up a nearby grass/dirt/stone/sand block to carry, like an Enderman. */
    private class TakeBlockGoal extends Goal {
        @Override
        public boolean canUse() {
            return BabyEnderman.this.getCarriedBlock() == null
                    && net.neoforged.neoforge.event.EventHooks.canEntityGrief(getServerLevel(BabyEnderman.this.level()), BabyEnderman.this)
                    && BabyEnderman.this.getRandom().nextInt(reducedTickDelay(40)) == 0;
        }

        @Override
        public void tick() {
            RandomSource random = BabyEnderman.this.getRandom();
            Level level = BabyEnderman.this.level();
            int x = Mth.floor(BabyEnderman.this.getX() - 1.5 + random.nextDouble() * 3.0);
            int y = Mth.floor(BabyEnderman.this.getY() + random.nextDouble() * 2.0);
            int z = Mth.floor(BabyEnderman.this.getZ() - 1.5 + random.nextDouble() * 3.0);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (isCarriable(state)) {
                level.removeBlock(pos, false);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(BabyEnderman.this, state));
                BabyEnderman.this.setCarriedBlock(state.getBlock().defaultBlockState());
            }
        }
    }

    /** Occasionally set a carried block back down. */
    private class LeaveBlockGoal extends Goal {
        @Override
        public boolean canUse() {
            return BabyEnderman.this.getCarriedBlock() != null
                    && !BabyEnderman.this.isPerched()
                    && net.neoforged.neoforge.event.EventHooks.canEntityGrief(getServerLevel(BabyEnderman.this.level()), BabyEnderman.this)
                    && BabyEnderman.this.getRandom().nextInt(reducedTickDelay(2000)) == 0;
        }

        @Override
        public void tick() {
            RandomSource random = BabyEnderman.this.getRandom();
            Level level = BabyEnderman.this.level();
            int x = Mth.floor(BabyEnderman.this.getX() - 1.0 + random.nextDouble() * 2.0);
            int y = Mth.floor(BabyEnderman.this.getY() + random.nextDouble() * 2.0);
            int z = Mth.floor(BabyEnderman.this.getZ() - 1.0 + random.nextDouble() * 2.0);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState carried = BabyEnderman.this.getCarriedBlock();
            if (carried != null
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())
                    && level.getEntities(BabyEnderman.this, new net.minecraft.world.phys.AABB(pos)).isEmpty()) {
                level.setBlock(pos, carried, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(BabyEnderman.this, carried));
                BabyEnderman.this.setCarriedBlock(null);
            }
        }
    }
}
