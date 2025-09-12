package rats.entity.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RunFromTntGoal extends Goal {
    protected final PathAwareEntity mob;
    protected Path fleePath;

    public RunFromTntGoal(PathAwareEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        ServerWorld world = getServerWorld(this.mob);
        double d = 36;
        Entity target = null;

        for (Entity entity : world.getEntitiesByClass(TntEntity.class, this.mob.getBoundingBox().expand(6.0, 3.0, 6.0), potentialEntity -> true)) {
            double distance = entity.squaredDistanceTo(mob.getPos());
            if (distance < d) {
                d = distance;
                target = entity;
            }
        }
        if (target == null) return false;
        Vec3d vec3d = FuzzyTargeting.findFrom(this.mob, 160, 7, target.getPos());
        if (vec3d == null) return false;
        this.fleePath = mob.getNavigation().findPathTo(vec3d.x, vec3d.y, vec3d.z, 0);
        return this.fleePath != null;
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingAlong(this.fleePath, 1.2);
    }
}
