package rats.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.List;
import java.util.Optional;

public record RatData(float health, int age, Optional<Text> name, Optional<LazyEntityReference<LivingEntity>> owner, Optional<Text> ownerName, List<StatusEffectInstance> effects, List<EntityAttributeInstance.Packed> attributes) {
    public static final Codec<RatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("health").forGetter(RatData::health),
            Codec.INT.fieldOf("age").forGetter(RatData::age),
            TextCodecs.CODEC.optionalFieldOf("name").forGetter(RatData::name),
            LazyEntityReference.<LivingEntity>createCodec().optionalFieldOf("owner").forGetter(RatData::owner),
            TextCodecs.CODEC.optionalFieldOf("ownerName").forGetter(RatData::ownerName),
            StatusEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(RatData::effects),
            EntityAttributeInstance.Packed.CODEC.listOf().fieldOf("attributes").forGetter(RatData::attributes)
    ).apply(instance, RatData::new));
}
