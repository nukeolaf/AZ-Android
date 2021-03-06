package com.az.network.posts.detail.comments

import com.az.core.Resource
import com.az.model.posts.detail.comments.CommentsData
import com.az.network.responsehandler.ResponseHandler

class CommentsRemoteDataSourceImpl(
    private val commentsApi: CommentsApi,
    private val responseHandler: ResponseHandler
) : CommentsRemoteDataSource {
    override suspend fun getComments(
        postId: Int,
        currentPage: Int,
        size: Int
    ): Resource<CommentsData> {
        return try {
            commentsApi.getComments(postId, currentPage, size).let { response ->
                responseHandler.handleSuccess(response)
            }
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }
}