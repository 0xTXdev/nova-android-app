package jp.co.soramitsu.common.base

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.utils.Event
import jp.co.soramitsu.common.utils.EventObserver
import jp.co.soramitsu.common.utils.onTextChanged
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    @Inject protected open lateinit var viewModel: T

    private val observables = mutableListOf<LiveData<*>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inject()
        initViews()
        subscribe(viewModel)

        viewModel.errorLiveData.observeEvent(::showError)

        viewModel.errorWithTitleLiveData.observeEvent {
            showErrorWithTitle(it.first, it.second)
        }

        viewModel.messageLiveData.observeEvent(::showMessage)
    }

    protected fun showError(errorMessage: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.common_error_general_title)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.common_ok) { _, _ -> }
            .show()
    }

    protected fun showErrorWithTitle(title: String, errorMessage: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.common_ok) { _, _ -> }
            .show()
    }

    protected fun showMessage(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        observables.forEach { it.removeObservers(this) }
        super.onDestroyView()
    }

    @Suppress("unchecked_cast")
    protected fun <V : Any?> observe(source: LiveData<V>, observer: Observer<V>) {
        source.observe(viewLifecycleOwner, observer as Observer<in Any?>)
        observables.add(source)
    }

    protected inline fun <V> LiveData<Event<V>>.observeEvent(crossinline observer: (V) -> Unit) {
        observe(viewLifecycleOwner, EventObserver {
            observer.invoke(it)
        })
    }

    protected inline fun <V> LiveData<V>.observe(crossinline observer: (V) -> Unit) {
        observe(viewLifecycleOwner, Observer {
            observer.invoke(it)
        })
    }

    abstract fun initViews()

    abstract fun inject()

    abstract fun subscribe(viewModel: T)
}