package com.mazaady.task.presentation.fragments.main

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mazaady.task.R
import com.mazaady.task.adapters.OptionAdapter
import com.mazaady.task.adapters.PropertyAdapter
import com.mazaady.task.databinding.FragmentMainBinding
import com.mazaady.task.domain.IOBackPressed
import com.mazaady.task.domain.Property
import com.mazaady.task.domain.models.Category
import com.mazaady.task.domain.models.Children
import com.mazaady.task.domain.models.Option
import com.mazaady.task.domain.models.PropData
import com.mazaady.task.domain.repository.MainRepo
import com.mazaady.task.utils.Status
import com.mazaady.task.utils.afterTextChangedFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.stream.Collectors.groupingBy
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment() ,Property  {


    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mAdapter : PropertyAdapter
    private lateinit var optionAdapter : OptionAdapter
    private lateinit var properties:List<PropData>
      private var optionId: Option?=null
    private var parentId: Int?=null



    // private lateinit var mainRepo :MainRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, com.mazaady.task.R.layout.fragment_main, container, false)

        initialization()
        observeInsertionState()

        binding.submitBtn.setOnClickListener {

            findNavController().navigate(R.id.action_main_to_auction_details)
        }
        requireActivity().onBackPressedDispatcher.addCallback() {
            // Handle the back button event
           if (binding.includeLayout.isVisible)
           {
               binding.includeLayout.visibility=GONE
           }else{
              requireActivity().onBackPressed()
           }
        }

        return binding.root
    }




    private fun observeInsertionState()
    {
        /*
        collect data from SharedStateFlow to handle Single Events by param (reply=0) inside
        lifecycle coroutine scope with 'launchWhenStarted' to make flow aware with fragment lifecycle
         */
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getMainCategories.collect{
                    it ->
                when(it.status)
                {
                    Status.SUCCESS ->{
                        binding.progressBar.visibility=View.GONE

                        it.data?.let { it1 -> setCategories(it1.data.categories) }
                    }
                    Status.ERROR ->{
                        binding.progressBar.visibility=View.GONE

                     //   handleErrorStatus(it)
                    }
                    Status.LOADING ->{
                       binding.progressBar.visibility=View.VISIBLE


                    }
                }

            }


        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getProperties.collect{
                    it ->
                when(it.status)
                {
                    Status.SUCCESS ->{
                        binding.progressBar.visibility=View.GONE

                        properties= it.data!!.data

                        mAdapter.updateDayListItems(properties)

                    }
                    Status.ERROR ->{
                        binding.progressBar.visibility=View.GONE

                      //  handleErrorStatus(it)
                    }
                    Status.LOADING ->{
                          binding.progressBar.visibility=View.VISIBLE


                    }
                }

            }


        }


    }

    private fun handleErrorStatus()
    {
        /*
        binding.progressBar.visibility=View.GONE
        if (it.data!=null)
        {
            Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
        }

         */
    }

    private fun setCategories(categories :List<Category>)
    {


        val categoriesAdapter: ArrayAdapter<Any?>? = context?.let { ArrayAdapter<Any?>(it, com.mazaady.task.R.layout.spinner, categories) }
        binding.mainCategorySpinner.adapter=categoriesAdapter

        binding.mainCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    // Get the value selected by the user
                    // e.g. to store it as a field or immediately call a method
                    val category: Category = parent.selectedItem as Category
                    val subCategoriesAdapter: ArrayAdapter<Any?>? = context?.let { ArrayAdapter<Any?>(it, com.mazaady.task.R.layout.spinner, category.children) }

                    binding.subCategorySpinner.adapter=subCategoriesAdapter

        }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        binding.subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // Get the value selected by the user
            // e.g. to store it as a field or immediately call a method
            val child: Children = parent.selectedItem as Children
            viewModel.getProperties(child.id.toString())



        }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }
    private fun initialization()
    {

        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = context?.let { PropertyAdapter(arrayListOf(), it,this) }!!
        binding.propertiesRecyclerView.adapter =mAdapter

        viewModel.getMainCategories()
        binding.mainCategorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                    Toast.makeText(context,position.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

    }

    override fun onPropertyClick(propDetails: PropData) {

        binding.includeLayout.visibility=View.VISIBLE
        binding.searchableLayout.propertyName.text=propDetails.slug

        lifecycleScope.launch {
            binding.searchableLayout.seachEdit.afterTextChangedFlow().debounce(500).collect() {
                if (it != null) {

                    if (it.length >= 1)
                    {

                        propDetails.options.filter { option -> option.name .contains(it.toString()) || option.slug.contains(it.toString()) }.let { list ->  optionAdapter.updateOptionListItems(list)
                        }
                    }
                    else if (it.isEmpty()) {
                        propDetails.options.filter { option -> option.name.equals("other") }.apply {
                            if (this.isEmpty())
                            {
                                propDetails.options.add(0, Option(false,0,"other",propDetails.id,"other"))

                            }
                        }
                        optionAdapter.updateOptionListItems(propDetails.options)

                    }

                }
            }
        }



        binding.searchableLayout.recyclerProperties.layoutManager=LinearLayoutManager(context)
        optionAdapter= context?.let { OptionAdapter(arrayListOf(), it,this) }!!
        binding.searchableLayout.recyclerProperties.adapter= optionAdapter

        propDetails.options.filter { option -> option.name.equals("other") }.apply {
            if (this.isEmpty())
            {
                propDetails.options.add(0, Option(false,0,"other",propDetails.id,"other"))

            }
        }

        optionAdapter.updateOptionListItems(propDetails.options)
        binding.searchableLayout.closeBtn.setOnClickListener {

            binding.includeLayout.visibility=View.GONE

        }

    }

    override fun onOptionClick(option:Option,position:Int) {
        Log.e("po="+position,"po="+option.slug)

            properties.filter {
                    it ->
                it.id==option.parent

            }.let { it->
                if (it.isNotEmpty())
                {
                    it[0].selectedOption=option
                }
            }


        binding.includeLayout.visibility=View.GONE

        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = context?.let { PropertyAdapter(properties as ArrayList<PropData>, it,this) }!!
        binding.propertiesRecyclerView.adapter =mAdapter    }

    override fun getOptionChild(optionId :Option,parent :Int) {
        this.optionId=optionId

        Log.e("parentid","="+parent.toString())


        viewModel.getOptionChild(optionId)
        this.parentId=parent

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getOptionsChild.collect{
                    it ->
                when(it.status)
                {
                    Status.SUCCESS ->{
                        binding.progressBar.visibility=View.GONE
                        val propData= it.data?.data?.get(0)
                        Log.e("childresponse","="+parent.toString())

                        properties.filter {

                                property -> property.id.toString()==parent.toString()
                        }.let {
                                result ->
                            if (result.isNotEmpty())
                            {
                                Log.e("good","thank god")

                                if (propData != null) {
                                    Log.e("good","thank god")
                                    result[0].childOptions=propData.options
                                }
                            }
                        }


                        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(context)

                        mAdapter = context?.let { PropertyAdapter(properties as ArrayList<PropData>, it,this@MainFragment) }!!
                        binding.propertiesRecyclerView.adapter=mAdapter


                    }
                    Status.ERROR ->{
                        binding.progressBar.visibility=View.GONE

                    }
                    Status.LOADING ->{
                        binding.progressBar.visibility=View.VISIBLE


                    }
                }

            }


        }

    }






}