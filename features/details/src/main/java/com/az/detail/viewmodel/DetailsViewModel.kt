package com.az.detail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.az.core.LoginStatus
import com.az.core.Preferences
import com.az.core.Status
import com.az.infinite_recyclerview.InfiniteViewModel
import com.az.model.posts.SimplePageData
import com.az.model.posts.detail.PostDetailData
import com.az.model.posts.detail.PostDetailRepository
import com.az.model.posts.detail.comments.CommentData
import com.az.model.posts.detail.comments.CommentsRepository
import com.az.model.posts.detail.comments.create.CreateCommentRepository
import com.az.model.posts.detail.comments.create.CreateCommentRequestData
import com.az.model.posts.detail.likes.LikePostRepository
import com.az.model.users.bookmark.create.CreateBookmarkRepository
import com.az.model.users.bookmark.delete.DeleteBookmarkRepository
import kotlinx.coroutines.launch

const val size = 10

class DetailsViewModel(
    private val postDetailRepository: PostDetailRepository,
    private val commentsRepository: CommentsRepository,
    private val createCommentRepository: CreateCommentRepository,
    private val likePostRepository: LikePostRepository,
    private val createBookmarkRepository: CreateBookmarkRepository,
    private val deleteBookmarkRepository: DeleteBookmarkRepository,
    sharedPrefs: Preferences
) : InfiniteViewModel<CommentData>() {

    private var postId = 0

    private val loginStatus = sharedPrefs.getLoginStatus()
    private val loginSession = sharedPrefs.getLoginSession()

    private val _details = MutableLiveData<PostDetailData>()
    val details: LiveData<PostDetailData> = _details

    val comments: LiveData<List<CommentData?>> = items
    private lateinit var simplePageData: SimplePageData

    val comment = MutableLiveData<String>()

    var toastMessageHandler: ((message: String) -> Unit)? = null
    var hideSoftInputHandler: (() -> Unit)? = null

    init {
        initSimplePageData()
    }

    fun isGuestLogin(): Boolean {
        return loginStatus == LoginStatus.GUEST_LOGIN.status
    }

    fun setPostId(id: Int) {
        postId = id
        getPostDetail()
        getItems()
    }

    private fun initSimplePageData() {
        simplePageData = SimplePageData(0, 0, 0)
    }

    private fun getCurrentPage(): Int {
        return simplePageData.currentPage + 1
    }

    override fun hasNextPage(): Boolean {
        return simplePageData.let { it.currentPage < it.totalPages }
    }

    override fun getItems() {
        setIsLoading(true)
        getComments()
    }

    private fun getPostDetail() {
        viewModelScope.launch {
            val response = postDetailRepository.getPostDetail(postId)
            when (response.status) {
                Status.SUCCESS -> _details.value = response.data
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
        }
    }

    private fun getComments() {
        viewModelScope.launch {
            val response = commentsRepository.getComments(postId, getCurrentPage(), size)
            when (response.status) {
                Status.SUCCESS -> {
                    setItemLoadingView(false)
                    _items.value = items.value?.plus(response.data!!.commentList)
                        ?: response.data!!.commentList
                    simplePageData = response.data!!.simplePage
                }
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
            setIsLoading(false)
        }
    }

    fun createComment() {
        if (isCommentInvalid()) {
            return
        }
        viewModelScope.launch {
            val response =
                createCommentRepository.createComment(
                    postId,
                    CreateCommentRequestData(comment.value!!)
                ).also { getPostDetail() }
            when (response.status) {
                Status.SUCCESS -> {
                    hideSoftInputHandler?.invoke()
                    initSimplePageData()
                    clearComment.invoke()
                    clearComments.invoke()
                    getItems()
                    showToast("댓글작성완료")
                }
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
        }
    }

    private fun isCommentInvalid(): Boolean {
        return comment.value.isNullOrBlank()
    }

    val likePostHandler = fun() { likePost() }
    val bookmarkHandler = fun() { setBookmark() }

    private fun likePost() {
        if (isGuestLogin()) {
            showToast("가입이 필요한 서비스입니다").also { _details.value = details.value }
            return
        }
        if (details.value?.detailedPost?.pressLike == true) {
            showToast("이미 좋아요를 눌렀습니다")
            _details.value = PostDetailData(details.value!!.detailedPost)
            return
        }
        viewModelScope.launch {
            val response = likePostRepository.likePost(postId)
            when (response.status) {
                Status.SUCCESS -> _details.value = PostDetailData(response.data!!.detailedPost)
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
        }
    }

    private fun setBookmark() {
        if (isGuestLogin()) {
            showToast("가입이 필요한 서비스입니다").also { _details.value = details.value }
            return
        }
        if (details.value?.detailedPost?.pressBookMark == true) {
            deleteBookmark()
        } else {
            addBookmark()
        }
    }

    private fun deleteBookmark() {
        viewModelScope.launch {
            val response = deleteBookmarkRepository.deleteBookmark(postId)
            when (response.status) {
                Status.SUCCESS -> {
                    details.value!!.detailedPost.copy(
                        pressBookMark = false
                    ).let { _details.value = PostDetailData(it) }
                        .also { showToast("북마크 취소됨") }
                }
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
        }
    }

    private fun addBookmark() {
        viewModelScope.launch {
            val response = createBookmarkRepository.addBookmark(postId)
            when (response.status) {
                Status.SUCCESS -> _details.value = PostDetailData(response.data!!.detailedPost)
                    .also { showToast("북마크 추가됨") }
                Status.ERROR -> Log.d(TAG, response.message!!)
            }
        }
    }

    private val clearComment = { comment.value = null }
    private val clearComments = { _items.value = null }

    private fun showToast(message: String) = toastMessageHandler?.invoke(message)

    companion object {
        private const val TAG = "DetailsViewModel"
    }
}