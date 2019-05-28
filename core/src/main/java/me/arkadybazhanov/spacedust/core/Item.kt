package me.arkadybazhanov.spacedust.core

interface Item

class Weapon(val attack : Int): Item

class Armor(val armorClass: Int): Item