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
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
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
        return "jsonRecipeEdit <set|clear|test|run|list> <name of tag/run>";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2 && (args.length != 1 || !args[0].equals("list"))) {
            throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
        }
        World w = sender.getEntityWorld();
        switch (args[0]) {
            case "run":
                if (sender.sendCommandFeedback()) {
                    JSONRecipeEdit.log(Level.INFO, String.format("attempting to run \"%s\" command chains by %s", args[1], sender.getName()));
                }
                JSONRecipeEdit.commandChains.runTrigger(String.format("Run: %s", args[1]), w);
                break;
            case "set":
                if (JSONRecipeEdit.commandChains.getTags().setTag(args[1], w)) {
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.onsetsuccess", args[1]);
                } else {
                    throw new CommandException("commands.jsonrecipeedit.onsetfail");
                }
                break;
            case "clear":
                if (JSONRecipeEdit.commandChains.getTags().clearTag(args[1], w)) {
                    notifyCommandListener(sender, this, "commands.jsonrecipeedit.onclearsuccess", args[1]);
                } else {
                    throw new CommandException("commands.jsonrecipeedit.onclearfail");
                }
                break;
            case "test":
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
            default:
                throw new WrongUsageException("commands.jsonrecipeedit.badformat", getUsage(sender));
        }
    }
}
