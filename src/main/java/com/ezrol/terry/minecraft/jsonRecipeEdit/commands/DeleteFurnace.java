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
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;

/**
 * Remove a recipe from the Furnace
 * Created by ezterry on 2/12/17.
 */
public class DeleteFurnace extends GenericCommand {
    private final Field smeltingList;
    private final Field experienceList;

    public DeleteFurnace(){
        boolean deob;
        try {
            FurnaceRecipes.class.getDeclaredField("smeltingList");
            deob = true;
        } catch (NoSuchFieldException e) {
            deob = false;
        }

        try {
            if (deob) {
                smeltingList = FurnaceRecipes.class.getDeclaredField("smeltingList");
                experienceList = FurnaceRecipes.class.getDeclaredField("experienceList");
            } else {
                smeltingList = FurnaceRecipes.class.getDeclaredField("field_77604_b");
                experienceList = FurnaceRecipes.class.getDeclaredField("field_77605_c");
            }
            smeltingList.setAccessible(true);
            experienceList.setAccessible(true);
        }
        catch(Exception e){
            throw(new RuntimeException(String.format("Error reflecting FurnaceRecipes - %s",e.toString())));
        }
    }

    @Override
    public String getCommandName() {
        return "delete furnace";
    }

    @Override
    public void runCommand(JsonObject command) {
        FurnaceRecipes furnace = FurnaceRecipes.instance();

        ResourceLocation itemname;
        int meta=0;

        itemname = new ResourceLocation(command.get("item").getAsString());
        if(command.has("meta")){
            if(command.get("meta").getAsJsonPrimitive().isString() &&
                    command.get("meta").getAsJsonPrimitive().getAsString().equals("*")){
                meta = OreDictionary.WILDCARD_VALUE;
            }
            else {
                meta = command.get("meta").getAsNumber().intValue();
            }
        }

        //get the item stack item+meta
        Item item = ForgeRegistries.ITEMS.getValue(itemname);
        if(item == null){
            error(String.format("Unable to load item %s in command: %s",itemname.toString(),command.toString()));
            return;
        }

        @SuppressWarnings("ConstantConditions")
        ItemStack testitem = new ItemStack(ForgeRegistries.ITEMS.getValue(itemname), 1, meta);

        try {
            @SuppressWarnings({"unchecked"})
            Map<ItemStack, ItemStack> list = (Map<ItemStack, ItemStack>) smeltingList.get(furnace);
            @SuppressWarnings({"unchecked"})
            Map<ItemStack, Float> exp = (Map<ItemStack, Float>) experienceList.get(furnace);

            LinkedList<ItemStack> todelete = new LinkedList<>();

            for (Map.Entry<ItemStack, ItemStack> entry : list.entrySet()){
                if(meta == OreDictionary.WILDCARD_VALUE && entry.getValue().getItem().equals(testitem.getItem())){
                    todelete.addFirst(entry.getKey());
                    exp.remove(entry.getValue());
                }
                else if(entry.getValue().isItemEqual(testitem)){
                    todelete.addFirst(entry.getKey());
                    exp.remove(entry.getValue());
                }
            }
            for(ItemStack rm : todelete){
                info(String.format("Removing furnace recipe: %s",rm.toString()));
                list.remove(rm);
            }
        } catch (IllegalAccessException e) {
            error("unable to access furnace recipes");
        }
    }
}