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
import com.ezrol.terry.minecraft.jsonRecipeEdit.virtualcommandblock.VCommandSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;

/**
 * Read in a Virtual Command Chain
 * These act like chains of command blocks based on the input criteria
 *
 * Created by ezterry on 2/18/17.
 */
public class VirtualCommandChain extends GenericCommand{
    @Override
    public String getCommandName() {
        return "virtual command chain";
    }

    private boolean isString(JsonElement e){
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isString();
    }

    @Override
    public void runCommand(JsonObject command) {
        VCommandSet chain;
        LinkedList<String> cmdlst = new LinkedList<>();
        String trigger;
        String dim;
        String filter;
        boolean log;
        String name;

        if (command.has("trigger") && isString(command.get("trigger"))) {
            trigger = command.get("trigger").getAsString();
        } else {
            error("Command chain trigger must be provided");
            error(String.format("not provided in: %s", command.toString()));
            return;
        }

        if (!command.has("dim")) {
            dim = "*";
        } else if (isString(command.get("dim"))) {
            dim = command.get("dim").getAsString();
        } else if (command.get("dim").isJsonPrimitive() && command.get("dim").getAsJsonPrimitive().isNumber()) {
            dim = String.format("%d", command.get("dim").getAsJsonPrimitive().getAsInt());
        } else {
            error(String.format("Command chain dimension must be either a string or a number, got: %s",
                    command.get("dim").toString()));
            return;
        }

        if (!command.has("filter")) {
            filter = "";
        } else if (isString(command.get("filter"))) {
            filter = command.get("filter").getAsString();
        } else {
            error(String.format("Unexpected command chain filter: %s",
                    command.get("filter").toString()));
            return;
        }

        log = command.has("log") && command.get("log").getAsJsonPrimitive().getAsBoolean();

        if (!command.has("name")) {
            name = "VCommandChain";
        } else {
            name = command.get("name").getAsJsonPrimitive().getAsString();
        }

        if (command.has("chain") && command.get("chain").isJsonArray()) {
            for (JsonElement e : command.get("chain").getAsJsonArray()) {
                if (isString(e)) {
                    cmdlst.addLast(e.getAsString());
                } else {
                    error(String.format("invalid chain command: %s", e.toString()));
                    return;
                }
            }
        }

        chain = new VCommandSet(trigger, filter, dim, cmdlst, log, name);
        JSONRecipeEdit.commandChains.addCommand(chain);
        info(String.format("Adding command chain: %s", name));
    }
}
