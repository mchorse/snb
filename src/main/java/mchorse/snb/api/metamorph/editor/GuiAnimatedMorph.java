package mchorse.snb.api.metamorph.editor;

import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.bodypart.GuiBodyPartEditor;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import mchorse.snb.api.animation.AnimationManager;
import mchorse.snb.api.animation.model.AnimatorConfig;
import mchorse.snb.api.metamorph.AnimatedMorph;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAnimatedMorph extends GuiAbstractMorph<AnimatedMorph>
{
    public AnimatorConfig userConfig;

    public GuiAnimatedMorphPanel general;
    public GuiMeshesPanel meshes;
    public GuiActionsPanel actions;
    public GuiBodyPartEditor bodyPart;

    public GuiAnimatedModelRenderer model;

    public GuiAnimatedMorph(Minecraft mc)
    {
        super(mc);

        /* Animated morph panels */
        this.general = new GuiAnimatedMorphPanel(mc, this);
        this.meshes = new GuiMeshesPanel(mc, this);
        this.actions = new GuiActionsPanel(mc, this);
        this.bodyPart = new GuiAnimatedBodyPartEditor(mc, this);
        this.defaultPanel = this.general;

        this.registerPanel(this.bodyPart, IKey.str("Body part"), Icons.LIMB);
        this.registerPanel(this.meshes, IKey.str("Meshes"), Icons.MATERIAL);
        this.registerPanel(this.actions, IKey.str("Actions"), Icons.MORE);
        this.registerPanel(this.general, IKey.str("General"), Icons.POSE);

        /* Miscellaneous */
        this.prepend(new GuiDrawable((n) ->
        {
            this.drawGradientRect(0, this.area.ey() - 30, this.area.w, this.area.ey(), 0x00000000, 0x88000000);
        }));
    }

    @Override
    public void setPanel(GuiMorphPanel panel)
    {
        this.model.bone = null;

        super.setPanel(panel);
    }

    @Override
    protected GuiModelRenderer createMorphRenderer(Minecraft mc)
    {
        this.model = new GuiAnimatedModelRenderer(mc);
        this.model.looking = false;
        this.model.picker((bone) ->
        {
            if (this.view.delegate instanceof IBonePicker)
            {
                ((IBonePicker) this.view.delegate).pickBone(bone);
            }
        });

        return this.model;
    }

    @Override
    public boolean canEdit(AbstractMorph morph)
    {
        if (morph instanceof AnimatedMorph)
        {
            AnimatedMorph animated = (AnimatedMorph) morph;

            return AnimationManager.INSTANCE.animations.containsKey(animated.animationName);
        }

        return false;
    }

    @Override
    public void startEdit(AnimatedMorph morph)
    {
        morph.parts.initBodyParts();
        morph.initiateAnimator();
        this.userConfig = new AnimatorConfig();
        this.bodyPart.setLimbs(morph.animator.animation.collectBones());
        this.model.controller = morph.animator;

        if (morph.userConfigData != null && !morph.userConfigData.hasNoTags())
        {
            this.userConfig.fromNBT(morph.userConfigData);
        }

        super.startEdit(morph);
    }

    @Override
    public void finishEdit()
    {
        this.updateMorph();
        super.finishEdit();
    }

    public void updateMorph()
    {
        this.morph.userConfigData = this.userConfig.toNBT(null);
        this.morph.userConfigChanged = true;
        this.morph.updateAnimator();
    }

}