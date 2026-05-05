package com.example.raintodo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.raintodo.R
import com.example.raintodo.view.dialog.LoginDialog
import com.example.raintodo.viewmodel.DolistViewModel
import com.example.raintodo.viewmodel.SharedViewModel

class UserFragment : Fragment() {

    private lateinit var dolistViewModel: DolistViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var tvUsername: TextView
    private var loginDialog: LoginDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        dolistViewModel = ViewModelProvider(requireActivity())[DolistViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        tvUsername = view.findViewById(R.id.tv_username)
        val ivSwitch = view.findViewById<ImageView>(R.id.iv_switch)

        val currentUsername = dolistViewModel.getCurrentUsername()
        if (!currentUsername.isNullOrEmpty()) {
            tvUsername.text = currentUsername
        }

        // 切换用户
        ivSwitch.setOnClickListener {
            showSwitchUserDialog()
        }

        return view
    }

    private fun showSwitchUserDialog() {
        dolistViewModel.logout()
        sharedViewModel.clearData()
        tvUsername.text = "未登录"

        loginDialog = LoginDialog(requireContext())

        loginDialog?.onLoginClickListener = { username, password ->
            dolistViewModel.login(username, password)
        }

        loginDialog?.onRegisterClickListener = { username, password ->
            dolistViewModel.register(username, password)
        }

        loginDialog?.show()
        dolistViewModel.loginResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                com.example.raintodo.repository.Resource.Status.SUCCESS -> {
                    if (resource.data == true) {
                        loginDialog?.dismiss()
                        loginDialog = null

                        val username = dolistViewModel.getCurrentUsername()
                        tvUsername.text = username
                        dolistViewModel.getCurrentUserId()?.let { userId ->
                            sharedViewModel.init(userId)
                        }

                        requireActivity().findViewById<ViewPager2>(R.id.vp_main)
                            ?.setCurrentItem(0, true)
                    }
                }

                com.example.raintodo.repository.Resource.Status.ERROR -> {
                    // 登录失败
                }

                else -> {}
            }
        }

        dolistViewModel.registerResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                com.example.raintodo.repository.Resource.Status.SUCCESS -> {
                    if (resource.data == true) {
                        loginDialog?.onRegisterSuccess()
                    }
                }

                else -> {}
            }
        }
    }
}