package ry.tech.mtc.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ry.tech.mtc.R;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonBack).setOnClickListener(v -> {
            // Обработка нажатия кнопки "Назад"
            requireActivity().onBackPressed();
        });

        view.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            // Обработка нажатия кнопки редактирования
        });
    }
}