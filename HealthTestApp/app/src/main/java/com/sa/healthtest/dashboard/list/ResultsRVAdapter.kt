package com.sa.healthtest.dashboard.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.utils.inflate
import kotlinx.android.synthetic.main.item_results.view.*

class ResultsRVAdapter : RecyclerView.Adapter<ResultsRVAdapter.ResultVH>() {

    private var items = ArrayList<FitResponse>()

    fun removeItem(tag: String?) {
        if (items.isNotEmpty()) {
            for (i in items.indices) {
                if (items[i].resourceName == tag) {
                    items.removeAt(i)
                    notifyItemRemoved(i)
                }
            }
        }
    }

    fun setData(item: FitResponse) {
        var isAdded = false
        if (items.isEmpty()) {
            items.add(item)
            notifyDataSetChanged()
        } else {
            for (i in items.indices) {
                if (items[i].resourceName == item.resourceName) {
                    items[i] = item
                    notifyItemChanged(i)
                    isAdded = true
                    break
                }
            }
            if (!isAdded) {
                items.add(item)
                notifyItemInserted(items.size - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsRVAdapter.ResultVH =
            ResultsRVAdapter.ResultVH(parent.inflate(R.layout.item_results))

    override fun onBindViewHolder(holder: ResultsRVAdapter.ResultVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ResultVH(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: FitResponse) {
            itemView.steps.text = itemView.context.getString(R.string.results_template, item.stepCount)
            itemView.resource.setImageResource(item.icon)
        }
    }
}