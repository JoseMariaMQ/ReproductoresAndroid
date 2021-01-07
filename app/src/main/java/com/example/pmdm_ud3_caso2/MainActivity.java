package com.example.pmdm_ud3_caso2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    //Variable con la que referenciaremos la vista
    private VideoView videoView;
    //Variable MediaController para los botones para manejar el video
    private MediaController mediaController;
    //Cadena con el enlace al video
    private final String video = "https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";

    private Button prev, playPause, next;
    private ProgressBar progressBar;

    private int pStatus = 0;
    private Handler handler = new Handler();

    private int duration;

    //Elementos para la reproducción con MediaPlayer
    private  MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;

    private Button playPause2;
    private Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Referenciamos con la interfaz gráfica
        videoView = (VideoView) findViewById(R.id.videoView);

        prev = (Button) findViewById(R.id.prev);
        playPause = (Button) findViewById(R.id.playPause);
        next = (Button) findViewById(R.id.next);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Inicializamos el MediaController
        mediaController = new MediaController(this);

        //Creamos los controles del video
        videoView.setMediaController(mediaController);
        //Pasamos por parametro el enlace al video
        videoView.setVideoURI(Uri.parse(video));
//        videoView.setVideoPath(video);
        //Iniciamos automaticamente el video
//        videoView.start();

        //Evento del botón playPause, inicia y pausa la reproducción
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprobamnamos el estado del video
                if(!videoView.isPlaying()) {
                    //Iniciamos video
                    videoView.start();
                    //Obtenemos la duración del video para la barra de progreso
                    duration = videoView.getDuration();
                    //Indicamos cual será el total de la barra de progreso
                    progressBar.setMax(duration);
                    progressBarUpdate();
                    //Cambiamos el texto al botón
                    playPause.setText("PAUSE");
                } else if(videoView.isPlaying()) {
                    //Pausamos video
                    videoView.pause();
                    //Cambiamos texto del botón
                    playPause.setText("PLAY");
                }
            }
        });

        //Evento del botón para avanzar la reproducción 10 segundos
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtenemos posición del video
                int pos = videoView.getCurrentPosition();
                //Añadimos 10 segundos
                videoView.seekTo(pos + 10000);
            }
        });
        //Evento del botón para retroceder la reproducción 5 segundos
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtenemos posición del video
                int pos = videoView.getCurrentPosition();
                //Quitamos 5 segundos
                videoView.seekTo(pos - 5000);
            }
        });

        //Método que actualiza la barra de progreso
//        progressBarUpdate();

        //Repoducción con MediaPlayer
        //Referenciamos con la vista SurfaceView
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        //Referenciamos botones
        playPause2 = (Button) findViewById(R.id.playPause2);
        stop = (Button) findViewById(R.id.stop);
        //Inicializamos MediaPlayer
        mediaPlayer = new MediaPlayer();
        // Obteniendo el objeto SurfaceHolder a partir del SurfaceView
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    //Método con el cual actualizaremos la barra de progreso
    public void progressBarUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Ejecutamos mientras el estado de la barra sea distinto a la duración del video
                while (pStatus != duration) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Actualizamos barra de progreso
                            progressBar.setProgress(pStatus);
                            //Comprobamos si deja de reproducirse video para cambiar texto al botón
                            if (!videoView.isPlaying()) playPause.setText("PLAY");
                        }
                    });
                    try {
                        //Lo comprobamos cada 100 milisegundos
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Actualizamos el valor del estado obteniendo el valor de progressBar
                    pStatus = videoView.getCurrentPosition();
                }
            }
        }).start();
    }

    //Método que se invoca al crear SurfaceView, y ya podemos trabajar para reproducir el video
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            mediaPlayer.setDisplay(holder);
            mediaPlayer.setDataSource(video);
            mediaPlayer.prepare();
//            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Eventos al pulsar botones de play, pause y stop
        //Implemento en el mismo botón la acción de PLAY y PAUSE cambiando texto del botón
        playPause2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprobamos si existe mediaPlayer y si está reproduciendose o no para
                // hacer la accion de PLAY o PAUSE
                if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    //Iniciamos reproducción
                    mediaPlayer.start();
                    //Cambiamos texto al botón
                    playPause2.setText("PAUSE");
                } else if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                    //Pausamos reproducción
                    mediaPlayer.pause();
                    //Cambiamos texto al botón
                    playPause2.setText("PLAY");
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    //Paramos reproducción
                    mediaPlayer.stop();
                    try {
                        //Volvemos a preparar el reproductor
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Movemos la posición al inicio para empezar de nuevo al pulsar PLAY
                    mediaPlayer.seekTo(0);
                    //Cambiamos texto al botón
                    playPause2.setText("PLAY");
                }
            }
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    //Aprovechamos este método que se invoca cuando se destruye SurfaceView para liberar
    // recursos del MediaPlayer con el método release()
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mediaPlayer.release();
    }
}