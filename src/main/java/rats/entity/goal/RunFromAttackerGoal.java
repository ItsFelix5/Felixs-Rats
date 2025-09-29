package rats.entity.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class RunFromAttackerGoal extends Goal {
    protected final PathAwareEntity mob;
    private MobEntity target;

    public RunFromAttackerGoal(PathAwareEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        List<Entity> list = mob.getWorld().getOtherEntities(mob, Box.of(mob.getPos(), 15, 10, 15),
                e -> e instanceof MobEntity mobEntity && mobEntity.getTarget() == mob);
        if (list.isEmpty()) return false;
        this.target = (MobEntity) list.getFirst();
        for (RatEntity rat : mob.getWorld().getEntitiesByClass(RatEntity.class,
                Box.from(mob.getPos()).expand(mob.getAttributeValue(EntityAttributes.FOLLOW_RANGE)),
                rat -> rat.getTarget() == null && rat.canAttackWithOwner(target, rat.getOwner())))
            rat.setTarget(target);
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return target.isAlive() && target.getTarget() == mob;
    }

    @Override
    public void tick() {
        if (mob.getNavigation().isIdle() || mob.getNavigation().getTargetPos().getSquaredDistance(mob.getPos()) < 16) {
            Vec3d vec3d = FuzzyTargeting.findFrom(this.mob, 160, 7, target.getPos());
            if (vec3d == null) return;
            Path fleePath = mob.getNavigation().findPathTo(vec3d.x, vec3d.y, vec3d.z, 0);
            if (fleePath == null) return;
            mob.getNavigation().startMovingAlong(fleePath, 1.3);
        }
    }
}

