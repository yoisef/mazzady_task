package com.mazaady.task.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mazaady.task.R
import com.mazaady.task.databinding.RowPropertyBinding
import com.mazaady.task.domain.Property
import com.mazaady.task.domain.models.Category
import com.mazaady.task.domain.models.Option
import com.mazaady.task.domain.models.PropData

import com.mazaady.task.utils.UserDiffCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


class PropertyAdapter @Inject constructor (var days: ArrayList<PropData>,var context1: Context,actions:Property) :  RecyclerView.Adapter<PropertyAdapter.SearchViewHolder>() {


    private lateinit var binding: RowPropertyBinding
    private var context: Context = context1
    private val actions: Property = actions



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {

        binding = RowPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SearchViewHolder(binding)

    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        Log.e("selectedopion","=="+ (days[position].selectedOption?.slug ))
        val city = days[position]
        holder.bind(city)


    }


    fun updateDayListItems(newDays: List<PropData>) {

        val diffCallback = UserDiffCallback(this.days, newDays)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.days.clear()
        this.days.addAll(newDays)
        diffResult.dispatchUpdatesTo(this)


    }


    inner class SearchViewHolder(private val binding: RowPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(propertyDetails: PropData) {
            binding.property = propertyDetails

            if (propertyDetails.selectedOption!=null)
            {

                Log.e("optionselected","selected"+ propertyDetails.selectedOption!!.slug.toString())
                binding.optionSelected.text= propertyDetails.selectedOption!!.slug
                if (propertyDetails.selectedOption!!.child)
                {
                    binding.childLayout.visibility=View.VISIBLE
                    actions.getOptionChild(propertyDetails.selectedOption!!,propertyDetails.id)


                }else{
                    binding.childLayout.visibility=View.GONE

                }


                if (propertyDetails.selectedOption!!.name.equals("other"))
                {
                    binding.otherLayout.visibility=View.VISIBLE
                }else{
                    binding.otherLayout.visibility=View.GONE

                }


            }
            if (propertyDetails.childOptions!=null)
            {
                Log.e("childoprion","selected"+ propertyDetails.childOptions!!.toString())

                val categoriesAdapter: ArrayAdapter<Any?>? = context?.let { propertyDetails.childOptions?.let { it1 -> ArrayAdapter<Any?>(it, com.mazaady.task.R.layout.spinner, it1.toList() )} }
                binding.childSpinner.adapter=categoriesAdapter

                binding.childSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    // Get the value selected by the user
                    // e.g. to store it as a field or immediately call a method


                }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            }
            binding.propertyLayout.setOnClickListener {

                actions.onPropertyClick(propertyDetails)
            }


            binding.propertyParentName.text = propertyDetails.slug
        }
    }
}

