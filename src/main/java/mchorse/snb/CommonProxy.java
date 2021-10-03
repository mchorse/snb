package mchorse.snb;

import mchorse.snb.api.metamorph.MetamorphHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Common proxy 
 */
public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        if (Loader.isModLoaded("metamorph"))
        {
            MetamorphHandler.register();
        }
    }

    public void init(FMLInitializationEvent event)
    {}
}