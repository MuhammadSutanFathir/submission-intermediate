package com.example.submissionintermediate.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.submissionintermediate.data.response.ListStoryItem
import com.example.submissionintermediate.databinding.ItemStoriesBinding
import com.example.submissionintermediate.view.DetailActivity
import com.example.submissionintermediate.view.DetailActivity.Companion.EXTRA_ID


class StoriesAdapter : PagingDataAdapter<ListStoryItem, StoriesAdapter.StoriesViewHolder>(diffCallback){
    class StoriesViewHolder(private val binding: ItemStoriesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (stories: ListStoryItem) {
            binding.tvItemName.text = stories.name
            Glide.with(itemView.context)
                .load(stories.photoUrl)
                .into(binding.ivItemPhoto)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(EXTRA_ID, stories.id)
                itemView.context.startActivity(intent)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding = ItemStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}