package ry.tech.mtc;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import ry.tech.mtc.fragments.*;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_first) {
                selectedFragment = new HomeFragment();
                animateNavigation(itemId);
            } else if (itemId == R.id.nav_second) {
                selectedFragment = new ManagementFragment();
                animateNavigation(itemId);
            } else if (itemId == R.id.nav_third) {
                selectedFragment = new ProfileFragment();
                animateNavigation(itemId);
            }

            if (selectedFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                transaction.replace(R.id.fragment_container, selectedFragment);
                transaction.commit();
            }

            return true;
        });

        // Установка начального фрагмента
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_first);
        }
    }

    private void animateNavigation(int itemId) {
        View view = bottomNavigationView.findViewById(itemId);
        if (view != null) {
            AnimationSet animationSet = new AnimationSet(true);

            // Анимация увеличения
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    1.0f, 1.2f, // от нормального размера до увеличенного
                    1.0f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(150); // уменьшили длительность
            scaleAnimation.setInterpolator(new OvershootInterpolator(2.0f));

            // Анимация возврата к исходному размеру
            ScaleAnimation reverseAnimation = new ScaleAnimation(
                    1.2f, 1.0f,
                    1.2f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            reverseAnimation.setDuration(100);
            reverseAnimation.setStartOffset(150); // начнется после первой анимации
            reverseAnimation.setInterpolator(new OvershootInterpolator(1.5f));

            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(reverseAnimation);
            view.startAnimation(animationSet);
        }
    }
}
