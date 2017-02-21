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
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;

/**
 * Add an item as fuel
 *
 * Created by ezterry on 2/21/17.
 */
public class AddFuel extends GenericCommand implements IFuelHandler {
    private final HashMap<ItemStack,Integer> burnables;

    public AddFuel(){
        burnables = new HashMap<>();
        GameRegistry.registerFuelHandler(this);
    }

    @Override
    public String getCommandName() {
        return("add fuel item");
    }

    @Override
    public void runCommand(JsonObject command) {
        ItemStack itm;
        int burnTime;
        if(command.has("item") && command.get("item").isJsonArray()){
            itm = getItemFromArray(command.get("item").getAsJsonArray(),1);
        }
        else{
            error(String.format("did not find \'item\' in command: %s",command.toString()));
            return;
        }
        if(command.has("burntime") && command.get("burntime").isJsonPrimitive()){
            burnTime = command.get("burntime").getAsJsonPrimitive().getAsInt();
        }
        else{
            error(String.format("did not find \'burntime\' in command: %s",command.toString()));
            return;
        }
        if(itm == null){
            error(String.format("unable to read item: %s",command.toString()));
            return;
        }
        info(String.format("Making item %s fuel time: %d",itm.toString(),burnTime));
        burnables.put(itm,burnTime);

    }

    @Override
    public int getBurnTime(ItemStack fuel) {
        for(ItemStack itm : burnables.keySet()){
            if(itm.getMetadata()== OreDictionary.WILDCARD_VALUE){
                if(fuel.getItem() == fuel.getItem()){
                    return(burnables.get(itm));
                }
            }
            else if(itm.isItemEqual(fuel)){
                return(burnables.get(itm));
            }
        }
        return 0;
    }
}
