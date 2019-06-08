package me.arkadybazhanov.spacedust

import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.item_view.view.*
import me.arkadybazhanov.spacedust.Inventory.ItemHolder
import me.arkadybazhanov.spacedust.core.Item
import me.arkadybazhanov.spacedust.core.Weapon

class Inventory : RecyclerView.Adapter<ItemHolder>() {
    private val _items: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int = _items.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val text = _items[position].name +
                (_items[position] as? Weapon)?.let {
                    " (damage: ${it.damage})"
                }
        holder.textView.text = text
    }

    class ItemHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView get() = view.itemTextView
    }

    val items: MutableList<Item> = object : AbstractMutableList<Item>() {
        override val size: Int get() = _items.size
        override fun add(index: Int, element: Item) = _items.add(index, element).also { notifyItemInserted(index) }
        override fun get(index: Int): Item = _items[index]
        override fun removeAt(index: Int): Item = _items.removeAt(index).also {notifyItemRemoved(index) }
        override fun set(index: Int, element: Item): Item = _items.set(index, element).also { notifyItemChanged(index) }
    }
}