JSON Recipe Edit

A Json based recipe editor:

Allows you to:
* Remove existing recipes
* Add items to the ore dictionary
* Remove an item from the ore dictionary
* Add new shapeless recipes
* Add new shaped recipes

Json Format

[
  {
    "command": "del_recipe",
    "item": "minecraft:emerald_ore",
  },
  {
    "command": "add_ore",
    "orename": "gem_allgems"
    "item": "minecraft:diamond_ore",
  },
  {
    "command": "del_ore",
    "orename": "gem_diamond",
    "item": "minecraft:diamond_ore:*",
  },
  {
    "command": "shapeless_recipe",
    "item": "minecraft:emerald_ore",
    "recipe": ["minecraft:stone:0","minecraft:stone","minecraft:stone",
               null,"ore:gem_emerald",null,
               "minecraft:stone","minecraft:stone","minecraft:stone"]
  }
]

#notes:
command = command the block is related to:
 - del_recipe, remove the recipe for "item"
 - add_ore, add "item" to the ore dictionary for "orename"
 - del_ore, remove the "item" form the ore dictionary "orename"
 - shapeless_recipe, add a recipe for "item" using the list in "recipe"
 - shaped_recipe, add a recipe for "item" using the list in "recipe"
 - add_smelting, smelt the first item from "recipe" into "item"
 - del_smelting, remove item from being able to smelt
 - init_inv, this "item" will be added to the list of items to give when a player
     first spawns (initial inventory)
 
item = minecraft item to remove/add to the recipe/ore dict meta is after the ":"
you can have either "*" for all metadata, or comma separated for a list the 
recipe commands will only craft the first result found (or default for '*')

orename = ore dictionary name

recipe = a list of items:meta, a list <= 4 in length is a 2x2 grid, else the
items are mapped into a 3x3 (left to right top to bottom)
smelting recipes only the first item will be used


