package mchorse.snb.api.metamorph.editor;

import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.client.gui.editor.GuiAnimation;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import mchorse.snb.api.animation.model.AnimatorPoseTransform;
import mchorse.snb.api.metamorph.AnimatedMorph;
import mchorse.snb.api.metamorph.AnimatedPose;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * General morph panel for animated morph editor
 */
public class GuiAnimatedMorphPanel extends GuiMorphPanel<AnimatedMorph, GuiAnimatedMorph> implements IBonePicker
{
    public GuiTextElement name;
    public GuiTrackpadElement scale;
    public GuiTrackpadElement scaleGui;
    public GuiTrackpadElement scaleItems;
    public GuiToggleElement renderHeldItems;
    public GuiTextElement head;

    public GuiButtonElement createPose;
    public GuiStringListElement bones;
    public GuiToggleElement fixed;
    public GuiToggleElement animated;
    public GuiPoseTransformations transforms;
    public GuiAnimation animation;

    private IKey createLabel = IKey.str("Create pose");
    private IKey resetLabel = IKey.str("Reset pose");

    private AnimatorPoseTransform transform;

    public GuiAnimatedMorphPanel(Minecraft mc, GuiAnimatedMorph editor)
    {
        super(mc, editor);

        /* Pose editor */
        this.createPose = new GuiButtonElement(mc, this.createLabel, this::createResetPose);
        this.bones = new GuiStringListElement(mc, this::pickBone);
        this.bones.background();
        this.fixed = new GuiToggleElement(mc, IKey.str("Fixate movement"), this::toggleFixed);
        this.animated = new GuiToggleElement(mc, IKey.lang("Animated pose"), this::toggleAnimated);
        this.transforms = new GuiPoseTransformations(mc);

        this.createPose.flex().relative(this).xy(10, 10).w(110).h(20);
        this.bones.flex().relative(this.createPose).y(1F, 5).w(1F).hTo(this.fixed.flex(), -5);
        this.animated.flex().relative(this).x(10).y(1F, -10).w(110).anchorY(1);
        this.fixed.flex().relative(this.animated).y(-1F, -5).w(1F);
        this.transforms.flex().relative(this).set(0, 0, 256, 70).x(0.5F, -128).y(1, -80);

        this.add(this.createPose, this.animated, this.fixed, this.bones, this.transforms);

        /* General */
        this.name = new GuiTextElement(mc, 120, (str) -> this.editor.userConfig.name = str);
        this.scale = new GuiTrackpadElement(mc, (value) ->
        {
            this.editor.userConfig.scale = value.floatValue();
            this.editor.updateMorph();
        });
        this.scale.tooltip(IKey.str("Scale"));
        this.scaleGui = new GuiTrackpadElement(mc, (value) -> this.editor.userConfig.scaleGui = value.floatValue());
        this.scaleGui.tooltip(IKey.str("GUI scale"));
        this.scaleItems = new GuiTrackpadElement(mc, (value) -> this.editor.userConfig.scaleItems = value.floatValue());
        this.scaleItems.tooltip(IKey.str("Items scale"));
        this.renderHeldItems = new GuiToggleElement(mc, IKey.str("Render items"), true, (b) -> this.editor.userConfig.renderHeldItems = b.isToggled());
        this.head = new GuiTextElement(mc, 120, (str) ->
        {
            this.editor.userConfig.head = str;
            this.editor.updateMorph();
        });

        this.animation = new GuiAnimation(mc, false);
        this.animation.flex().column(5).padding(0);
        this.animation.interpolations.removeFromParent();

        GuiScrollElement element = new GuiScrollElement(mc);

        element.cancelScrollEdge();
        element.add(this.animation);
        element.add(Elements.label(IKey.str("Display name")), this.name);
        element.add(Elements.label(IKey.str("Scale")), this.scale, this.scaleGui, this.scaleItems, this.renderHeldItems);
        element.add(Elements.label(IKey.str("Head bone")), this.head);

        element.flex().relative(this).x(1F).h(1F).w(130).anchorX(1F).column(5).vertical().stretch().scroll().padding(10);

        this.add(element, this.animation.interpolations);
    }

