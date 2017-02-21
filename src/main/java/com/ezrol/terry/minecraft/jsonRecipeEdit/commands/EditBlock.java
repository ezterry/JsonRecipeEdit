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
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Allows editing of a block data
 * Created by ezterry on 2/21/17.
 */
public class EditBlock extends GenericCommand{

    @Override
    public String getCommandName() {
        return "edit block";
    }

    @Override
    public void runCommand(JsonObject command) {
        Block block;
        String blockName = command.get("block").getAsString();

        ResourceLocation key = new ResourceLocation(blockName);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) {
            error(String.format("Cant find block: %s",blockName));
            return;
        }

        block = ForgeRegistries.BLOCKS.getValue(key);

        if(block == null){
            error(String.format("unable to find block, canceling edit: %s",command.toString()));
            return;
        }

        info(String.format("Editing stats for %s",block.toString()));
        //now we have the block see what can be changed
        if(command.has("hardness")){
            block.setHardness(command.get("hardness").getAsFloat());
        }
        if(command.has("resistance")){
            block.setResistance(command.get("resistance").getAsFloat());
        }
        if(command.has("unbreakable")){
            if(command.get("unbreakable").getAsBoolean()){
                block.setBlockUnbreakable();
            }
        }
        if(command.has("harvestlevel")){
            JsonArray ar = command.get("harvestlevel").getAsJsonArray();
            if(ar.size() != 2){
                error(String.format("invalid harvest level array, must be [\"class\",level]: %s", ar.toString()));
                return;
            }
            block.setHarvestLevel(ar.get(0).getAsString(),ar.get(1).getAsInt());
        }
        if(command.has("unlocalizedname")){
            block.setUnlocalizedName(command.get("unlocalizedname").getAsString());
        }
        if(command.has("lightlevel")){
            block.setLightLevel(command.get("lightlevel").getAsFloat());
        }
        if(command.has("lightopacity")){
            block.setLightOpacity(command.get("lightopacity").getAsInt());
        }
        if(command.has("slipperiness")){
            block.slipperiness = command.get("slipperiness").getAsFloat();
        }
    }
}
