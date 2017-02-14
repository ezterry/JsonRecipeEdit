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
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Add a furnace recipe
 *
 * Created by ezterry on 2/12/17.
 */
public class AddFurnace extends GenericCommand {
    @Override
    public String getCommandName() {
        return "add furnace";
    }

    @Override
    public void runCommand(JsonObject command) {
        ItemStack in;
        ItemStack out;
        float xp=0.1f;

        in = getItemFromArray(command.get("input").getAsJsonArray(),1);
        if(in == null){
            error(String.format("unable to find Item in that you are tying to cook %s",command.toString()));
            return;
        }
        int stacksize = 1;
        if(command.has("count") && command.get("count").isJsonPrimitive()) {
            try{
                stacksize = command.get("count").getAsJsonPrimitive().getAsInt();
            }
            catch (Exception e){
                error(String.format("Unable to parse count (stack size) assuming 1 for command: %s",command.toString()));
            }
        }
        out = getItemFromArray(command.get("output").getAsJsonArray(),stacksize);
        if(out == null){
            error(String.format("unable to find Item in result while running command %s",command.toString()));
            return;
        }

        if(command.has("xp")){
            xp = command.get("xp").getAsFloat();
        }
        GameRegistry.addSmelting(in,out,xp);
    }
}
