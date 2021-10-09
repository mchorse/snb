package mchorse.snb.commands;

import mchorse.snb.ClientProxy;
import mchorse.snb.client.EntityModelHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class CommandSnB extends CommandBase
{
    @Override
    public String getName()
    {
        return "snb";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "snb.commands.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        EntityModelHandler models = ClientProxy.modelHandler;
        String action = args[0];

        if (action.equals("clear"))
        {
            models.refreshAnimationSkins();
        }
        else if (action.equals("reload"))
        {
            try
            {
                models.refreshAnimations();
            }
            catch (Exception e)
            {
                sender.sendMessage(new TextComponentString("An error occurred during animation reload procedure! Check the log for details."));
                e.printStackTrace();
            }
        }
        else
        {
            throw new CommandException("Given command wasn't recognized!");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "clear", "reload");
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}