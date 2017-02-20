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
import net.minecraft.command.ICommandManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Simulates virtual command block chains
 *
 * Created by ezterry on 2/16/17.
 */
@SuppressWarnings({"WeakerAccess"})
public class VCommandLogic {
    private boolean worldLoaded;
    private ICommandManager cmdManager;
    HashMap<String,LinkedList<VCommandSet>> commandTable; //commands trigger -> list of command sets
    boolean loaded;
    private VCommandTagData tags;

    public VCommandLogic(){
        loaded=false;
        worldLoaded = false;
        commandTable = new HashMap<>();
        tags=new VCommandTagData();
        cmdManager=null;
    }

    public boolean addCommand(VCommandSet c){
        if(c.isValid()){
            if(!loaded){
                loaded = true;
                JSONRecipeEdit.log(Level.INFO,"Registering Tick Event for Virtual Command Chains");
                MinecraftForge.EVENT_BUS.register(this);
            }
            String trigger = c.getTrigger();
            if(!commandTable.containsKey(trigger)){
                commandTable.put(trigger,new LinkedList<VCommandSet>());
            }
            commandTable.get(trigger).addLast(c);
            return true;
        }
        return false;
    }


    public void runTrigger(String t,World w){
        if(cmdManager == null){
            //noinspection ConstantConditions
            cmdManager = w.getMinecraftServer().getCommandManager();
        }
        if(worldLoaded && commandTable.containsKey(t)) {
            int d = w.provider.getDimension();
            for (VCommandSet s : commandTable.get(t)) {
                s.evaluate(cmdManager,w,d);
            }
        }
    }

    public void initTags(World w){
        VCommandTagData data = (VCommandTagData)w.getPerWorldStorage().getOrLoadData(VCommandTagData.class, JSONRecipeEdit.MODID);
        if(data == null) {
            tags=new VCommandTagData();
            w.getPerWorldStorage().setData(JSONRecipeEdit.MODID, tags);
        }
        else
        {
            tags = data;
        }

    }

    public VCommandTagData getTags(){
        return tags;
    }

    private void runServerLoad(World overworld){
        JSONRecipeEdit.log(Level.INFO,"Trigger Server Load Virtual Command Chains");
        runTrigger("ServerStart",overworld);
    }

    public void runServerUnload() {
        this.worldLoaded = false;
        JSONRecipeEdit.log(Level.INFO,"World Unload");
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onServerWorldTick(TickEvent.WorldTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            return;
        }
        if(event.type != TickEvent.Type.WORLD){
            return;
        }
        int dim = event.world.provider.getDimension();

        //first run of overworld is the world load event
        if(!worldLoaded && dim == 0){
            this.worldLoaded = true;
            runServerLoad(event.world);
        }
        else{
            runTrigger("Always",event.world);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        World w = event.player.getEntityWorld();
        runTrigger("PlayerJoin",w);
    }
}
