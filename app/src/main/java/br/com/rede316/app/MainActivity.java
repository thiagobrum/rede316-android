package br.com.rede316.app;

import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class MainActivity extends AppCompatActivity {

    private ListenableFuture<MediaController> controllerFuture;
    private MediaController controller;

    private ImageButton btnPlay;
    private ImageButton btnBrasil, btnPlanalto, btnBahia;
    private TextView tvTitle, tvSub, tvNow, tvStatus;
    private View statusDot;
    private String currentStation = "brasil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dark status bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#0A2E14"));

        setContentView(R.layout.activity_main);

        // Bind views
        btnPlay = findViewById(R.id.btnPlay);
        btnBrasil = findViewById(R.id.btnBrasil);
        btnPlanalto = findViewById(R.id.btnPlanalto);
        btnBahia = findViewById(R.id.btnBahia);
        tvTitle = findViewById(R.id.tvTitle);
        tvSub = findViewById(R.id.tvSub);
        tvNow = findViewById(R.id.tvNow);
        tvStatus = findViewById(R.id.tvStatus);
        statusDot = findViewById(R.id.statusDot);

        // Play button
        btnPlay.setOnClickListener(v -> {
            if (controller == null) return;
            if (controller.isPlaying()) {
                controller.pause();
            } else {
                if (controller.getCurrentMediaItem() == null) {
                    loadStation("brasil");
                }
                controller.play();
            }
        });

        // Station buttons
        btnBrasil.setOnClickListener(v -> loadStation("brasil"));
        btnPlanalto.setOnClickListener(v -> loadStation("planalto"));
        btnBahia.setOnClickListener(v -> loadStation("bahia"));

        // Highlight default
        updateStationButtons("brasil");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to RadioService
        SessionToken token = new SessionToken(this,
                new ComponentName(this, RadioService.class));
        controllerFuture = new MediaController.Builder(this, token).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                controller = controllerFuture.get();
                setupPlayerListener();
                updateUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    protected void onStop() {
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
        }
        super.onStop();
    }

    private void loadStation(String id) {
        if (controller == null) return;
        RadioStation station = RadioStation.findById(id);
        currentStation = id;
        controller.setMediaItem(station.toMediaItem());
        controller.prepare();
        controller.play();
        updateStationButtons(id);
    }

    private void setupPlayerListener() {
        if (controller == null) return;
        controller.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateUI();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                updateUI();
            }

            @Override
            public void onMediaItemTransition(MediaItem item, int reason) {
                if (item != null) {
                    currentStation = item.mediaId;
                    updateStationButtons(currentStation);
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (controller == null) return;
        runOnUiThread(() -> {
            boolean playing = controller.isPlaying();
            int state = controller.getPlaybackState();

            // Play button icon
            btnPlay.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);

            // Station info
            RadioStation station = RadioStation.findById(currentStation);
            tvTitle.setText(station.title);
            tvSub.setText(station.subtitle);

            // Status
            if (playing) {
                tvNow.setText("♪ " + getNowPlaying());
                tvNow.setTextColor(Color.parseColor("#34C759"));
                tvStatus.setText("AO VIVO");
                tvStatus.setTextColor(Color.parseColor("#FF3B30"));
                tvStatus.setVisibility(View.VISIBLE);
                statusDot.setVisibility(View.VISIBLE);
            } else if (state == Player.STATE_BUFFERING) {
                tvNow.setText("Conectando...");
                tvNow.setTextColor(Color.parseColor("#F0B429"));
                tvStatus.setText("CONECTANDO");
                tvStatus.setTextColor(Color.parseColor("#F0B429"));
                tvStatus.setVisibility(View.VISIBLE);
                statusDot.setVisibility(View.GONE);
            } else {
                tvNow.setText("Aperte o play para ouvir");
                tvNow.setTextColor(Color.parseColor("#AABFAE"));
                tvStatus.setVisibility(View.GONE);
                statusDot.setVisibility(View.GONE);
            }
        });
    }

    private void updateStationButtons(String activeId) {
        btnBrasil.setAlpha(activeId.equals("brasil") ? 1.0f : 0.4f);
        btnPlanalto.setAlpha(activeId.equals("planalto") ? 1.0f : 0.4f);
        btnBahia.setAlpha(activeId.equals("bahia") ? 1.0f : 0.4f);
    }

    private String getNowPlaying() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        int min = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE);
        float h = hour + min / 60f;

        String[][] schedule = {
                {"0", "Madrugada da Paz"},
                {"6", "Manhã com Deus"},
                {"7.5", "Nossa Pátria Brasil"},
                {"9", "Bom Dia Rede 3.16"},
                {"10", "Quadro Especial"},
                {"13", "Playlist na Rede 3.16"},
                {"14", "Tarde Viva"},
                {"15", "Quadro Especial"},
                {"17", "Programa Especial"},
                {"18", "Sala de Oração"},
                {"19", "Boa Noite na 3.16"},
        };

        String name = schedule[0][1];
        for (int i = schedule.length - 1; i >= 0; i--) {
            if (h >= Float.parseFloat(schedule[i][0])) {
                name = schedule[i][1];
                break;
            }
        }
        return name;
    }
}
