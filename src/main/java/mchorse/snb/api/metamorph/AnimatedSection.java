package mchorse.snb.api.metamorph;

import mchorse.metamorph.api.creative.categories.MorphCategory;
import mchorse.metamorph.api.creative.sections.MorphSection;
import mchorse.snb.ClientProxy;
import mchorse.snb.api.animation.AnimationManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class AnimatedSection extends MorphSection
{
    public MorphCategory category;

    public AnimatedSection(String title)
    {
        super(title);

        this.category = new MorphCategory(this, "snb");
    }

    @Override
    public void update(World world)
    {
        ClientProxy.modelHandler.refreshAnimations();

        this.category.clear();

        for (AnimationManager.AnimationEntry entry : AnimationManager.INSTANCE.animations.values())
        {
            AnimatedMorph morph = new AnimatedMorph();
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Name", "snb." + entry.animation.name);
            tag.setString("Animation", entry.animation.name);

            morph.fromNBT(tag);

            this.category.add(morph);
        }

        this.categories.clear();
        this.categories.add(this.category);
    }

    @Override
    public void reset()
    {
        this.categories.clear();
    }
}