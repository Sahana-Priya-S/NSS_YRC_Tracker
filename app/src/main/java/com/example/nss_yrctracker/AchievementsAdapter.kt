package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AchievementsAdapter(private var achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        this.achievements = newAchievements
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.achievementIconImageView)
        private val name: TextView = itemView.findViewById(R.id.achievementNameTextView)
        private val description: TextView = itemView.findViewById(R.id.achievementDescriptionTextView)

        fun bind(achievement: Achievement) {
            name.text = achievement.name
            description.text = achievement.description

            // Use Glide to load the icon, with a placeholder
            Glide.with(itemView.context)
                .load(achievement.iconUrl)
                .placeholder(R.drawable.ic_achievements) // Default icon if URL is empty/invalid
                .into(icon)
        }
    }
}