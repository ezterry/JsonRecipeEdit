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

package com.ezrol.terry.minecraft.jsonRecipeEdit.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple registry of all the "JSON Recipe Edit" commands
 * Use getInstance() to receive the global instance
 *
 * Created by ezterry on 2/8/17.
 */
public class CommandRegistry {
    private Map<String, IRECommand> commands;
    private static CommandRegistry instance = null;

    /**
     * Get the singleton instance of CommandRegistry
     *
     * @return CommandRegistry instance
     */
    public synchronized static CommandRegistry getInstance(){
        if(instance == null){
            instance = new CommandRegistry();
        }
        return instance;
    }

    /**
     * private constructor call getInstance() for the CommandRegistry singleton
     */
    private CommandRegistry() {
        commands = new HashMap<>();
    }

    /**
     * Register a new command (the name will be pulled from the IRECommand object
     * @param cmd command to register
     */
    public synchronized void register(IRECommand cmd) {
        commands.put(cmd.getCommandName(), cmd);
    }

    /**
     * Return the IRECommand for the given command
     *
     * @param name - name of the command to lookup
     * @return the command object
     */
    public IRECommand getCommand(String name){
            return(commands.get(name));
    }

    /**
     * return true if the command 'name' has been registered
     * @param name - the command to check if exists
     * @return - true if the command exists, else false
     */
    public boolean hasCommand(String name) {
        return(commands.containsKey(name));
    }
}
