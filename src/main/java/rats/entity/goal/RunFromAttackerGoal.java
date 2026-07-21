package rats.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class RunFromAttackerGoal extends Goal {
    protected final PathfinderMob mob;
    private Mob target;

    public RunFromAttackerGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        List<Entity> list = mob.level().getEntities(mob, AABB.ofSize(mob.position(), 15, 10, 15),
                e -> e instanceof Mob mobEntity && mobEntity.getTarget() == mob);
        if (list.isEmpty()) return false;
        this.target = (Mob) list.getFirst();
        for (RatEntity rat : mob.level().getEntitiesOfClass(RatEntity.class,
                new AABB(mob.position(), mob.position()).inflate(mob.getAttributeValue(Attributes.FOLLOW_RANGE)),
                rat -> rat.getTarget() == null && rat.wantsToAttack(target, rat.getOwner())))
            rat.setTarget(target);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target.isAlive() && target.getTarget() == mob;
    }

    @Override
    public void tick() {
        if (mob.getNavigation().isDone() || mob.getNavigation().getTargetPos().distSqr(BlockPos.containing(mob.position())) < 16) {
            Vec3 vec3d = DefaultRandomPos.getPosAway(this.mob, 160, 7, target.position());
            if (vec3d == null) return;
            Path fleePath = mob.getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, 0);
            if (fleePath == null) return;
            mob.getNavigation().moveTo(fleePath, 1.3);
        }
    }
}
