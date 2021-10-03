package mchorse.snb.events;

import mchorse.snb.api.animation.AnimationManager;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RefreshAnimationsEvent extends Event
{
    public AnimationManager manager;

    public RefreshAnimationsEvent(AnimationManager manager)
    {
        this.manager = manager;
    }
}