    private void createResetPose(GuiButtonElement button)
    {
        if (this.morph.pose == null)
        {
            AnimatedPose pose = new AnimatedPose();

            for (String bone : this.morph.animator.animation.meshes.get(0).armature.bones.keySet())
            {
                pose.bones.put(bone, new AnimatorPoseTransform(bone));
            }

            this.morph.pose = pose;
        }
        else
        {
            this.morph.pose = null;
            this.editor.model.bone = "";
        }

        this.setPoseEditorVisible();
    }

    private void pickBone(List<String> bone)
    {
        this.pickBone(bone.get(0));
    }

    @Override
    public void pickBone(String bone)
    {
        if (this.morph.pose == null)
        {
            return;
        }

        this.transform = this.morph.pose.bones.get(bone);

        this.bones.setCurrentScroll(bone);
        this.animated.toggled(this.morph.animated);
        this.fixed.toggled(this.transform.fixed == AnimatorPoseTransform.FIXED);
        this.transforms.set(this.transform);
        this.editor.model.bone = bone;
    }

    private void toggleFixed(GuiToggleElement toggle)
    {
        this.transform.fixed = toggle.isToggled() ? AnimatorPoseTransform.FIXED : AnimatorPoseTransform.ANIMATED;
    }

    private void toggleAnimated(GuiToggleElement toggle)
    {
        this.morph.animated = toggle.isToggled();
    }

    @Override
    public void fillData(AnimatedMorph morph)
    {
        super.fillData(morph);

        this.setPoseEditorVisible();

        this.name.setText(this.editor.userConfig.name);
        this.scale.setValue(this.editor.userConfig.scale);
        this.scaleGui.setValue(this.editor.userConfig.scaleGui);
        this.scaleItems.setValue(this.editor.userConfig.scaleItems);
        this.renderHeldItems.toggled(this.editor.userConfig.renderHeldItems);
        this.head.setText(this.editor.userConfig.head);

        this.animation.fill(morph.animation);
    }

    private void setPoseEditorVisible()
    {
        AnimatedPose pose = this.morph.pose;

        this.createPose.label = pose == null ? this.createLabel : this.resetLabel;
        this.bones.setVisible(pose != null);
        this.fixed.setVisible(pose != null);
        this.animated.setVisible(pose != null);
        this.transforms.setVisible(pose != null);

        this.bones.clear();
        this.bones.add(this.morph.animator.animation.meshes.get(0).armature.bones.keySet());
        this.bones.sort();

        if (pose != null)
        {
            this.pickBone(this.bones.getList().get(0));
        }
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);
    }

    public static class GuiPoseTransformations extends GuiTransformations
    {
        public AnimatorPoseTransform trans;

        public GuiPoseTransformations(Minecraft mc)
        {
            super(mc);
        }

        public void set(AnimatorPoseTransform trans)
        {
            this.trans = trans;

            if (trans != null)
            {
                this.fillT(trans.x, trans.y, trans.z);
                this.fillS(trans.scaleX, trans.scaleY, trans.scaleZ);
                this.fillR(trans.rotateX / (float) Math.PI * 180, trans.rotateY / (float) Math.PI * 180, trans.rotateZ / (float) Math.PI * 180);
            }
        }

        @Override
        public void setT(double x, double y, double z)
        {
            this.trans.x = (float) x;
            this.trans.y = (float) y;
            this.trans.z = (float) z;
        }

        @Override
        public void setS(double x, double y, double z)
        {
            this.trans.scaleX = (float) x;
            this.trans.scaleY = (float) y;
            this.trans.scaleZ = (float) z;
        }

        @Override
        public void setR(double x, double y, double z)
        {
            this.trans.rotateX = (float) (x / 180F * (float) Math.PI);
            this.trans.rotateY = (float) (y / 180F * (float) Math.PI);
            this.trans.rotateZ = (float) (z / 180F * (float) Math.PI);
        }
    }
}