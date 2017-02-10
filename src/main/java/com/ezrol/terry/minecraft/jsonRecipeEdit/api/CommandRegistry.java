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
