package mchorse.snb;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SnB.MOD_ID, name = "Skin&Bones", version = SnB.VERSION, dependencies = "required-after:mclib@[%MCLIB%,);required-after:metamorph@[%METAMORPH%,)")
public final class SnB
{
    public static final String MOD_ID = "snb";
    public static final String VERSION = "%VERSION%";

    @SidedProxy(serverSide = "mchorse.snb.CommonProxy", clientSide = "mchorse.snb.ClientProxy")
    public static CommonProxy proxy;

    public static String config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = event.getModConfigurationDirectory().getAbsolutePath();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }
}