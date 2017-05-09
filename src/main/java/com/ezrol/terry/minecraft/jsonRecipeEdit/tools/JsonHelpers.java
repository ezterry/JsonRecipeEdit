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

package com.ezrol.terry.minecraft.jsonRecipeEdit.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A collection of gson helper functions
 *
 * Created by ezterry on 5/7/17.
 */
@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class JsonHelpers {
    /**
     * Get an integer (required) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static int getInt(JsonObject obj, String name){
        if(!obj.has(name)){
            throw(new RuntimeException(String.format("Integer %s is required",name)));
        }
        try{
            return(obj.get(name).getAsInt());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get integer %s from json: (%s)",name,e.toString())));
        }
    }

    /**
     * Get an integer (optional) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static int getInt(JsonObject obj,String name,int def){
        if(!obj.has(name)){
            return(def);
        }
        try{
            return(obj.get(name).getAsInt());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get integer %s from json: (%s)",name,e.toString())));
        }
    }
    /**
     * Get a string (required) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static String getString(JsonObject obj,String name){
        if(!obj.has(name)){
            throw(new RuntimeException(String.format("String %s is required",name)));
        }
        try{
            return(obj.get(name).getAsString());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get string %s from json: (%s)",name,e.toString())));
        }
    }

    /**
     * Get a string (optional) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static String getString(JsonObject obj,String name,String def){
        if(!obj.has(name)){
            return(def);
        }
        try{
            return(obj.get(name).getAsString());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get string %s from json: (%s)",name,e.toString())));
        }
    }

    /**
     * Get a Bool (required) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static boolean getBool(JsonObject obj,String name){
        if(!obj.has(name)){
            throw(new RuntimeException(String.format("Boolean %s is required",name)));
        }
        try{
            return(obj.get(name).getAsBoolean());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get boolean %s from json: (%s)",name,e.toString())));
        }
    }

    /**
     * Get a Bool (optional) from the json object)
     *
     * @param obj - json object to look at
     * @param name - name of the element to extract
     * @return - the integer found
     */
    public static boolean getBool(JsonObject obj,String name,boolean def){
        if(!obj.has(name)){
            return(def);
        }
        try{
            return(obj.get(name).getAsBoolean());
        }
        catch (Exception e){
            throw(new RuntimeException(String.format("Unable to get boolean %s from json: (%s)",name,e.toString())));
        }
    }

    public static void assertExists(JsonObject obj,String name){
        if(!obj.has(name)){
            throw(new RuntimeException(String.format("Field %s is required",name)));
        }
    }

    public static void assertArray(JsonObject obj,String name){
        assertExists(obj,name);
        if(!obj.get(name).isJsonArray()){
            throw(new RuntimeException(String.format("Field %s must be a json array",name)));
        }
    }

    public static void assertArrayOrString(JsonObject obj,String name){
        assertExists(obj,name);
        JsonElement i = obj.get(name);
        if(!i.isJsonArray()){
            if(!i.isJsonPrimitive()) {
                throw (new RuntimeException(String.format("Field %s must be a json array", name)));
            }
        }
    }
}
