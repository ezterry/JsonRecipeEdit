package com.ezrol.terry.minecraft.jsonRecipeEdit.api;

import com.google.gson.JsonObject;

/**
 * A JSON Recipe Edit Command
 * Created by ezterry on 2/8/17.
 */
public interface IRECommand {
    /**
     * get the name of the command
     * @return - the command name
     */
    String getCommandName();

    /**
     * process the command
     *
     * This is called with the json object from the "json script" with this command, it is up
     * to your IRECommand implementation to extract data from the JsonObject and action the request
     *
     * @param command the JSONObject with the "command" getCommandName() to be actioned
     */
    void runCommand(JsonObject command);

}
