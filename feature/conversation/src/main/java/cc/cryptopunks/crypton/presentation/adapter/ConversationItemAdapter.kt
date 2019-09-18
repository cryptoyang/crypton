package cc.cryptopunks.crypton.presentation.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.crypton.conversation.R
import cc.cryptopunks.crypton.presentation.viewmodel.ConversationItemViewModel
import cc.cryptopunks.crypton.util.BaseFragment
import cc.cryptopunks.crypton.util.ext.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.conversation_item.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject

class ConversationItemAdapter @Inject constructor(
    private val baseFragment: BaseFragment
) :
    PagedListAdapter<ConversationItemViewModel, ConversationItemAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(parent.inflate(R.layout.conversation_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer,
        CoroutineScope by baseFragment + Job() {

        fun bind(model: ConversationItemViewModel?) {
            coroutineContext.cancelChildren()
            model?.apply {
                conversationLetter.apply {
                    text = letter.toString()
                    setBackgroundResource(avatarColor)
                }
                conversationTitleTextView.text = title
                launch {
                    lastMessageFlow.collect { message ->
                        lastMessageTextView.text = message.text
                        dateTextView.text = Date(message.timestamp).toString()
                    }
                }
            } ?: let {
                conversationLetter.apply {
                    text = ""
                    background = null
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<ConversationItemViewModel>() {
        override fun areItemsTheSame(
            oldItem: ConversationItemViewModel,
            newItem: ConversationItemViewModel
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ConversationItemViewModel,
            newItem: ConversationItemViewModel
        ) = areItemsTheSame(oldItem, newItem)
    }
}

suspend fun ConversationItemAdapter.bind(
    flow: Flow<PagedList<ConversationItemViewModel>>
) = flow.collect {
    submitList(it)
}