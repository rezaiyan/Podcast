package com.hezaro.wall.feature.profile

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.EndlessLayoutManager
import kotlinx.android.synthetic.main.fragment_list.recyclerList
import kotlinx.android.synthetic.main.item_explore.view.downloadStatus
import kotlinx.android.synthetic.main.item_explore.view.logo
import kotlinx.android.synthetic.main.item_explore.view.podcaster
import kotlinx.android.synthetic.main.item_explore.view.title
import org.koin.android.ext.android.inject

class ListFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_list
    override fun tag(): String = this::class.java.simpleName
    private val activity: MainActivity by lazy { requireActivity() as MainActivity }

    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = ListFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(episodes) {
                (recyclerList.adapter as ListAdapter).updateList(it)
            }
        }
        recyclerList.apply {
            layoutManager = EndlessLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ListAdapter(mutableListOf()) { e, _ -> activity.openEpisodeInfo(e) }
        }

        vm.getEpisodes()

        if (activity.isPlayerOpen())
            liftList()
    }

    private fun liftList() {
        val params = recyclerList.layoutParams as FrameLayout.LayoutParams
        if (params.bottomMargin == 0) {
            val animator =
                ValueAnimator.ofInt(params.bottomMargin, resources.getDimension(R.dimen.mini_player_height).toInt())
            animator.addUpdateListener { valueAnimator ->
                params.bottomMargin = valueAnimator.animatedValue as Int
                recyclerList.requestLayout()
            }
            animator.duration = 100
            animator.start()
        }
    }

    inner class ListAdapter(private val list: MutableList<Episode>, private val onItemClick: (Episode, Int) -> Unit) :
        RecyclerView.Adapter<ListAdapter.ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false))

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: ListAdapter.ItemHolder, position: Int) {
            holder.bind(list[position])
        }

        fun updateList(it: MutableList<Episode>) {
            val count = itemCount
            list.addAll(it)
            notifyItemRangeInserted(count, itemCount)
        }

        inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bind(episode: Episode) {
                itemView.let {

                    episode.run {
                        if (playStatus == Status.IN_PROGRESS)
                            itemView.setBackgroundColor(itemView.context.resources.getColor(R.color.colorTextSecondary))
                        it.logo.load(cover)
                        if (isDownloaded == 1)
                            it.downloadStatus.progress = 0.74f
                        else it.downloadStatus.progress = 0.12f
                        it.title.text = title
                        it.podcaster.text = podcast.title
                        it.setOnClickListener { onItemClick(this, adapterPosition) }

                    }

                }
            }
        }
    }
}