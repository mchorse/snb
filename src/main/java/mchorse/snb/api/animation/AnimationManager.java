package mchorse.snb.api.animation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mchorse.snb.api.animation.json.ActionConfigAdapter;
import mchorse.snb.api.animation.json.AnimationMeshConfigAdapter;
import mchorse.snb.api.animation.json.AnimatorActionsConfigAdapter;
import mchorse.snb.api.animation.json.AnimatorConfigAdapter;
import mchorse.snb.api.animation.json.AnimatorHeldItemConfigAdapter;
import mchorse.snb.api.animation.model.ActionConfig;
import mchorse.snb.api.animation.model.AnimatorActionsConfig;
import mchorse.snb.api.animation.model.AnimatorConfig;
import mchorse.snb.api.animation.model.AnimatorConfig.AnimatorConfigEntry;
import mchorse.snb.api.animation.model.AnimatorHeldItemConfig;
import mchorse.snb.api.bobj.BOBJLoader.BOBJData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation manager class
 * 
 * This class is responsible for managing 
 */
@SideOnly(Side.CLIENT)
public class AnimationManager
{
    /**
     * Cached animations 
     */
    public Map<String, AnimationEntry> animations = new HashMap<String, AnimationEntry>();

    /**
     * Morph configurations
     */
    public Map<String, AnimatorConfigEntry> configs = new HashMap<String, AnimatorConfigEntry>();

    /**
     * Default config 
     */
    public AnimatorConfigEntry defaultConfig;

    /**
     * GSON that is used for reading {@link AnimatorConfig} from JSON. 
     */
    public Gson gson;

    /**
     * Static instance of the animation manager, please don't modify  
     */
    public static final AnimationManager INSTANCE = new AnimationManager();

    private AnimationManager()
    {
        /* Default config */
        this.defaultConfig = new AnimatorConfigEntry(new AnimatorConfig(), 0);
        this.defaultConfig.config.rightHands.put("right_hand", new AnimatorHeldItemConfig("right_hand"));
        this.defaultConfig.config.leftHands.put("left_hand", new AnimatorHeldItemConfig("left_hand"));

        /* Create GSON parser */
        GsonBuilder gson = new GsonBuilder();

        gson.registerTypeAdapter(AnimationMeshConfig.class, new AnimationMeshConfigAdapter());
        gson.registerTypeAdapter(AnimatorConfig.class, new AnimatorConfigAdapter());
        gson.registerTypeAdapter(AnimatorActionsConfig.class, new AnimatorActionsConfigAdapter());
        gson.registerTypeAdapter(AnimatorHeldItemConfig.class, new AnimatorHeldItemConfigAdapter());
        gson.registerTypeAdapter(ActionConfig.class, new ActionConfigAdapter());

        this.gson = gson.create();
    }

    /**
     * Get animation much easily 
     */
    public Animation getAnimation(String name)
    {
        AnimationEntry entry = this.animations.get(name);

        return entry == null ? null : entry.animation;
    }

    /**
     * Get config much easily 
     */
    public AnimatorConfigEntry getConfig(String name)
    {
        AnimatorConfigEntry entry = this.configs.get(name);

        return entry == null ? this.defaultConfig : entry;
    }

    /**
     * Animation entry class
     * 
     * This class is responsible for storing information about the 
     * animation, and 
     */
    @SideOnly(Side.CLIENT)
    public static class AnimationEntry
    {
        public Animation animation;
        public File directory;
        public long lastModified;

        public AnimationEntry(Animation animation, File directory, long lastModified)
        {
            this.animation = animation;
            this.directory = directory;
            this.lastModified = lastModified;
        }

        public void reloadAnimation(BOBJData data, long lastModified)
        {
            this.animation.reload(data);
            this.lastModified = lastModified;
        }
    }
}