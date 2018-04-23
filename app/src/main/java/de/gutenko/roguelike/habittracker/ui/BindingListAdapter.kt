package de.gutenko.roguelike.habittracker.ui

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_ID
import android.view.LayoutInflater
import android.view.ViewGroup
import de.gutenko.roguelike.habittracker.androidLog
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

data class BindingViewHolder<T : ViewDataBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root)

class BindingListAdapter<T : ViewDataBinding, I, E>(
    @LayoutRes
    private val layoutId: Int,
    private val variable: Int,
    private val eventsMapper: (T, Int) -> Observable<E>,
    private val itemIds: ((I) -> Long)?,
    itemCallback: DiffUtil.ItemCallback<I>
) : ListAdapter<I, BindingViewHolder<T>>(itemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<T>(inflater, layoutId, parent, false)

        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T>, position: Int) {
        holder.binding.setVariable(variable, getItem(position))

        eventsMapper(holder.binding, position).subscribe(eventSubject)
    }

    override fun getItemId(position: Int): Long {
        return itemIds?.invoke(getItem(position)) ?: NO_ID
    }

    private val eventSubject = PublishSubject.create<E>()
    val events = eventSubject.androidLog("Events")

    class Builder<T : ViewDataBinding, I, E> {
        private lateinit var sameComparator: (I, I) -> Boolean
        private lateinit var contentComparator: (I, I) -> Boolean
        private var variable = 0
        private lateinit var mapper: (T, Int) -> Observable<E>
        private var idMapper: ((I) -> Long)? = null

        fun identical(comparator: (I, I) -> Boolean): Builder<T, I, E> = apply {
            sameComparator = comparator
        }

        fun same(comparator: (I, I) -> Boolean): Builder<T, I, E> = apply {
            contentComparator = comparator
        }

        fun variable(variable: Int) = apply {
            this.variable = variable
        }

        fun ids(idMapper: (I) -> Long) = apply {
            this.idMapper = idMapper
        }

        fun eventsFor(mapper: (T, Int) -> Observable<E>) = apply {
            this.mapper = mapper
        }

        fun build(@LayoutRes layoutId: Int): BindingListAdapter<T, I, E> {
            val sameComparator = sameComparator
            val contentComparator = contentComparator

            return BindingListAdapter(
                layoutId,
                variable,
                mapper,
                idMapper,
                object : DiffUtil.ItemCallback<I>() {
                    override fun areItemsTheSame(oldItem: I, newItem: I): Boolean {
                        return sameComparator(oldItem, newItem)
                    }

                    override fun areContentsTheSame(oldItem: I, newItem: I): Boolean {
                        return contentComparator(oldItem, newItem)
                    }
                })
                .apply {
                    setHasStableIds(idMapper != null)
                }
        }
    }
}