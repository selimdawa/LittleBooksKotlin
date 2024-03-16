package com.flatcode.littlebooks.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.flatcode.littlebooks.Adapter.ImageSliderAdapter.SliderViewHolder
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarteist.autoimageslider.SliderViewAdapter
import java.util.Objects

class ImageSliderAdapter(var context: Context?, var setTotalCount: Int) :
    SliderViewAdapter<SliderViewHolder>() {

    var ImageLink: String? = null

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    when (position) {
                        0 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("1").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        1 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("2").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        2 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("3").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        3 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("4").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        4 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("5").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        5 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("6").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        6 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("7").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        7 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("8").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        8 -> {
                            ImageLink = Objects.requireNonNull(snapshot.child("9").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        9 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("10").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        10 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("11").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        11 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("12").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        12 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("13").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        13 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("14").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        14 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("15").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        15 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("16").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        16 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("17").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        17 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("18").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        18 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("19").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }

                        19 -> {
                            ImageLink =
                                Objects.requireNonNull(snapshot.child("20").value).toString()
                            Glide.with(viewHolder.itemView).load(ImageLink)
                                .into(viewHolder.ImageSlider)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getCount(): Int {
        return setTotalCount
    }

    class SliderViewHolder(var itemView: View) : ViewHolder(
        itemView
    ) {
        var ImageSlider: ImageView

        init {
            ImageSlider = itemView.findViewById(R.id.imageView)
        }
    }
}