package com.mazaady.task.presentation.fragments.auctionDetails

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mazaady.task.R
import com.mazaady.task.adapters.BiddersAdapter
import com.mazaady.task.adapters.PropertyAdapter
import com.mazaady.task.databinding.FragmentAuctionDetailsBinding
import com.mazaady.task.databinding.FragmentMainBinding
import com.mazaady.task.domain.models.Category
import com.mazaady.task.domain.models.Children
import com.mazaady.task.domain.models.PropData
import com.mazaady.task.domain.repository.MainRepo
import com.mazaady.task.presentation.fragments.main.MainViewModel
import com.mazaady.task.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AuctionFragment : Fragment() {


    private lateinit var binding: FragmentAuctionDetailsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var biddersAdapter : BiddersAdapter
   // private lateinit var mainRepo :MainRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, com.mazaady.task.R.layout.fragment_auction_details, container, false)

     //   initialization()
      //  observeInsertionState()


        setupBiddersRecyclerView()

        return binding.root
    }

    private fun setupBiddersRecyclerView()
    {
        binding.biddersRecyclerView.layoutManager=LinearLayoutManager(context)
        biddersAdapter = context?.let { BiddersAdapter(it) }!!
        binding.biddersRecyclerView.adapter =biddersAdapter

    }



}