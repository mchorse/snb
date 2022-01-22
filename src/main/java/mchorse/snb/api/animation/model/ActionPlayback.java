package mchorse.snb.api.animation.model;

import mchorse.mclib.utils.MathUtils;
import mchorse.snb.api.bobj.BOBJAction;
import mchorse.snb.api.bobj.BOBJArmature;
import mchorse.snb.api.bobj.BOBJBone;
import mchorse.snb.api.bobj.BOBJGroup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ActionPlayback
{
    public List<BOBJAction> actions;
    public ActionConfig config;

    private int fade;
    private float ticks;
    private float speed = 1;

    private boolean looping;
    private boolean fading;
    public boolean playing = true;
    public int priority;

    private BOBJAction current;

    public ActionPlayback(List<BOBJAction> actions, ActionConfig config)
    {
        this(actions, config, true);
    }

    public ActionPlayback(List<BOBJAction> actions, ActionConfig config, boolean looping)
    {
        this.actions = actions;
        this.config = config;
        this.looping = looping;
        this.setSpeed(1);
    }

    public ActionPlayback(List<BOBJAction> actions, ActionConfig config, boolean looping, int priority)
    {
        this(actions, config, looping);
        this.priority = priority;
    }

    private int getDuration()
    {
        return this.current == null ? 0 : this.current.getDuration();
    }

    /* Action playback control methods */

    /**
     * Resets the animation (if config allows) 
     */
    public void reset()
    {
        int index = this.actions.indexOf(this.current);

        if (this.config.randomVariant)
        {
            this.current = this.actions.get((int) (Math.random() * this.actions.size()));
        }
        else
        {
            this.current = this.actions.get(MathUtils.cycler(index + 1, 0, this.actions.size() - 1));
        }

        if (this.config.reset)
        {
            this.ticks = Math.copySign(1, this.speed) < 0 ? this.getDuration() : 0;
        }

        this.unfade();
    }

    /**
     * Whether this action playback finished fading 
     */
    public boolean finishedFading()
    {
        return this.fading && this.fade <= 0;
    }

    /**
     * Whether this action playback is fading 
     */
    public boolean isFading()
    {
        return this.fading && this.fade > 0;
    }

    /**
     * Start fading 
     */
    public void fade()
    {
        this.fade = (int) this.config.fade;
        this.fading = true;
    }

    /**
     * Reset fading
     */
    public void unfade()
    {
        this.fade = 0;
        this.fading = false;
    }

    /**
     * Calculate fade factor with given partial ticks
     * 
     * Closer to 1 means started fading, meanwhile closer to 0 is almost 
     * finished fading.
     */
    public float getFadeFactor(float partialTicks)
    {
        return (this.fade - partialTicks) / this.config.fade;
    }

    /**
     * Set speed of an action playback 
     */
    public void setSpeed(float speed)
    {
        this.speed = speed * this.config.speed;
    }

    /* Update methods */

    public void update()
    {
        if (this.fading && this.fade > 0)
        {
            this.fade--;

            return;
        }

        if (!this.playing) return;

        this.ticks += this.speed;

        if (!this.looping && !this.fading && this.ticks >= this.getDuration())
        {
            this.fade();
        }

        if (this.looping)
        {
            if (this.ticks >= this.getDuration() && this.speed > 0 && this.config.clamp)
            {
                this.ticks -= this.getDuration();
                this.ticks += this.config.tick;
            }
            else if (this.ticks < 0 && this.speed < 0 && this.config.clamp)
            {
                this.ticks = this.getDuration() + this.ticks;
                this.ticks -= this.config.tick;
            }
        }
    }

    public float getTick(float partialTick)
    {
        float ticks = this.ticks + partialTick * this.speed;

        if (this.looping)
        {
            if (ticks >= this.getDuration() && this.speed > 0 && this.config.clamp)
            {
                ticks -= this.getDuration();
            }
            else if (this.ticks < 0 && this.speed < 0 && this.config.clamp)
            {
                ticks = this.getDuration() + ticks;
            }
        }

        return ticks;
    }

    public void apply(BOBJArmature armature, float partialTick)
    {
        if (this.current == null)
        {
            return;
        }

        for (BOBJGroup group : this.current.groups.values())
        {
            BOBJBone bone = armature.bones.get(group.name);

            if (bone != null)
            {
                group.apply(bone, this.getTick(partialTick));
            }
        }
    }

    public void applyInactive(BOBJArmature armature, float partialTick, float x)
    {
        if (this.current == null)
        {
            return;
        }

        for (BOBJGroup group : this.current.groups.values())
        {
            BOBJBone bone = armature.bones.get(group.name);

            if (bone != null)
            {
                group.applyInterpolate(bone, this.ticks, x);
            }
        }
    }
}