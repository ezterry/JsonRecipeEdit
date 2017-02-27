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
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

/**
 *
 * Virtual Command Block Sender
 * Sends the commands for the Virtual Command Block Chains
 *
 * Created by ezterry on 2/18/17.
 */
public class VCommandSender implements ICommandSender {
    private String name;
    private boolean logOn;
    private World world;
    private boolean lastSuccess;

    VCommandSender(World w){
        name = "VCommandChain";
        logOn = false;
        world = w;
        lastSuccess = false;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String getName() {
        return name;
    }

    @SuppressWarnings("WeakerAccess")
    public void setName(String n) {
        name = n;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public ITextComponent getDisplayName()
    {
        return new TextComponentString(this.getName());
    }

    @SuppressWarnings("WeakerAccess")
    public void setLog(boolean v){
        logOn = v;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void sendMessage(ITextComponent component) {
        if(logOn){
            JSONRecipeEdit.log(Level.INFO,String.format("message [%s]: %s",
                    getName(),component.getUnformattedText()));
        }
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean canUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public BlockPos getPosition() {
        return world.getSpawnPoint();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Vec3d getPositionVector()
    {
        BlockPos pos = getPosition();
        return new Vec3d((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public World getEntityWorld() {
        return world;
    }

    @Nullable
    @Override
    public Entity getCommandSenderEntity() {
        return null;
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean popSuccess(){
        boolean tmp = lastSuccess;
        lastSuccess=false;
        return tmp;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void setCommandStat(CommandResultStats.Type type, int amount) {
        if(type == CommandResultStats.Type.SUCCESS_COUNT && amount != 0){
            lastSuccess = true;
        }
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return world.getMinecraftServer();
    }
}
