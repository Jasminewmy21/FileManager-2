package com.example.filemanager

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class FileAdapter(mContext: Context, fileitList: List<Fileit>) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    private var mlongClickListener: OnItemLongClickListener? = null
    private var mClickListener: OnItemClickListener? = null

    inner class ViewHolder(view: View, listener: OnItemClickListener, longlistener: OnItemLongClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener, OnLongClickListener {
        private val mlongListener: OnItemLongClickListener
        private val mListener: OnItemClickListener
        var fileimg: ImageView
        var filename: TextView
        var llRoot: LinearLayout
        override fun onLongClick(v: View): Boolean {
            mlongListener.onItemLongClick(v, position)
            return true
        }

        override fun onClick(v: View) {
            mListener.onItemClick(v, position)
        }

        init {
            mListener = listener
            mlongListener = longlistener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            fileimg = view.findViewById<View>(R.id.file_img) as ImageView
            filename = view.findViewById<View>(R.id.file_name) as TextView
            llRoot = view.findViewById(R.id.ll_root)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, postion: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, postion: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        mlongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view, mClickListener!!, mlongClickListener!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileit = mfileList[position]
        if (fileit.imageId == 1) {
            holder.fileimg.setImageResource(R.drawable.ic_file)
            holder.filename.text = fileit.name
            holder.llRoot.setPadding(dp2px(holder.llRoot.context, 15f), 0, 0, 0)
        } else if (fileit.imageId == 0 || fileit.imageId == 2 || fileit.imageId == 3) {
            holder.fileimg.setImageResource(R.drawable.ic_folder)
            holder.filename.text = fileit.name
            holder.llRoot.setPadding(dp2px(holder.llRoot.context, 10f), 0, 0, 0)
        }
    }

    override fun getItemCount(): Int {
        return mfileList.size
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    companion object {
        private lateinit var mfileList: List<Fileit>
        fun dp2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }

    init {
        mfileList = fileitList
    }
}