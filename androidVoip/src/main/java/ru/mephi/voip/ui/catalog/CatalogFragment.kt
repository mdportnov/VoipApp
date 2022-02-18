package ru.mephi.voip.ui.catalog

import android.annotation.SuppressLint
import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.SearchRecord
import ru.mephi.shared.data.model.SearchType
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.peek
import ru.mephi.voip.R
import ru.mephi.voip.databinding.FragmentCatalogBinding
import ru.mephi.voip.databinding.ToolbarCatalogBinding
import ru.mephi.voip.ui.catalog.adapter.BreadcrumbsAdapter
import ru.mephi.voip.ui.catalog.adapter.DataAdapter
import ru.mephi.voip.utils.*

class CatalogFragment : Fragment(),
    OnRefreshListener, LifecycleOwner {
    private lateinit var unitsAdapter: DataAdapter
    private lateinit var breadcrumbsAdapter: BreadcrumbsAdapter
    private val viewModel: CatalogViewModel by inject()
    private lateinit var binding: FragmentCatalogBinding
    private lateinit var toolbarBinding: ToolbarCatalogBinding
    private var searchType = SearchType.USERS

    private val unitListUpdateObserver: Observer<Stack<UnitM>> =
        Observer<Stack<UnitM>> {
            retrieveCatalog()
        }

    private val breadcrumbsUpdateObserver: Observer<Stack<UnitM>> =
        Observer<Stack<UnitM>> {
            retrieveCatalog()
        }

    private fun initMainPageObserver() {
        viewModel.goNext(getString(R.string.init_code_str))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogBinding.inflate(inflater, container, false)

        toolbarBinding = binding.toolbarCatalog

        if (viewModel.catalogStack.isEmpty())
            initMainPageObserver()

        return binding.root
    }

    override fun onRefresh() {
        refreshData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.goBack()
            scrollToPosition()

            viewModel.dismissProgressBar()

            if (viewModel.catalogStack.isEmpty())
                requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        retrieveCatalog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        initEventObservers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        if (isOnline(requireContext())) {
            if (viewModel.catalogStack.isNullOrEmpty())
                initMainPageObserver()
        } else
            showSnackBar(binding.root, getString(R.string.update_is_not_impossible))
        unitsAdapter.notifyDataSetChanged()
        binding.swiperefresh.isRefreshing = false
    }

    private fun retrieveCatalog() {
        if (viewModel.catalogStack.isNotEmpty()) {
            unitsAdapter.changeData(viewModel.catalogStack.peek()!!)
            breadcrumbsAdapter.changeData(viewModel.breadcrumbStack.toMutableList())
        }
    }

    private fun scrollToPosition(toStart: Boolean = false) {
        if (toStart)
            (binding.rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        else
            viewModel.catalogStack.lastOrNull()?.let {
                it.scrollPosition?.let { pos ->
                    (binding.rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        pos, 0
                    )
                }
            }
    }

    private fun performSearch(query: String) {
        // при нажатии на enter (search) на клавиатуре
        requireActivity().hideKeyboard()
        toolbarBinding.searchView.clearFocus()

        viewModel.breadcrumbStack.lastOrNull()?.let {
            if (it.shortname != query) {
                if (searchType == SearchType.USERS) {
                    viewModel.search(query, SearchType.USERS)
                    retrieveCatalog()
                }
                if (searchType == SearchType.UNITS) {
                    viewModel.search(query, SearchType.UNITS)
                    retrieveCatalog()
                }
            }
        }
    }

    private fun initEventObservers() {
        viewModel.eventsFlow.onEach {
            when (it) {
                is CatalogViewModel.Event.ShowSnackBar -> {
                    showSnackBar(binding.root, it.text)
                }
                is CatalogViewModel.Event.ShowToast -> {}
                CatalogViewModel.Event.ProgressBar.Dismiss -> {
                    binding.rv.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                CatalogViewModel.Event.ProgressBar.Show -> {
                    binding.rv.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
                is CatalogViewModel.Event.ScrollRvTo -> {
                    if (it.pos == 0)
                        scrollToPosition(true)
                    else
                        scrollToPosition()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfig = AppBarConfiguration(navController.graph)
        val navHostFragment = NavHostFragment.findNavController(this)

        NavigationUI.setupWithNavController(toolbarBinding.toolbar, navHostFragment, appBarConfig)
        (activity as AppCompatActivity).setSupportActionBar(toolbarBinding.toolbar)
    }

    //https://medium.com/mindorks/create-a-network-sensing-activity-in-android-614a1fa62a22
    private fun initViews() {
        toolbarBinding.switchSearchType.setCheckedChangeListener { current ->
            if (current == IconSwitch.Checked.LEFT) {
                searchType = SearchType.USERS
                toolbarBinding.searchView.queryHint = getString(R.string.search_of_appointments)
            } else {
                searchType = SearchType.UNITS
                toolbarBinding.searchView.queryHint = getString(R.string.search_of_units)
            }
        }

        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.item_label)

        val cursorAdapter = SimpleCursorAdapter(
            context,
            R.layout.search_item,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        toolbarBinding.searchView.suggestionsAdapter = cursorAdapter

        toolbarBinding.searchView.apply {
            queryHint = getString(R.string.search_of_appointments)
            setOnQueryTextFocusChangeListener { _: View, isFocused: Boolean ->
                if (isFocused) {
                    toolbarBinding.toolbarTitle.visibility = View.GONE
                    toolbarBinding.logoLeftImage.visibility = View.GONE
                } else {
                    toolbarBinding.toolbarTitle.visibility = View.VISIBLE
                    toolbarBinding.logoLeftImage.visibility = View.VISIBLE
                    toolbarBinding.switchSearchType.visibility = View.VISIBLE

                    toolbarBinding.searchView.onActionViewCollapsed()
                }
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    val cursor =
                        MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
                    query?.let {
                        viewModel.getSearchRecords().map { it.name }
                            .forEachIndexed { index, suggestion ->
                                if (suggestion.contains(query, true))
                                    cursor.addRow(arrayOf(index, suggestion))
                            }
                    }
                    cursorAdapter.changeCursor(cursor)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    if (isOnline(appContext))
                        if (query.length >= 3) {
                            if (isLetters(query)) {
                                performSearch(query)
                                viewModel.addSearchRecord(SearchRecord(null, query, searchType))
                            } else
                                toast(getString(R.string.wrong_query))
                        } else
                            toast(getString(R.string.enter_longer_query))
                    else
                        toast(getString(R.string.connection_lost))
                    return true
                }
            })

            // Установка слушателя нажатия на предложку
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    requireActivity().hideKeyboard()
                    val cursor =
                        toolbarBinding.searchView.suggestionsAdapter.getItem(position) as Cursor
                    val selection =
                        cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
                    toolbarBinding.searchView.setQuery(selection, false)
                    performSearch(selection)
                    viewModel.addSearchRecord(SearchRecord(null, selection, searchType))
                    return true
                }
            })
        }

        binding.swiperefresh.setOnRefreshListener(this)

        // Верхний Breadcrumb список
        binding.brv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        breadcrumbsAdapter = BreadcrumbsAdapter(context, viewModel)
        binding.brv.adapter = breadcrumbsAdapter
        breadcrumbsAdapter.changeData(viewModel.breadcrumbStack.toMutableList())

        val llm = LinearLayoutManager(context)
        binding.rv.layoutManager = llm
        unitsAdapter = DataAdapter(requireContext(), viewModel)

        binding.rv.adapter = unitsAdapter

        val dividerItemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider)
            ?.let { dividerItemDecoration.setDrawable(it) }
        binding.rv.addItemDecoration(dividerItemDecoration)

        viewModel.catalogLiveData.observe(viewLifecycleOwner, unitListUpdateObserver)
        viewModel.breadcrumbLiveData.observe(viewLifecycleOwner, breadcrumbsUpdateObserver)

        binding.brv.smoothScrollToPosition(breadcrumbsAdapter.itemCount)
    }
}