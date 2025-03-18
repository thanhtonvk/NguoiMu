package com.phuyen.nguoikhuyettat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.phuyen.nguoikhuyettat.R
import com.phuyen.nguoikhuyettat.database.DBContext
import com.phuyen.nguoikhuyettat.models.NguoiThan
import com.phuyen.nguoikhuyettat.utils.BitmapUtils

class NguoiThanAdapter(var nguoiThanList: MutableList<NguoiThan>, var context: Context) :
    RecyclerView.Adapter<NguoiThanAdapter.ViewHolder>() {
    var dbContext: DBContext = DBContext(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val heroView = inflater.inflate(R.layout.item_nguoithan, parent, false)
        val viewHolder: ViewHolder = ViewHolder(heroView)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nguoiThan = nguoiThanList[position]
        val bitmap = BitmapUtils.getImage(nguoiThan.anh)
        holder.imgAvatar.setImageBitmap(bitmap)
        holder.tvName.text = nguoiThan.ten
        holder.btnXoa.setOnClickListener {
            dbContext.xoa(nguoiThan.embedding)
            nguoiThanList.remove(nguoiThan)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return nguoiThanList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgAvatar: ImageView =
            itemView.findViewById(R.id.imageAvatar)
        var tvName: TextView =
            itemView.findViewById(R.id.tvTen)
        var btnXoa: Button =
            itemView.findViewById(R.id.btnXoa)
    }
}
