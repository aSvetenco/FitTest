package com.sa.healthtest.dashboard.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import kotlinx.android.synthetic.main.item_results.view.*

class ResultsRVAdapter : RecyclerView.Adapter<ResultsRVAdapter.ServiceVH>() {

    private var items = ArrayList<FitResponse>()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceVH =
            ServiceVH(LayoutInflater.from(parent.context).inflate(R.layout.item_results, parent, false))

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ServiceVH, position: Int) {
        holder.bind(items[position])
    }

    class ServiceVH(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: FitResponse) {
            itemView.steps.text = itemView.context.getString(R.string.results_template, item.stepCount)
            itemView.resource.text = item.resourceName
        }
    }
}