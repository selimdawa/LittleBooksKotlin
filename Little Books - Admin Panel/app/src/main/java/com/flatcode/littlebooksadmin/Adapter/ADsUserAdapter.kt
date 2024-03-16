package com.flatcode.littlebooksadmin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Filter.ADsUserFilter
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.MyApplication
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ItemAdsUserBinding
import java.text.MessageFormat

class ADsUserAdapter(private val context: Context, var list: ArrayList<User?>, isUser: Boolean) :
    RecyclerView.Adapter<ADsUserAdapter.ViewHolder>(), Filterable {

    private var binding: ItemAdsUserBinding? = null
    var filterList: ArrayList<User?>
    private var filter: ADsUserFilter? = null
    var isUser: Boolean
    var all = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemAdsUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val userId = DATA.EMPTY + item!!.id
        val username = DATA.EMPTY + item.username
        val profileImage = DATA.EMPTY + item.profileImage
        val timestamp = DATA.EMPTY + item.timestamp
        val adLoaded = DATA.EMPTY + item.adLoad
        val adClicked = DATA.EMPTY + item.adClick
        val formattedDate: String = MyApplication.formatTimestamp(timestamp.toLong())

        VOID.Glide(true, context, profileImage, holder.profileImage)

        if (username == DATA.EMPTY) {
            holder.username.visibility = View.GONE
        } else {
            holder.username.visibility = View.VISIBLE
            holder.username.text = username
        }

        val First = holder.position
        val Final = list.size - First
        holder.time.text = formattedDate
        holder.rank.text = MessageFormat.format("{0}", Final)
        holder.numberADsLoad.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adLoaded)
        holder.numberADsClick.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adClicked)

        //ADsNumber(userId, DATA.AD_LOADED, DATA.AD_LOAD, holder.numberADsLoad);
        //ADsNumber(userId, DATA.AD_CLICKED, DATA.AD_CLICK, holder.numberADsClick);
        holder.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.ADS_INFO, DATA.PROFILE_ID, userId)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ADsUserFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var profileImage: ImageView
        var username: TextView
        var rank: TextView
        var numberADsLoad: TextView
        var numberADsClick: TextView
        var time: TextView
        var item: LinearLayout

        init {
            profileImage = binding!!.profileImage
            username = binding!!.username
            rank = binding!!.rank
            numberADsLoad = binding!!.numberADsLoad
            numberADsClick = binding!!.numberADsClick
            time = binding!!.time
            item = binding!!.item
        }
    } /*private void ADsNumber(String userId, String type, String hash, TextView numberADs) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ADs item = snapshot.getValue(ADs.class);
                    assert item != null;
                    if (type.equals(DATA.AD_LOADED)) {
                        i = i + item.getAdsLoadedCount();
                    } else if (type.equals(DATA.AD_CLICKED)) {
                        i = i + item.getAdsClickedCount();
                    }
                    numberADs.setText(MessageFormat.format("{0}{1}", DATA.EMPTY, i));
                    updateAdsCount(i);
                }
            }

            private void updateAdsCount(int i) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(hash, i);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DATA.USERS);
                reference.child(userId).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    init {
        filterList = list
        this.isUser = isUser
    }
}