package mchorse.snb;

import mchorse.mclib.McLib;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.events.RegisterConfigEvent;
import mchorse.snb.utils.ValueButtons;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = SnB.MOD_ID, name = "Skin&Bones", version = SnB.VERSION, dependencies = "required-after:mclib@[%MCLIB%,);required-after:metamorph@[%METAMORPH%,)", updateJSON = "https://raw.githubusercontent.com/mchorse/snb/main/version.json")
public final class SnB
{
    public static final String MOD_ID = "snb";
    public static final String VERSION = "%VERSION%";

    @SidedProxy(serverSide = "mchorse.snb.CommonProxy", clientSide = "mchorse.snb.ClientProxy")
    public static CommonProxy proxy;

    public static String config;

    @SubscribeEvent
    public void onConfigRegister(RegisterConfigEvent event)
    {
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        builder.category("general").register(new ValueButtons("buttons"));
        builder.getCategory().markClientSide();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = event.getModConfigurationDirectory().getAbsolutePath();
        proxy.preInit(event);

        McLib.EVENT_BUS.register(this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }
}