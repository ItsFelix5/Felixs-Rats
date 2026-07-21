package rats.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.List;
import java.util.Optional;

public record RatData(float health, int age, Optional<Component> name, Optional<EntityReference<LivingEntity>> owner, Optional<Component> ownerName, List<MobEffectInstance> effects, List<AttributeInstance.Packed> attributes) {
    public static final Codec<RatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("health").forGetter(RatData::health),
            Codec.INT.fieldOf("age").forGetter(RatData::age),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(RatData::name),
            EntityReference.<LivingEntity>codec().optionalFieldOf("owner").forGetter(RatData::owner),
            ComponentSerialization.CODEC.optionalFieldOf("ownerName").forGetter(RatData::ownerName),
            MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(RatData::effects),
            AttributeInstance.Packed.CODEC.listOf().fieldOf("attributes").forGetter(RatData::attributes)
    ).apply(instance, RatData::new));
}
