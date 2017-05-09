/*
 * Copyright (c) 2017, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.ezrol.terry.minecraft.jsonRecipeEdit.virtualcommandblock;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.ezrol.terry.minecraft.jsonRecipeEdit.tools.DataDumps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

/**
 * The jsonRecipeEdit command
 * allows the user to:
 *
 * * set/clear/test trigger tags
 * * run command chain groups
 *
 * Created by ezterry on 2/19/17.
 */
public class JsonRecipeEditCommand extends CommandBase {
    @SuppressWarnings("NullableProblems")
    @Override
    public String getName() {
        return "jsonRecipeEdit";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getUsage(ICommandSender sender) {
        return "jsonRecipeEdit <set|clear|test|run|list|runone|runin|datadump> <parameters>";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World w = sender.getEntityWorld();

        if(args.length < 1){
            throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
        }
        switch (args[0]) {
            case "run":
                if(args.length != 2){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
                }
                if (sender.sendCommandFeedback()) {
                    JSONRecipeEdit.log(Level.INFO, String.format("attempting to run \"%s\" command chains by %s", args[1], sender.getName()));
                }
                JSONRecipeEdit.commandChains.runTrigger(String.format("Run: %s", args[1]), w);
                break;
            case "set":
                if(args.length != 2){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
                }
                if (JSONRecipeEdit.commandChains.getTags().setTag(args[1], w)) {
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.onsetsuccess", args[1]);
                } else {
                    throw new CommandException("commands.jsonrecipeedit.onsetfail");
                }
                break;
            case "clear":
                if(args.length != 2){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
                }
                if (JSONRecipeEdit.commandChains.getTags().clearTag(args[1], w)) {
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.onclearsuccess", args[1]);
                } else {
                    throw new CommandException("commands.jsonrecipeedit.onclearfail");
                }
                break;
            case "test":
                if(args.length != 2){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
                }
                if (JSONRecipeEdit.commandChains.getTags().checkTag(args[1])) {
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.testtagset");
                } else {
                    throw new CommandException("commands.jsonrecipeedit.testtagclear");
                }
                break;
            case "list":
                //list all tags
                notifyCommandListener(sender,this,"commands.jsonrecipeedit.taglist",
                        JSONRecipeEdit.commandChains.getTags().getList().toString());
                break;
            case "runone":
                //run one of the listed commands
                if(args.length > 1){
                    String cmd = args[server.getEntityWorld().rand.nextInt(args.length-1)+1];
                    if (sender.sendCommandFeedback()) {
                        JSONRecipeEdit.log(Level.INFO, String.format("attempting to run randomly selected \"%s\" command chains by %s", cmd, sender.getName()));
                    }
                    JSONRecipeEdit.commandChains.runTrigger(String.format("Run: %s", cmd), w);
                }
                else{
                    throw new CommandException("commands.jsonrecipeedit.nocommandprovided");
                }
                break;
            case "runin":
                if(args.length != 3){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat2", getUsage(sender));
                }
                //run the command (with)in n blocks of the current position
                int range = parseInt(args[1]);
                BlockPos p = sender.getPosition();
                if(range > 0) {
                    p=p.add(
                            server.getEntityWorld().rand.nextInt(range * 2) - range,
                            server.getEntityWorld().rand.nextInt(range * 2) - range,
                            server.getEntityWorld().rand.nextInt(range * 2) - range
                    );
                }
                if (sender.sendCommandFeedback()) {
                    JSONRecipeEdit.log(Level.INFO, String.format("attempting to run \"%s\" command chains by %s", args[1], sender.getName()));
                    JSONRecipeEdit.log(Level.INFO, String.format("(chain being run at cords: %d,%d,%d)",p.getX(),p.getY(),p.getZ()));
                }
                JSONRecipeEdit.commandChains.runTrigger(String.format("Run: %s", args[2]), w, p);
                break;
            case "datadump":
                if(args.length != 2){
                    throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
                }
                if(DataDumps.runDataDump(args[1])){
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.dumpsuccess");
                }
                else{
                    throw new CommandException("commands.jsonrecipeedit.dumpfail");
                }
                break;
            default:
                throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
        }
    }
}
