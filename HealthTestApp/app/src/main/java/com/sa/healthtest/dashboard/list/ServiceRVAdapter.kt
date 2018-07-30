package com.sa.healthtest.dashboard.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_services.view.*

class ServiceRVAdapter : RecyclerView.Adapter<ServiceRVAdapter.ServiceVH>() {

    private var items = ArrayList<FitResponse>()
    private var switchListener = PublishSubject.create<FitResponse>()
    private val NO_POSTION = -1

    fun setData(items: List<FitResponse>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun onSwitchStateChangedListener(): Observable<FitResponse> {
        return switchListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceVH =
            ServiceVH(LayoutInflater.from(parent.context).inflate(R.layout.item_services, parent, false))

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ServiceVH, position: Int) {
        holder.bind(items[position], switchListener)
    }

    fun onUserDeniedPermission(serviceName: String) {
        Observable.fromIterable(items)
                .filter { it.resourceName == serviceName }
                .map { it.isConnected = false }
                .map { getItemPositionByServiceName(serviceName) }
                .filter { it != NO_POSTION }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { notifyItemChanged(it) }
    }

    private fun getItemPositionByServiceName(serviceName: String): Int {
        for (i in items.indices) {
            if (items[i].resourceName == serviceName) return i
        }
        return NO_POSTION
    }

    class ServiceVH(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: FitResponse, listener: PublishSubject<FitResponse>) {
            itemView.service_icon.setImageResource(item.icon)
            itemView.service_name.text = item.resourceName
            itemView.connection_switcher.isChecked = item.isConnected
            itemView.connection_switcher.setOnCheckedChangeListener { _, isChecked ->
                item.isConnected = isChecked
                listener.onNext(item)
            }
        }
    }
}