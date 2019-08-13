package com.idanatz.oneadapter.internal.holders

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.idanatz.oneadapter.external.events.EventHooksMap
import com.idanatz.oneadapter.external.events.SwipeEventHook
import com.idanatz.oneadapter.internal.selection.OneItemDetail
import com.idanatz.oneadapter.internal.interfaces.InternalModuleConfig
import com.idanatz.oneadapter.external.states.StatesMap
import com.idanatz.oneadapter.internal.utils.inflateLayout
import com.idanatz.oneadapter.internal.utils.let2

@Suppress("NAME_SHADOWING")
internal abstract class OneViewHolder<M> (
        parent: ViewGroup,
        moduleConfig: InternalModuleConfig,
        private val statesHooksMap: StatesMap<M>? = null, // Not all ViewHolders will have states configured
        private val eventsHooksMap: EventHooksMap<M>? = null // Not all ViewHolders will have events configured
) : RecyclerView.ViewHolder(parent.context.inflateLayout(moduleConfig.withLayoutResource(), parent, false)) {

    private var model: M? = null
    lateinit var viewBinder: ViewBinder
    var isSwiping = false

    abstract fun onBind(model: M?)
    abstract fun onUnbind()

    fun onBindViewHolder(model: M?) {
        this.model = model
        this.viewBinder = ViewBinder(itemView)

        handleEventHooks()
        onBind(model)
    }

    fun onBindSelection(model: M?, selected: Boolean) {
        let2(statesHooksMap?.getSelectionState(), model) { selectionState, model ->
            if (selected) selectionState.onSelected(model, true)
            else selectionState.onSelected(model, false)
        }
    }

    fun createItemLookupInformation(): OneItemDetail? {
        if (isSwiping) // while swiping disable the option to select
            return null

        return let2(statesHooksMap?.getSelectionState(), model) { selectionState, model ->
            return if (selectionState.selectionEnabled(model)) {
                OneItemDetail(adapterPosition, itemId)
            }
            else
                null
        }
    }

    fun getSwipeEventHook() = eventsHooksMap?.getSwipeEventHook()

    fun onSwipe(canvas: Canvas, xAxisOffset: Float) {
        eventsHooksMap?.getSwipeEventHook()?.onSwipe(canvas, xAxisOffset, viewBinder)
    }

    fun onSwipeComplete(swipeDirection: SwipeEventHook.SwipeDirection) {
        let2(eventsHooksMap?.getSwipeEventHook(), model) { swipeEventHook, model ->
            swipeEventHook.onSwipeComplete(model, swipeDirection, viewBinder)
        }
    }

    private fun handleEventHooks() {
        let2(eventsHooksMap?.getClickEventHook(), model) { clickEventHook, model ->
            itemView.setOnClickListener { clickEventHook.onClick(model, viewBinder) }
        }
    }
}