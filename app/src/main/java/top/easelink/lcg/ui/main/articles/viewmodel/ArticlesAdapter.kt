package top.easelink.lcg.ui.main.articles.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import top.easelink.framework.base.BaseViewHolder
import top.easelink.lcg.R
import top.easelink.lcg.databinding.ItemArticleEmptyViewBinding
import top.easelink.lcg.ui.main.article.view.PostPreviewDialog
import top.easelink.lcg.ui.main.articles.viewmodel.ArticleEmptyItemViewModel.ArticleEmptyItemViewModelListener
import top.easelink.lcg.ui.main.model.OpenArticleEvent
import top.easelink.lcg.ui.main.source.model.Article
import java.lang.ref.WeakReference

class ArticlesAdapter(
    private var mListener: ArticleFetcher?
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var fragmentManager: WeakReference<FragmentManager>? = null
    private val mArticleList: MutableList<Article> = mutableListOf()

    override fun getItemCount(): Int {
        return if (mArticleList.isEmpty()) {
            1
        } else {
            mArticleList.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mArticleList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            if (position == mArticleList.size) {
                VIEW_TYPE_LOAD_MORE
            } else VIEW_TYPE_NORMAL
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> { ArticleViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_article_view
                        , parent, false
                    ))
            }
            VIEW_TYPE_LOAD_MORE -> LoadMoreViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_load_more_view
                        , parent, false
                    )
            )
            VIEW_TYPE_EMPTY -> {
                val emptyViewBinding =
                    ItemArticleEmptyViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                EmptyViewHolder(
                    emptyViewBinding
                )
            }
            else -> {
                val emptyViewBinding =
                    ItemArticleEmptyViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                EmptyViewHolder(
                    emptyViewBinding
                )
            }
        }
    }

    fun addItems(articleList: List<Article>) {
        mArticleList.addAll(articleList)
        notifyDataSetChanged()
    }

    fun clearItems() {
        mArticleList.clear()
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fragmentManager = WeakReference(fragmentManager)
    }

    inner class ArticleViewHolder internal constructor(private val view: View) :
        BaseViewHolder(view) {
        override fun onBind(position: Int) {
            val article = mArticleList[position]
            view.findViewById<View>(R.id.article_container).apply {
                setOnLongClickListener {
                    fragmentManager?.get()?.let {
                        PostPreviewDialog.newInstance(mArticleList[position].url)
                            .show(it, PostPreviewDialog.TAG)
                    }
                    true
                }
                setOnClickListener {
                    EventBus.getDefault().post(OpenArticleEvent(article.url))
                }
            }

            view.apply {
                findViewById<TextView>(R.id.title_text_view).text = article.title
                findViewById<TextView>(R.id.author_text_view).text = article.author
                findViewById<TextView>(R.id.date_text_view).text = article.date
                findViewById<TextView>(R.id.reply_and_view).text = article.let { "${it.reply} / ${it.view}" }
                findViewById<TextView>(R.id.origin).text = article.origin
            }
        }

    }

    inner class EmptyViewHolder internal constructor(private val mBinding: ItemArticleEmptyViewBinding) :
        BaseViewHolder(mBinding.root), ArticleEmptyItemViewModelListener {
        override fun onBind(position: Int) {
            val emptyItemViewModel = ArticleEmptyItemViewModel(this)
            mBinding.viewModel = emptyItemViewModel
        }

        override fun onRetryClick() {
            mListener?.fetchArticles(ArticleFetcher.FETCH_INIT)
        }

    }

    inner class LoadMoreViewHolder internal constructor(view: View) :
        BaseViewHolder(view) {
        override fun onBind(position: Int) {
            mListener?.fetchArticles(ArticleFetcher.FETCH_MORE)
        }
    }

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_NORMAL = 1
        private const val VIEW_TYPE_LOAD_MORE = 2
    }

}