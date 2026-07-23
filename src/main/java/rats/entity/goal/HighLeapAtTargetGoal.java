package rats.entity.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import rats.entity.RatEntity;

import java.util.EnumSet;

public class HighLeapAtTargetGoal extends Goal {
    private static final double MIN_VERTICAL_GAP = 0.5;
    private static final double MAX_VERTICAL_GAP = 4.25;
    private static final double MAX_START_DISTANCE_SQR = 36.0;
    private static final double RUN_UP_DISTANCE = 5.25;
    private static final double MIN_RUN_UP_DISTANCE = 4.75;
    private static final int MAX_BACK_UP_TICKS = 35;
    private static final int MAX_LEAP_TICKS = 30;
    private static final int RETRY_COOLDOWN = 8;
    private static final double BACK_UP_SPEED = 1.25;

    private final RatEntity mob;
    private LivingEntity target;
    private Vec3 runUpPosition;
    private Phase phase;
    private int phaseTicks;
    private int cooldown;
    private boolean attacked;

    public HighLeapAtTargetGoal(RatEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (mob.hasControllingPassenger() || !mob.onGround()) return false;

        LivingEntity currentTarget = mob.getTarget();
        if (!isValidTarget(currentTarget)) return false;

        target = currentTarget;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || target != mob.getTarget() || !target.isAlive()) return false;

        return switch (phase) {
            case BACKING_UP -> phaseTicks < MAX_BACK_UP_TICKS
                    && mob.onGround()
                    && isElevatedOutOfReach(target)
                    && horizontalDistanceSqr(target) <= MAX_START_DISTANCE_SQR * 2.0;
            case LEAPING -> phaseTicks < MAX_LEAP_TICKS && (!mob.onGround() || phaseTicks < 3);
        };
    }

    @Override
    public void start() {
        attacked = false;
        phaseTicks = 0;
        runUpPosition = findRunUpPosition();

        if (horizontalDistanceSqr(target) >= MIN_RUN_UP_DISTANCE * MIN_RUN_UP_DISTANCE) {
            leap();
        } else {
            phase = Phase.BACKING_UP;
            moveToRunUpPosition();
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        target = null;
        runUpPosition = null;
        cooldown = RETRY_COOLDOWN;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) return;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        phaseTicks++;

        if (phase == Phase.BACKING_UP) {
            double distanceSqr = horizontalDistanceSqr(target);
            boolean hasRunUp = distanceSqr >= MIN_RUN_UP_DISTANCE * MIN_RUN_UP_DISTANCE;

            if (hasRunUp || phaseTicks >= MAX_BACK_UP_TICKS) {
                leap();
            } else {
                // Pathfinding commonly finishes at the foot of a pillar. Keep
                // issuing direct movement instead of waiting underneath it.
                if (phaseTicks % 5 == 0) {
                    runUpPosition = findRunUpPosition();
                    moveToRunUpPosition();
                }
                if (mob.getNavigation().isDone()) {
                    mob.getMoveControl().setWantedPosition(
                            runUpPosition.x,
                            runUpPosition.y,
                            runUpPosition.z,
                            BACK_UP_SPEED
                    );
                }
            }
        } else if (!attacked
                && mob.isWithinMeleeAttackRange(target)
                && mob.getSensing().hasLineOfSight(target)) {
            mob.swing(InteractionHand.MAIN_HAND);
            attacked = mob.doHurtTarget(getServerLevel(mob), target);
        }
    }

    private boolean isValidTarget(LivingEntity candidate) {
        if (candidate == null
                || !candidate.isAlive()
                || horizontalDistanceSqr(candidate) > MAX_START_DISTANCE_SQR
                || !isElevatedOutOfReach(candidate)) {
            return false;
        }

        Path path = mob.getNavigation().createPath(candidate, 0);
        return path == null || !path.canReach() || mob.getNavigation().isStuck();
    }

    private boolean isElevatedOutOfReach(LivingEntity candidate) {
        double verticalGap = candidate.getBoundingBox().minY - mob.getBoundingBox().maxY;
        return verticalGap >= MIN_VERTICAL_GAP && verticalGap <= MAX_VERTICAL_GAP;
    }

    private Vec3 findRunUpPosition() {
        Vec3 away = horizontalDirectionFromTarget();
        double currentDistance = Math.sqrt(horizontalDistanceSqr(target));
        double extraDistance = Math.max(0.0, RUN_UP_DISTANCE - currentDistance);
        return mob.position().add(away.scale(extraDistance));
    }

    private Vec3 horizontalDirectionFromTarget() {
        Vec3 away = new Vec3(mob.getX() - target.getX(), 0.0, mob.getZ() - target.getZ());
        if (away.lengthSqr() > 1.0E-7) return away.normalize();

        double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
        return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
    }

    private void moveToRunUpPosition() {
        Path path = mob.getNavigation().createPath(
                runUpPosition.x,
                runUpPosition.y,
                runUpPosition.z,
                0
        );
        if (path != null) {
            mob.getNavigation().moveTo(path, BACK_UP_SPEED);
        } else {
            mob.getMoveControl().setWantedPosition(
                    runUpPosition.x,
                    runUpPosition.y,
                    runUpPosition.z,
                    BACK_UP_SPEED
            );
        }
    }

    private void leap() {
        phase = Phase.LEAPING;
        phaseTicks = 0;
        mob.getNavigation().stop();

        Vec3 towardTarget = new Vec3(
                target.getX() - mob.getX(),
                0.0,
                target.getZ() - mob.getZ()
        );
        if (towardTarget.lengthSqr() <= 1.0E-7) {
            towardTarget = horizontalDirectionFromTarget().reverse();
        } else {
            towardTarget = towardTarget.normalize();
        }

        double horizontalDistance = Math.sqrt(horizontalDistanceSqr(target));
        double horizontalSpeed = Math.clamp(horizontalDistance * 0.27, 0.68, 0.95);
        double heightDifference = target.getY() - mob.getY();
        double verticalSpeed = Math.clamp(0.84 + Math.max(0.0, heightDifference - 2.0) * 0.10, 0.84, 1.05);

        Vec3 velocity = towardTarget.scale(horizontalSpeed).add(0.0, verticalSpeed, 0.0);
        mob.setDeltaMovement(velocity);
    }

    private double horizontalDistanceSqr(LivingEntity entity) {
        double dx = entity.getX() - mob.getX();
        double dz = entity.getZ() - mob.getZ();
        return dx * dx + dz * dz;
    }

    private enum Phase {
        BACKING_UP,
        LEAPING
    }
}
