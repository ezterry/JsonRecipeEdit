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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Class that implements adding both shaped and shapeless recipes
 * (this is implemented in two seperate JSON commands)
 *
 * Created by ezterry on 2/9/17.
 */
public class AddRecipe extends GenericCommand{
    private boolean shapped;
    static private final String validref="abcdefghijklmnopqrstuvwxyz";

    public AddRecipe(boolean shapped){
        this.shapped=shapped;
    }

    static private boolean CheckRef(String r) {
        return r.length() == 1 && (validref.contains(r));
    }

    @Override
    public String getCommandName() {
        if(shapped){
            return("shaped recipe");
        }
        else{
            return("shapeless recipe");
        }
    }

    @Nullable
    private ItemStack getItemFromArray(JsonArray input,int amount){
        if(input.size() == 0){
            error("null array expecting [<item name>] or [<item name>,<meta>]");
            return(null);
        }
        int meta = 0;

        try{
            ResourceLocation itemres;
            itemres = new ResourceLocation(input.get(0).getAsString());
            if(input.size()>=2){
                meta = input.get(1).getAsInt();
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

    @Override
    public void runCommand(JsonObject command) {
        List<Object> params = new LinkedList<>();

        //first read in ingredients
        JsonObject jsonIngredients = command.get("ingredients").getAsJsonObject();
        for(Map.Entry<String,JsonElement> e : jsonIngredients.entrySet()){
            if(!CheckRef(e.getKey())){
                error(String.format("invalid ingredient reference '%s' in command: %s",e.getKey(),command));
                return;
            }
            if(e.getValue().isJsonPrimitive()){
                //assume its an oredict entry
                if(shapped) {
                    params.add(e.getKey().toCharArray()[0]);
                }
                params.add(e.getValue().getAsString());
            }
            else if(e.getValue().isJsonArray()){
                ItemStack itm = getItemFromArray(e.getValue().getAsJsonArray(),1);
                if(itm == null){
                    error(String.format("unable to find item %s",e.getValue().toString()));
                    return;
                }
                if(shapped) {
                    params.add(e.getKey().toCharArray()[0]);
                }
                params.add(itm);
            }
        }

        //read in the template
        if(shapped) {
            int index = 0;
            for (JsonElement tmpl : command.get("template").getAsJsonArray()) {
                params.add(index, tmpl.getAsString());
                index++;
            }
        }

        //load the resulting item [item,meta,nbt]
        int stacksize = 1;
        if(command.has("count") && command.get("count").isJsonPrimitive()) {
            try{
                stacksize = command.get("count").getAsJsonPrimitive().getAsInt();
            }
            catch (Exception e){
                error(String.format("Unable to parse count (stack size) assuming 1 for command: %s",command.toString()));
            }
        }
        ItemStack result = getItemFromArray(command.get("result").getAsJsonArray(),stacksize);
        if(result == null){
            error(String.format("unable to find Item in result while running command %s",command.toString()));
            return;
        }
        if(command.get("result").getAsJsonArray().size()>=3){
            try
            {
                result.setTagCompound(
                        JsonToNBT.getTagFromJson(command.get("result").getAsJsonArray().get(2).getAsString()));
            }
            catch (NBTException nbtexception)
            {
                error(String.format("unable to parse NBT: %s",nbtexception));
                error(String.format("String ignoring NBT data for command: %s",command.toString()));
            }
        }

        //now the recipe is loaded
        if(shapped){
            ShapedOreRecipe shapedRecipe = new ShapedOreRecipe(result,params.toArray());
            //shaped recipes can be mirrored (true by default)
            if(command.has("mirrored") &&
                    command.get("mirrored").isJsonPrimitive() &&
                    command.getAsJsonPrimitive("mirrored").isBoolean())
            {
                shapedRecipe.setMirrored(command.getAsJsonPrimitive("mirrored").getAsBoolean());
            }
            else{
                shapedRecipe.setMirrored(true);
            }

            info(String.format("Add recipe for %s.",result));
            GameRegistry.addRecipe(shapedRecipe);
        }
        else{
            ShapelessOreRecipe shapelessRecipe = new ShapelessOreRecipe(result,params.toArray());

            info(String.format("Add recipe for %s.",result));
            GameRegistry.addRecipe(shapelessRecipe);
        }
    }
}
