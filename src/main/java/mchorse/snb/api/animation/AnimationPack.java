package mchorse.snb.api.animation;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Animation pack
 * 
 * This class is responsible for providing access to the animation 
 * resources on the client's side.
 */
@SideOnly(Side.CLIENT)
public class AnimationPack implements IResourcePack
{
    public static Set<String> DOMAINS = ImmutableSet.<String>of("s&b");

    public File config;

    public AnimationPack(File config)
    {
        this.config = config;
        this.config.mkdirs();
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        return new FileInputStream(new File(this.config, location.getResourcePath()));
    }

    @Override
    public boolean resourceExists(ResourceLocation location)
    {
        return new File(this.config, location.getResourcePath()).exists();
    }

    @Override
    public Set<String> getResourceDomains()
    {
        return DOMAINS;
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException
    {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException
    {
        return null;
    }

    @Override
    public String getPackName()
    {
        return "Skin&Bones animation pack";
    }
}