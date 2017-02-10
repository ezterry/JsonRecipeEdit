package com.ezrol.terry.minecraft.jsonRecipeEdit.commands;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.ezrol.terry.minecraft.jsonRecipeEdit.api.IRECommand;
import org.apache.logging.log4j.Level;

/**
 * A shell to the built in commands to add some logging shortcuts
 *
 * Created by ezterry on 2/9/17.
 */
public abstract class GenericCommand implements IRECommand{
    protected final void log(Level lvl,String msg){
        JSONRecipeEdit.log(lvl,String.format("(%s) %s",this.getCommandName(),msg));
    }
    protected final void error(String msg){
        log(Level.ERROR,msg);
    }
    protected final void info(String msg){
        log(Level.INFO,msg);
    }
}
