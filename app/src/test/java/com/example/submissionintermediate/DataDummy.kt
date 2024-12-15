package com.example.submissionintermediate

import com.example.submissionintermediate.data.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                id = i.toString(),
                name = "Author $i",
                description = "Description of story $i",
                photoUrl = "https://dummyimage.com/600x400/000/fff&text=Story+$i",
                createdAt = "2024-12-03T00:00:00Z",
                lon = 100.0 + i,
                lat = -6.0 + i
            )
            items.add(story)
        }
        return items
    }
}
