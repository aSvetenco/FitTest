package com.sa.healthtest.dashboard.list

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.utils.inflate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_services.view.*

class ServiceRVAdapter : RecyclerView.Adapter<ServiceRVAdapter.ServiceVH>() {

    private var items = ArrayList<FitResponse>()
    private val NO_POSTION = -1
    internal var switchListener: (FitResponse) -> Unit = { _ -> }

    fun setData(items: List<FitResponse>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceVH =
            ServiceVH(parent.inflate(R.layout.item_services))

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ServiceVH, position: Int) {
        holder.bind(items[position], switchListener)
    }

    fun onUserDeniedPermission(serviceName: String) {
        Log.d("ServiceRVAdapter", "serviceName: $serviceName")
        Observable.fromIterable(items)
                .filter { it.clazzName == serviceName }
                .map { it.isConnected = false }
                .map { getItemPositionByServiceName(serviceName) }
                .filter { it != NO_POSTION }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { notifyItemChanged(it) }
    }

    private fun getItemPositionByServiceName(serviceName: String): Int {
        for (i in items.indices) {
            if (items[i].clazzName == serviceName) return i
        }
        return NO_POSTION
    }

    class ServiceVH(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: FitResponse, switchListener: (FitResponse) -> Unit) {
            itemView.service_icon.setImageResource(item.icon)
            itemView.service_name.text = item.resourceName
            itemView.connection_switcher.isChecked = item.isConnected
            itemView.connection_switcher.setOnCheckedChangeListener { _, isChecked ->
                item.isConnected = isChecked
                switchListener(item)
            }
        }
    }
}