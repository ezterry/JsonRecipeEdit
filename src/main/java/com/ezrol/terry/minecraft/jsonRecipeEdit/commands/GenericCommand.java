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

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.ezrol.terry.minecraft.jsonRecipeEdit.api.IRECommand;
import com.google.gson.JsonArray;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

/**
 * A shell to the built in commands to add some logging shortcuts
 *
 * Created by ezterry on 2/9/17.
 */
@SuppressWarnings("WeakerAccess")
public abstract class GenericCommand implements IRECommand{
    protected final void log(Level lvl,String msg){
        JSONRecipeEdit.log(lvl,String.format("(%s) %s",this.getCommandName(),msg));
    }
    protected final void error(String msg){
        log(Level.ERROR,msg);
    }
    protected final void info(String msg){
        log(Level.INFO,msg);
    }

    /**
     * Gets an ItemStack from an Json array of 1 to 2 in length
     * param 1: item "modid:name"
     * param 2: meta a number or "*" for a wildcard (default 0 if omitted)
     * param 3: nbt a string of the nbt data (default "" if omitted)
     *
     * @param input Json Array to process
     * @param amount number of items in the stack
     * @return ItemStack representing the input
     */
    @Nullable
    protected ItemStack getItemFromArray(JsonArray input, int amount){
        if(input.size() == 0){
            error("null array expecting [<item name>] or [<item name>,<meta>]");
            return(null);
        }
        int meta = 0;

        try{
            ResourceLocation itemres;
            itemres = new ResourceLocation(input.get(0).getAsString());

            if(input.size()>=2){
                if(input.get(1).getAsJsonPrimitive().isString() &&
                        input.get(1).getAsJsonPrimitive().getAsString().equals("*")){
                    meta = OreDictionary.WILDCARD_VALUE;
                }
                else {
                    meta = input.get(1).getAsInt();
                }
            }
            Item item = ForgeRegistries.ITEMS.getValue(itemres);
            if(item == null){
                return null;
            }
            ItemStack stack = new ItemStack(item, amount, meta);
            if(input.size()>=3){
                try
                {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(input.get(2).getAsString()));
                }
                catch (NBTException nbtexception)
                {
                    error(String.format("unable to parse NBT: %s",nbtexception));
                    error(String.format("String ignoring NBT data for command: %s",input.toString()));
                }
            }
            return(stack);
        }
        catch(Exception e){
            error(String.format("Unable to read in item from json: %s",e.toString()));
            return null;
        }
    }

    /**
     * Get a fluid stack from a Json array of length 2
     * [fluid,quantity]
     * Such as ["water",1000] for 1 bucket of water
     *
     * If no amount is provided 1000mb is assumed
     *
     * @param input the json array to create a FluidStack from
     */
    @Nullable
    protected FluidStack getFluidFromArray(JsonArray input){
        if(input.size() == 0){
            error("null array expecting [<fluid>] or [<fluid>,<amount>]");
            return(null);
        }
        try{
            String name = input.get(0).getAsString();
            int quant = 1000;

            if(input.size() > 1){
                quant = input.get(1).getAsInt();
            }

            return(FluidRegistry.getFluidStack(name,quant));
        }
        catch(Exception e){
            error(String.format("Unable to read in fluid from json: %s",e.toString()));
            return null;
        }
    }
}
