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
import com.ezrol.terry.minecraft.jsonRecipeEdit.api.IRECommand;
import org.apache.logging.log4j.Level;

/**
 * A shell to the built in commands to add some logging shortcuts
 *
 * Created by ezterry on 2/9/17.
 */
@SuppressWarnings("WeakerAccess")
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
