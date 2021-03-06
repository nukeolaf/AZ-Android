package com.az.model.posts.detail

import com.az.model.posts.AuthorData

data class DetailedPostData(
    val author: AuthorData,
    val bookMarkCount: Int,
    val commentCount: Int,
    val content: String,
    val createdDate: String,
    val id: Int,
    val likes: Int,
    val modifiedDate: String,
    val pressBookMark: Boolean,
    val pressLike: Boolean
)