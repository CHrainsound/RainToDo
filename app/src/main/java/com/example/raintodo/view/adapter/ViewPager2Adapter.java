package com.example.raintodo.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.raintodo.view.fragment.MainFragment;
import com.example.raintodo.view.fragment.UserFragment;

public class ViewPager2Adapter extends FragmentStateAdapter {
    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new MainFragment();
            case 1:
                return new UserFragment();
            default:
                return new MainFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
