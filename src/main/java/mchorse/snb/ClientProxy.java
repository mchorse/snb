package mchorse.snb;

import mchorse.mclib.utils.files.GlobalTree;
import mchorse.snb.api.animation.AnimationPack;
import mchorse.snb.client.EntityModelHandler;
import mchorse.snb.utils.SnBTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /**
     * Animation pack  
     */
    public static AnimationPack pack;

    /**
     * Model render handler 
     */
    public static EntityModelHandler modelHandler;

    /**
     * Client folder where saved selectors and animations are getting
     * stored. 
     */
    public static File clientFolder;

    /**
     * Get client storage location
     * 
     * This method returns File pointer to a folder where client side 
     * data is stored. This method will return {@code null} if it was 
     * invoked when the game is in the main menu.
     */
    public static File getClientFolder()
    {
        Minecraft mc = Minecraft.getMinecraft();
        ServerData data = mc.getCurrentServerData();

        File file = null;

        if (data != null)
        {
            /* Removing port, because this will distort the folder name */
            file = new File(clientFolder, data.serverIP.replaceAll(":[\\w]{1,5}$", "").replaceAll("[^\\w\\d_\\- ]", "_"));
        }
        else if (mc.isSingleplayer())
        {
            /* I probably should've used getFolderName() in the beginning ... */
            file = new File(clientFolder, mc.getIntegratedServer().getWorldName().replaceAll("[^\\w\\d_\\- ]", "_"));
        }

        if (file != null)
        {
            file.mkdirs();
        }

        return file;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        this.injectResourcePack(event.getModConfigurationDirectory().getAbsolutePath());
    }

    /**
     * Inject actors skin pack into FML's resource packs list
     *
     * It's done by accessing private FMLClientHandler list (via reflection) and
     * appending actor pack.
     *
     * Thanks to diesieben07 for giving the idea.
     */
    @SuppressWarnings("unchecked")
    private void injectResourcePack(String path)
    {
        clientFolder = new File(path, "snb/client/");

        try
        {
            Field field = FMLClientHandler.class.getDeclaredField("resourcePackList");
            field.setAccessible(true);

            List<IResourcePack> packs = (List<IResourcePack>) field.get(FMLClientHandler.instance());
            packs.add(pack = new AnimationPack(new File(path, "snb/models/")));

            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();

            if (manager instanceof SimpleReloadableResourceManager)
            {
                ((SimpleReloadableResourceManager) manager).reloadResourcePack(pack);
            }

            GlobalTree.TREE.register(new SnBTree(pack.config));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);

        /* Register event handlers */
        MinecraftForge.EVENT_BUS.register(modelHandler = new EntityModelHandler());
    }
}