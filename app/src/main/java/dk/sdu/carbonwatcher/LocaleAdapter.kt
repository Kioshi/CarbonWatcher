package dk.sdu.carbonwatcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import java.util.*


class LocaleAdapter(
    private val mContext: Context, @param:LayoutRes private val mResource: Int,
    objects: ArrayList<Locale>
) : ArrayAdapter<Locale>(mContext, mResource, 0, objects) {

    private val mInflater: LayoutInflater
    private val items: ArrayList<Locale>

    init {
        mInflater = LayoutInflater.from(mContext)
        items = objects
    }

    override fun getDropDownView(
        position: Int, convertView: View?,
        parent: ViewGroup
    ): View {
        return createItemView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val offTypeTv = mInflater.inflate(mResource, parent, false) as TextView

        //val offTypeTv = view.findViewById(R.id.text1) as TextView
        val offerData = items[position]

        offTypeTv.setText(offerData.getDisplayCountry())

        return offTypeTv
    }
}