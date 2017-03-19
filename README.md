# JSON Recipe Edit #
A Json based recipe editor (and tweaker)

## Introduction ##

JSON Recipe Edit is a Minecraft mod intended to provide
a way for modpack creators to customize many elements
of their pack.

It works by having a single json file the pack creator
makes, this file is called the json script and is
evaluated after all mods are initialized.

Here many types of tweaks are possible such as updating
recipes and other parameters of blocks and items.  In many
ways this makes JSON Recipe Editor a mod related to
minetweaker / crafttweaker, however with a different
runtime, and no way to reload once the world is loaded, this 
means some things possible in this mod that can't be done in
minetweaker, and some things in minetweaker can't be done in
this mod.

## Json Script ##

The Json script is provided by the mod pack creator and
is stored in config/jsonrecipeedit.json the root node
of the script is a json array, containing a combination of
objects and strings.

**Strings**- the strings in the main json array are ignored
and are considered comments (since the json spec has no
comments)

**Objects**- these represent one or more commands to be run
in the order they are in the file,  Every "command" object
contains a member "command" with the value representing the
command name.  the rest of the object is defined by the
command.

A JSON Schema (http://json-schema.org/) is in this
repository util/jsonrecipeedit.schema.json to help users
validate their JSON Scripts prior to loading the pack.

## Commands ##

Commands and Documentation of the jsonScript commands and
their structure is avalible on the wiki at

https://github.com/ezterry/JsonRecipeEdit/wiki

## Mod Packs / Reuse Policy ##

This mod is released under a 2 clause BSD licence and thus
it may be used in any modpack public or private.  Also
you may modify and redistribute this mod without permission
form the author.

However it is considered polite if you distribute this mod
either with or without modification to link to either my
original github or curseforge page.

## API ##

If third pary mods wish to add commands they may by 
including the two api classes in your mods API folder:

+ com.ezrol.terry.minecraft.jsonRecipeEdit.api.CommandRegistry
+ com.ezrol.terry.minecraft.jsonRecipeEdit.api.IRECommand

For each command you want to add you will need to implement
an ``IRECommand`` object.  Recommended is in the constructor
of your ``IRECommand`` object to call ``register()``:

    public class HelloJson implements IRECommand{
        public HelloJson(){
            CommandRegistry.getInstance().register(this);
        }
        @Override
        public String getCommandName(){
            //command name (as to be used in the json)
            return("hello json");
        }
        @Override
        void public runCommand(JsonObject command){
            //JsonObject is your commands GSON json
            //object, use to get any parameters then
            //tweak per the input, here we just log
            
            FMLLog.log("YourModId", Level.INFO,
                       "Hello Json Script");
        }
    }
Then in your mods ``init()``, call the constructor if 
 Json Recipe edit is loaded.
 
    @EventHandler
    public void init(FMLInitializationEvent event) {
        
        /* initialize your mod */
        
        if(Loader.isModLoaded("jsonrecipeedit")){
            new HelloJson();
        }
    }
 
 Note" the schema object provided with this mod will not
 support third part mods out of box, althogh alternatives
 may be made for popular mods.
