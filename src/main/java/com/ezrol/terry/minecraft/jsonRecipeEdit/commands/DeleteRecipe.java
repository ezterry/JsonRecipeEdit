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
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.util.LinkedList;
import java.util.List;

/**
 * Command to delete a recipe
 *
 * Created by ezterry on 2/9/17.
 */
public class DeleteRecipe extends GenericCommand{
    @Override
    public String getCommandName() {
        return "delete recipe";
    }

    @Override
    public void runCommand(JsonObject command) {
        ResourceLocation itemname;
        int meta=0;
        ItemStack testitem;

        if(command.get("item").isJsonArray()){
            //new format use json array
            testitem=getItemFromArray(command.get("item").getAsJsonArray(),1);
        }
        else {
            log(Level.WARN,String.format("item in deprecated format, use new json array format: %s",command.toString()));
            itemname = new ResourceLocation(command.get("item").getAsString());
            if (command.has("meta")) {
                if (command.get("meta").getAsJsonPrimitive().isString() &&
                        command.get("meta").getAsJsonPrimitive().getAsString().equals("*")) {
                    meta = OreDictionary.WILDCARD_VALUE;
                } else {
                    meta = command.get("meta").getAsNumber().intValue();
                }
            }

            //get the item stack item+meta
            Item item = ForgeRegistries.ITEMS.getValue(itemname);
            if (item == null) {
                error(String.format("Unable to load item %s in command: %s", itemname.toString(), command.toString()));
                return;
            }

            //noinspection ConstantConditions
            testitem = new ItemStack(ForgeRegistries.ITEMS.getValue(itemname), 1, meta);
        }
        if(testitem == null){
            error(String.format("Item not found: %s",command.get("item").toString()));
            return;
        }
        meta = testitem.getMetadata();
        List<IRecipe> recipeList=CraftingManager.getInstance().getRecipeList();
        int index = 0;
        LinkedList<Integer> todelete = new LinkedList<>();

        for(IRecipe cur : recipeList){
            if(meta == OreDictionary.WILDCARD_VALUE && cur.getRecipeOutput().getItem().equals(testitem.getItem())){
                todelete.addFirst(index);
            }
            else if(cur.getRecipeOutput().isItemEqual(testitem)){
                todelete.addFirst(index);
            }
            index ++;
        }
        info(String.format("Removing %d recipes for %s.",todelete.size(),testitem.toString()));
        for(int i : todelete){
            recipeList.remove(i);
        }
    }
}
