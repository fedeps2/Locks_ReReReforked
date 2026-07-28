package melonslise.locks.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import melonslise.locks.Locks;
import melonslise.locks.common.components.interfaces.ISelection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class Selection implements ISelection {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Locks.ID, "selection");

    public BlockPos pos;

    public Selection(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public BlockPos get()
    {
        return this.pos;
    }

    @Override
    public void set(BlockPos pos)
    {
        this.pos = pos;
    }

    private BlockPos getNonNullPos() {
        return this.pos != null ? this.pos : BlockPos.ZERO;
    }

    public static final Codec<Selection> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Selection::getNonNullPos)
        ).apply(instance, Selection::new)
    );
}
