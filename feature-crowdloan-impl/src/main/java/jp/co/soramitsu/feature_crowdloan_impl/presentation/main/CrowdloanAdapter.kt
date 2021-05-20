package jp.co.soramitsu.feature_crowdloan_impl.presentation.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import jp.co.soramitsu.common.list.PayloadGenerator
import jp.co.soramitsu.common.list.resolvePayload
import jp.co.soramitsu.common.utils.inflateChild
import jp.co.soramitsu.common.utils.setTextOrHide
import jp.co.soramitsu.feature_crowdloan_impl.R
import jp.co.soramitsu.feature_crowdloan_impl.presentation.main.model.CrowdloanModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_crowdloan.view.itemCrowdloanIcon
import kotlinx.android.synthetic.main.item_crowdloan.view.itemCrowdloanParaDescription
import kotlinx.android.synthetic.main.item_crowdloan.view.itemCrowdloanParaName
import kotlinx.android.synthetic.main.item_crowdloan.view.itemCrowdloanParaRaised
import kotlinx.android.synthetic.main.item_crowdloan.view.itemCrowdloanTimeRemaining

class CrowdloanAdapter(
    private val imageLoader: ImageLoader
) : ListAdapter<CrowdloanModel, CrowdloanViewHolder>(CrowdloanDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrowdloanViewHolder {
        return CrowdloanViewHolder(imageLoader, parent.inflateChild(R.layout.item_crowdloan))
    }

    override fun onBindViewHolder(holder: CrowdloanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: CrowdloanViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)

        resolvePayload(holder, position, payloads) {
            when (it) {
                CrowdloanModel::timeRemaining -> holder.bindTimeRemaining(item)
            }
        }
    }
}

private object CrowdloanDiffCallback : DiffUtil.ItemCallback<CrowdloanModel>() {
    override fun areItemsTheSame(oldItem: CrowdloanModel, newItem: CrowdloanModel): Boolean {
        return oldItem.parachainId == newItem.parachainId
    }

    override fun areContentsTheSame(oldItem: CrowdloanModel, newItem: CrowdloanModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: CrowdloanModel, newItem: CrowdloanModel): Any? {
        return CrowdloanPayloadGenerator.diff(oldItem, newItem)
    }
}

private object CrowdloanPayloadGenerator : PayloadGenerator<CrowdloanModel>(
    CrowdloanModel::timeRemaining
)

class CrowdloanViewHolder(
    private val imageLoader: ImageLoader,
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: CrowdloanModel) = with(containerView) {
        itemCrowdloanParaDescription.setTextOrHide(item.description)
        itemCrowdloanParaName.text = item.title
        itemCrowdloanParaRaised.text = item.raised

        when (val icon = item.icon) {
            is CrowdloanModel.Icon.FromDrawable -> {
                itemCrowdloanIcon.setImageDrawable(icon.data)
            }
            is CrowdloanModel.Icon.FromLink -> {
                itemCrowdloanIcon.load(icon.data, imageLoader)
            }
        }

        bindTimeRemaining(item)
    }

    fun bindTimeRemaining(item: CrowdloanModel) {
        containerView.itemCrowdloanTimeRemaining.text = item.timeRemaining
    }
}
