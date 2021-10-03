package mchorse.snb.client;

import mchorse.mclib.utils.ReflectionUtils;
import mchorse.snb.ClientProxy;
import mchorse.snb.api.animation.Animation;
import mchorse.snb.api.animation.AnimationManager;
import mchorse.snb.api.animation.AnimationManager.AnimationEntry;
import mchorse.snb.api.animation.AnimationMesh;
import mchorse.snb.api.animation.AnimationPack;
import mchorse.snb.api.animation.model.AnimatorConfig;
import mchorse.snb.api.animation.model.AnimatorConfig.AnimatorConfigEntry;
import mchorse.snb.api.bobj.BOBJLoader;
import mchorse.snb.api.bobj.BOBJLoader.BOBJData;
import mchorse.snb.events.RefreshAnimationsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Entity model handler. This handler is responsible for rendering 
 * models on player.
 */
@SideOnly(Side.CLIENT)
public class EntityModelHandler
{
    @SubscribeEvent
    public void onClientJoin(ClientConnectedToServerEvent event)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            try
            {
                this.refreshAnimations();
            }
            catch (Exception e)
            {
                System.err.println("Skin&Bones: Failed to reload animations upon joining the world!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Refresh animation
     */
    public void refreshAnimations()
    {
        Set<String> names = new HashSet<String>();

        /* (Re)load animations */
        for (File file : ClientProxy.pack.config.listFiles())
        {
            if (file.isDirectory())
            {
                File model = new File(file, "model.bobj");

                if (model.exists())
                {
                    String name = file.getName();

                    try
                    {
                        AnimationEntry entry = AnimationManager.INSTANCE.animations.get(name);
                        BOBJData data = null;

                        long modified = model.lastModified();

                        if (entry != null && entry.lastModified < modified)
                        {
                            data = BOBJLoader.readData(new FileInputStream(model));
                            entry.reloadAnimation(data, modified);
                        }
                        else if (entry == null)
                        {
                            data = BOBJLoader.readData(new FileInputStream(model));
                            Animation animation = new Animation(name, data);

                            AnimationManager.INSTANCE.animations.put(name, entry = new AnimationEntry(animation, file, modified));

                            animation.init();
                            this.createTextureFolders(animation);
                        }

                        names.add(name);
                    }
                    catch (Exception e)
                    {
                        System.err.println("An error occurred during refreshing animation procedure for animation named '" + name + "'!");
                        e.printStackTrace();
                    }
                }
            }
        }

        /* Remove all animations which were deleted on the disk */
        Iterator<Map.Entry<String, AnimationEntry>> it = AnimationManager.INSTANCE.animations.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, AnimationEntry> entry = it.next();

            if (!names.contains(entry.getKey()))
            {
                it.remove();
                entry.getValue().animation.delete();
            }
        }

        MinecraftForge.EVENT_BUS.post(new RefreshAnimationsEvent(AnimationManager.INSTANCE));
    }

    /**
     * Create texture folders. It's for easier use, so users knew the 
     * where to put textures.
     */
    private void createTextureFolders(Animation animation)
    {
        for (AnimationMesh mesh : animation.meshes)
        {
            new File(ClientProxy.pack.config, animation.name + "/textures/" + mesh.name).mkdirs();
        }
    }

    /**
     * Refresh textures related to Skin&Bones animations
     */
    public void refreshAnimationSkins()
    {
        Map<ResourceLocation, ITextureObject> textureMap = ReflectionUtils.getTextures(Minecraft.getMinecraft().renderEngine);
        Iterator<Map.Entry<ResourceLocation, ITextureObject>> it = textureMap.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<ResourceLocation, ITextureObject> entry = it.next();
            ResourceLocation key = entry.getKey();

            if (AnimationPack.DOMAINS.contains(key.getResourceDomain()))
            {
                TextureUtil.deleteTexture(entry.getValue().getGlTextureId());
                it.remove();
            }
        }
    }

    /**
     * On refresh animations handler. This handler is responsible for 
     * reloading morph configs whenever animations get updated. 
     */
    @SubscribeEvent
    public void onRefreshAnimations(RefreshAnimationsEvent event)
    {
        for (Map.Entry<String, AnimationEntry> entry : event.manager.animations.entrySet())
        {
            String name = entry.getKey();
            AnimationEntry anim = entry.getValue();

            File file = new File(anim.directory, "model.json");
            AnimatorConfigEntry config = AnimationManager.INSTANCE.configs.get(name);

            /* This makes sure that animated morphs are get updated even 
             * if the config hasn't changed but the animation has 
             * changed */
            long modified = Math.max(file.lastModified(), anim.lastModified);
            boolean needsUpdate = config != null && modified <= config.lastModified;

            if (!file.exists() || needsUpdate)
            {
                continue;
            }

            try
            {
                String json = FileUtils.readFileToString(file, Charset.defaultCharset());

                if (json.isEmpty())
                {
                    continue;
                }

                AnimatorConfig morph = AnimationManager.INSTANCE.gson.fromJson(json, AnimatorConfig.class);

                if (config == null)
                {
                    AnimationManager.INSTANCE.configs.put(name, new AnimatorConfigEntry(morph, modified));
                }
                else
                {
                    config.config.copy(morph);
                    config.lastModified = modified;
                }
            }
            catch (Exception e)
            {
                System.err.println("An error occurred during reloading of animated morph configs for animation '" + name + "'!");
                e.printStackTrace();
            }
        }

        /* Remove irrelevant models */
        Iterator<String> it = AnimationManager.INSTANCE.configs.keySet().iterator();
        Set<String> keys = event.manager.animations.keySet();

        while (it.hasNext())
        {
            String key = it.next();

            if (!keys.contains(key))
            {
                it.remove();
            }
        }
    }
}