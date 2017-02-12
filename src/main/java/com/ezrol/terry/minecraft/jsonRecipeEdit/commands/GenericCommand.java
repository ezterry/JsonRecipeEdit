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
import net.minecraft.util.ResourceLocation;
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
            return(new ItemStack(item, amount, meta));
        }
        catch(Exception e){
            error(String.format("Unable to read in item from json: %s",e.toString()));
            return null;
        }
    }
}
