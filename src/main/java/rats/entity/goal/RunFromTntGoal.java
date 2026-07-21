package rats.entity.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RunFromTntGoal extends Goal {
    protected final PathfinderMob mob;
    protected Path fleePath;

    public RunFromTntGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        ServerLevel world = getServerLevel(this.mob);
        double d = 36;
        Entity target = null;

        for (Entity entity : world.getEntitiesOfClass(PrimedTnt.class, this.mob.getBoundingBox().inflate(6.0, 3.0, 6.0), potentialEntity -> true)) {
            double distance = entity.distanceToSqr(mob.position());
            if (distance < d) {
                d = distance;
                target = entity;
            }
        }
        if (target == null) return false;
        Vec3 vec3d = DefaultRandomPos.getPosAway(this.mob, 160, 7, target.position());
        if (vec3d == null) return false;
        this.fleePath = mob.getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, 0);
        return this.fleePath != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(this.fleePath, 1.2);
    }
}
