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

package com.ezrol.terry.minecraft.jsonRecipeEdit.commands;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * make a registry of "Anvil" crafts
 * This lets you add crafting to the Anvil (item + item = result)
 *
 * Created by ezterry on 2/26/17.
 */
public class AnvilCraft extends GenericCommand {
    @SuppressWarnings("WeakerAccess")
    public static class CraftEntry{
        public final ItemStack originalItem;
        public final ItemStack modifier;
        public final ItemStack result;
        public final int levels;

        private CraftEntry(ItemStack orig,ItemStack modifier,ItemStack result,int lvls){
            this.originalItem=orig;
            this.modifier=modifier;
            this.result=result;
            this.levels=lvls;
        }
    }

    private static HashMap<Item,LinkedList<CraftEntry>> craftingList = new HashMap<>();
    private boolean registered=false;

    public static List<CraftEntry> getEntries(){
        LinkedList<CraftEntry> r = new LinkedList<>();

        for(LinkedList<CraftEntry> e : craftingList.values()){
            r.addAll(e);
        }
        return(r);
    }

    @Override
    public String getCommandName() {
        return "anvil craft recipe";
    }

    @Override
    public void runCommand(JsonObject command) {
        if(command.has("original") &&
                command.has("modifier") &&
                command.has("result") &&
                command.has("levels")){
            //we have the required parameters
            ItemStack o = getItemFromArray(command.get("original").getAsJsonArray(),1);
            ItemStack m = getItemFromArray(command.get("modifier").getAsJsonArray(),1);
            ItemStack r = getItemFromArray(command.get("result").getAsJsonArray(),1);

            if(o == null){
                error("Unable to find original itme");
                throw(new RuntimeException("Missing Item"));
            }
            if(m == null){
                error("Unable to find original itme");
                throw(new RuntimeException("Missing Item"));
            }
            if(r == null){
                error("Unable to find original itme");
                throw(new RuntimeException("Missing Item"));
            }

            if(command.has("modifiercount")){
                m.setCount(command.get("modifiercount").getAsInt());
            }
            if(command.has("count")){
                r.setCount(command.get("count").getAsInt());
            }

            int levels = command.get("levels").getAsInt();
            CraftEntry entry = new CraftEntry(o,m,r,levels);

            if(!craftingList.containsKey(o.getItem())) {
                craftingList.put(o.getItem(),new LinkedList<CraftEntry>());
            }
            craftingList.get(o.getItem()).addLast(entry);
            info(String.format("Added Anvil Craft for: %s",r.toString()));
            if(!registered){
                registered=true;
                MinecraftForge.EVENT_BUS.register(this);
            }
        }
        else{
            error(String.format("Missing required field, must have original, modifier, result, and levels: %s",command.toString()));
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void anvil(AnvilUpdateEvent event){
        ItemStack left = event.getLeft();

        if(!craftingList.containsKey(left.getItem())){
            return;
        }
        LinkedList<CraftEntry> lst = craftingList.get(left.getItem());
        LinkedList<CraftEntry> possibleOutputs = new LinkedList<>();
        CraftEntry result=null;
        //validate left


        for(CraftEntry e : lst){
            if(e.originalItem.isItemEqual(left) && e.originalItem.getCount() == left.getCount()){
                if(e.originalItem.hasTagCompound()){
                    if(!left.hasTagCompound()){
                        continue;
                    }
                    NBTTagCompound c = e.originalItem.getTagCompound();
                    boolean valid=true;
                    if(c==null){
                        continue;
                    }
                    for(String k : c.getKeySet()){
                        if(left.getTagCompound() == null){
                            valid=false;
                            break;
                        }
                        if(!left.getTagCompound().hasKey(k)){
                            valid=false;
                            break;
                        }
                        if(!left.getTagCompound().getTag(k).equals(c.getTag(k))){
                            valid=false;
                            break;
                        }
                    }
                    if(valid){
                        possibleOutputs.add(e);
                    }
                }
                else{
                    possibleOutputs.add(e);
                }
            }
        }

        if(possibleOutputs.size()==0){
            return;
        }

        ItemStack right = event.getRight();
        //validate right
        for(CraftEntry e : possibleOutputs){
            if(e.modifier.isItemEqual(right) && e.modifier.getCount() <= right.getCount()){
                if(e.modifier.hasTagCompound()){
                    if(!right.hasTagCompound()){
                        continue;
                    }
                    NBTTagCompound c = e.modifier.getTagCompound();
                    boolean valid=true;
                    if(c==null){
                        continue;
                    }
                    for(String k : c.getKeySet()){
                        if(right.getTagCompound() == null){
                            valid=false;
                            break;
                        }
                        if(!right.getTagCompound().hasKey(k)){
                            valid=false;
                            break;
                        }
                        if(!right.getTagCompound().getTag(k).equals(c.getTag(k))){
                            valid=false;
                            break;
                        }
                    }
                    if(valid){
                        result=e;
                        break;
                    }
                }
                else{
                    result=e;
                    break;
                }
            }
        }

        if(result == null){
            return;
        }
        //if we are here right and left check out
        event.setMaterialCost(result.modifier.getCount());
        event.setCost(result.levels);
        event.setOutput(result.result.copy());
        if(event.getName() != null && !event.getName().equals("")) {
            event.getOutput().setStackDisplayName(event.getName());
        }
    }
}
