package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    private LottieAnimationView inkAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_activity);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        inkAnimation = findViewById(R.id.inkAnimation);
        inkAnimation.setAnimation(R.raw.splash_circle);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                inkAnimation.setVisibility(View.VISIBLE);
                inkAnimation.setProgress(0f);
                inkAnimation.playAnimation();
            }
        }, 2500);


        inkAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Animation bắt đầu
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation kết thúc -> mở Home
                Intent intent = new Intent(
                        WelcomeActivity.this,
                        MainActivity.class
                );

                startActivity(intent);

                // Không cho quay lại WelcomeActivity bằng nút Back
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Animation bị hủy
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Animation lặp lại
            }
        });

        inkAnimation.playAnimation();
    }
}