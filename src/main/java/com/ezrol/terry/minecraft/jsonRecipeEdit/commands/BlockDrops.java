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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Modify when drops when a block is broken
 *
 * Created by ezterry on 2/19/17.
 */
public class BlockDrops extends GenericCommand{
    private boolean registered;
    private HashMap<IBlockState,newDrops> substitutions;

    class newDrops{
        boolean dropOriginal;
        boolean dropOne;
        boolean silktouchDropOne;
        float fortuneMultiplier;
        float dropChance;
        Map<ItemStack,Float> stdItems;
        Map<ItemStack,Float> silktouchItems;
        float stdWeight;
        float silktouchWeight;
        int exp;
    }

    public BlockDrops(){
        registered=false;
        substitutions=new HashMap<>();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerBreaksBlock(BlockEvent.BreakEvent event){
        IBlockState block = event.getState();
        if(substitutions.containsKey(block)){
            boolean isSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,
                    event.getPlayer().getHeldItemMainhand()) > 0;
            newDrops drop = substitutions.get(block);

            if(drop.exp >= 0 && !isSilkTouch){
                event.setExpToDrop(drop.exp);
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerBreaksBlock(BlockEvent.HarvestDropsEvent event){
        IBlockState block = event.getState();
        ItemStack itm;

        if(substitutions.containsKey(block)){
            List<ItemStack> drops=event.getDrops();
            newDrops operation = substitutions.get(block);

            if(!operation.dropOriginal){
                drops.clear();
            }
            if(operation.dropChance>0){
                event.setDropChance(operation.dropChance);
            }
            if(event.isSilkTouching() ? operation.silktouchDropOne : operation.dropOne){
                //we just push one of the provided stacks
                itm=selectOne(operation,event.getWorld().rand, event.isSilkTouching());
                if(itm != null){
                    drops.add(itm.copy());
                }
            }
            else{
                //based on the odds add the provided stacks
                Map<ItemStack,Float> itms;

                if(event.isSilkTouching() && operation.silktouchItems != null){
                    itms = operation.silktouchItems;
                }
                else{
                    itms = operation.stdItems;
                }

                for(Map.Entry<ItemStack,Float> e : itms.entrySet()) {
                    float weight = e.getValue();
                    int i;

                    for(i=0;i<event.getFortuneLevel();i++){
                        weight *= operation.fortuneMultiplier;
                    }
                    float flr = (float)Math.floor(weight);
                    weight = weight - flr;
                    int count = (int)flr;
                    float check = event.getWorld().rand.nextFloat();

                    if(check < weight){
                        count++;
                    }

                    for(i=0;i<count;i++){
                        drops.add(e.getKey().copy());
                    }
                }
            }
        }
    }

    private ItemStack selectOne(newDrops d,Random r,boolean isSilkTouch){
        float v;
        Map<ItemStack,Float> itms;

        if(isSilkTouch && d.silktouchItems != null){
            v = r.nextFloat() * d.silktouchWeight;
            itms = d.silktouchItems;
        }
        else{
            v = r.nextFloat() * d.stdWeight;
            itms = d.stdItems;
        }

        ItemStack stack =null;

        for(Map.Entry<ItemStack,Float> e : itms.entrySet()){
            v-=e.getValue();
            stack = e.getKey();
            if(v<=0f){
                break;
            }
        }
        return(stack);
    }

    @Override
    public String getCommandName() {
        return "block drops";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void runCommand(JsonObject command) {
        newDrops data = new newDrops();
        data.stdItems =new HashMap<>();

        IBlockState block;

        //dropOriginal default=true
        data.dropOriginal = !command.has("droporiginal") ||
                command.get("droporiginal").getAsBoolean();

        //dropOne default=false
        data.dropOne = command.has("dropone") && command.get("dropone").getAsBoolean();
        data.silktouchDropOne = command.has("silkdropone") ?
                command.get("dropone").getAsBoolean() :
                data.dropOne;

        //fortune multipier default=1.10
        if (command.has("fortunemultiplier")) {
            data.fortuneMultiplier = command.get("fortunemultiplier").getAsFloat();
        } else {
            data.fortuneMultiplier = 1.10f;
        }

        //dropchance default=-1.0
        if (command.has("dropchance")) {
            data.dropChance = command.get("dropchance").getAsFloat();
        } else {
            data.dropChance = -1.0f;
        }

        //drop exp default -1 (don't change XP drop)
        if(command.has("exp")){
            data.exp = command.get("exp").getAsInt();
        } else {
            data.exp = -1;
        }

        data.stdWeight = 0;
        if(!command.has("drops") || !command.get("drops").isJsonArray()){
            error(String.format("command must contain a 'drops' array: %s",command.toString()));
            return;
        }
        for(JsonElement e : command.get("drops").getAsJsonArray()){
            if(!e.isJsonObject()){
                error(String.format("expected json object at: %s",e.toString()));
                return;
            }
            ItemStack itm;
            int count=1;
            Float weight;

            if(((JsonObject)e).has("count")){
                count = ((JsonObject)e).get("count").getAsInt();
            }
            if(((JsonObject)e).get("item").isJsonArray()){
                itm=getItemFromArray(((JsonObject)e).get("item").getAsJsonArray(),count);
            }
            else {
                error(String.format("missing stdItems array: %s",e.toString()));
                return;
            }
            if(((JsonObject)e).has("weight")){
                weight = ((JsonObject)e).get("weight").getAsFloat();
            }
            else{
                error(String.format("missing weight: %s",e.toString()));
                return;
            }
            data.stdItems.put(itm,weight);
            data.stdWeight += weight;
        }

        data.silktouchWeight = 0;
        if(command.has("silktouch")){
            data.silktouchItems = new HashMap<>();
            for(JsonElement e : command.get("silktouch").getAsJsonArray()){
                if(!e.isJsonObject()){
                    error(String.format("expected json object at: %s",e.toString()));
                    return;
                }
                ItemStack itm;
                int count=1;
                Float weight;

                if(((JsonObject)e).has("count")){
                    count = ((JsonObject)e).get("count").getAsInt();
                }
                if(((JsonObject)e).get("item").isJsonArray()){
                    itm=getItemFromArray(((JsonObject)e).get("item").getAsJsonArray(),count);
                }
                else {
                    error(String.format("missing stdItems array: %s",e.toString()));
                    return;
                }
                if(((JsonObject)e).has("weight")){
                    weight = ((JsonObject)e).get("weight").getAsFloat();
                }
                else{
                    error(String.format("missing weight: %s",e.toString()));
                    return;
                }
                data.silktouchItems.put(itm,weight);
                data.silktouchWeight += weight;
            }

        }
        else{
            data.silktouchItems = null;
        }

        String blockName = command.get("block").getAsString();
        String meta="";
        if(command.has("meta")){
            meta = command.get("meta").getAsString();
        }
        ResourceLocation key = new ResourceLocation(blockName);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) {
            error(String.format("Cant find block: %s",blockName));
            return;
        }

        if(!meta.equals("")){
            try {
                block = CommandSetBlock.convertArgToBlockState(ForgeRegistries.BLOCKS.getValue(key),meta);
            } catch (NumberInvalidException e1) {
                throw(new RuntimeException("unable to get block state (invalid number)"));
            } catch (InvalidBlockStateException e1) {
                throw(new RuntimeException("unable to get block state"));
            }
        }
        else{
            block = ForgeRegistries.BLOCKS.getValue(key).getDefaultState();
        }
        substitutions.put(block, data);
        info(String.format("Added drops to: %s",block.toString()));
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
    }
}
