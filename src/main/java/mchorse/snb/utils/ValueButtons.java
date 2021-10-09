package mchorse.snb.utils;

import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.ValueGUI;
import mchorse.snb.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ValueButtons extends ValueGUI
{
    public ValueButtons(String id)
    {
        super(id);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft mc, GuiConfigPanel panel)
    {
        GuiButtonElement button = new GuiButtonElement(mc, IKey.str("Open models folder..."), (b) ->
        {
            GuiUtils.openWebLink(ClientProxy.pack.config.toURI());
        });

        return Arrays.asList(button);
    }
}