package mchorse.snb.api.metamorph;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.events.ReloadMorphs;
import mchorse.snb.ClientProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Metamorph handler. Used solely to isolate registration of 
 * {@link AnimatedFactory} for optional Metamorph integration. 
 */
public class MetamorphHandler
{
    /**
     * Registers components of this optional integrations 
     */
    @Method(modid = "metamorph")
    public static void register()
    {
        AnimatedFactory factory = new AnimatedFactory();

        MorphManager.INSTANCE.factories.add(factory);
        MinecraftForge.EVENT_BUS.register(new MetamorphHandler());
    }

    @SubscribeEvent
    @Method(modid = "metamorph")
    @SideOnly(Side.CLIENT)
    public void onMorphsReload(ReloadMorphs event)
    {
        ClientProxy.modelHandler.refreshAnimations();
    }
}