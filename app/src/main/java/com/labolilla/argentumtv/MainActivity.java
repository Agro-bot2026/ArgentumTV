package com.labolilla.argentumtv;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerCanales;
    private RecyclerView recyclerCategorias;
    private SeccionAdapter seccionAdapter;
    private CategoriaAdapter categoriaAdapter;
    private List<JSONObject> listaCanales = new ArrayList<>();
    private Map<String, List<JSONObject>> mapaCategorias = new LinkedHashMap<>();
    private String categoriaActual = null;
    private EditText buscador;
    private DrawerLayout drawerLayout;
    private PlayerView playerView;
    private ExoPlayer player;
    private Button btnCerrarReproductor;
    private Button btnFullscreen;
    private Button btnEnviarCast;
    private MediaRouteButton mediaRouteButton;
    private CastContext castContext;
    private JSONObject canalActual;
    private View reproductorContainer;
    private boolean enFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Las teclas físicas de volumen controlan el volumen del VIDEO (media),
        // no el tono de llamada
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerCanales = findViewById(R.id.recycler_canales);
        recyclerCategorias = findViewById(R.id.recycler_categorias);
        buscador = findViewById(R.id.buscador);
        playerView = findViewById(R.id.player_view);
        btnCerrarReproductor = findViewById(R.id.btn_cerrar_reproductor);
        btnFullscreen = findViewById(R.id.btn_fullscreen);
        btnEnviarCast = findViewById(R.id.btn_enviar_cast);
        mediaRouteButton = findViewById(R.id.media_route_button);
        reproductorContainer = findViewById(R.id.reproductor_container);

        recyclerCanales.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategorias.setLayoutManager(new LinearLayoutManager(this));

        // El adapter de secciones se arma en filtrarCanales() tras cargar los datos
        seccionAdapter = new SeccionAdapter(this, new LinkedHashMap<>(), new ArrayList<>(),
                canal -> reproducirCanal(canal));
        recyclerCanales.setAdapter(seccionAdapter);

        categoriaAdapter = new CategoriaAdapter(this, new ArrayList<>(), categoria -> {
            categoriaActual = categoria;
            filtrarCanales();
            drawerLayout.closeDrawer(drawerLayout.findViewById(R.id.drawer_contenido));
        });
        recyclerCategorias.setAdapter(categoriaAdapter);

        btnCerrarReproductor.setOnClickListener(v -> cerrarReproductor());
        btnFullscreen.setOnClickListener(v -> toggleFullscreen());
        btnEnviarCast.setOnClickListener(v -> enviarAlCast());

        buscador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filtrarCanales(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Configurar Cast
        try {
            castContext = CastContext.getSharedInstance(this);
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        } catch (Exception e) {
            mediaRouteButton.setVisibility(View.GONE);
        }

        cargarCanales();
    }

    private void cargarCanales() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL("http://157.250.202.243:8083/canales.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) sb.append(linea);
                br.close();

                JSONArray jsonArray = new JSONArray(sb.toString());
                List<JSONObject> canales = new ArrayList<>();
                Map<String, List<JSONObject>> categorias = new LinkedHashMap<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject canal = jsonArray.getJSONObject(i);
                    canales.add(canal);
                    String cat = canal.optString("categoria", "General");
                    if (!categorias.containsKey(cat)) categorias.put(cat, new ArrayList<>());
                    categorias.get(cat).add(canal);
                }

                runOnUiThread(() -> {
                    listaCanales.clear();
                    listaCanales.addAll(canales);
                    mapaCategorias.clear();
                    mapaCategorias.putAll(categorias);
                    List<String> listaCategorias = new ArrayList<>(mapaCategorias.keySet());
                    categoriaAdapter = new CategoriaAdapter(MainActivity.this, listaCategorias, categoria -> {
                        categoriaActual = categoria;
                        filtrarCanales();
                        drawerLayout.closeDrawer(drawerLayout.findViewById(R.id.drawer_contenido));
                    });
                    recyclerCategorias.setAdapter(categoriaAdapter);
                    filtrarCanales();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al cargar canales", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Filtra por búsqueda y/o categoría, y muestra el resultado como
    // secciones horizontales (estilo Apple TV/Netflix)
    private void filtrarCanales() {
        String query = buscador.getText().toString().trim().toLowerCase();

        // Agrupar canales que coinciden, por categoría
        Map<String, List<JSONObject>> secciones = new LinkedHashMap<>();
        for (JSONObject canal : listaCanales) {
            String nombre = canal.optString("nombre", "").toLowerCase();
            String cat = canal.optString("categoria", "General");
            boolean coincideNombre = nombre.contains(query);
            boolean coincideCat = (categoriaActual == null || cat.equals(categoriaActual));
            if (coincideNombre && coincideCat) {
                if (!secciones.containsKey(cat)) secciones.put(cat, new ArrayList<>());
                secciones.get(cat).add(canal);
            }
        }

        // Ordenar categorías: las que tengan canales con logo primero, y por cantidad
        List<String> orden = new ArrayList<>(secciones.keySet());
        orden.sort((a, b) -> {
            long logosA = secciones.get(a).stream().filter(c -> !c.optString("logo", "").isEmpty()).count();
            long logosB = secciones.get(b).stream().filter(c -> !c.optString("logo", "").isEmpty()).count();
            if (logosA != logosB) return Long.compare(logosB, logosA);
            return Integer.compare(secciones.get(b).size(), secciones.get(a).size());
        });

        seccionAdapter = new SeccionAdapter(this, secciones, orden, canal -> reproducirCanal(canal));
        recyclerCanales.setAdapter(seccionAdapter);
    }

    private void reproducirCanal(JSONObject canal) {
        String url = canal.optString("url", "");
        if (url.isEmpty()) {
            Toast.makeText(this, "Canal no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        canalActual = canal;

        reproductorContainer.setVisibility(View.VISIBLE);
        btnEnviarCast.setVisibility(View.VISIBLE);

        if (player == null) {
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(10000)
                    .setReadTimeoutMs(10000);

            // Renderers con soporte de extensiones (FFmpeg para audio MP2/AC3)
            androidx.media3.exoplayer.DefaultRenderersFactory renderersFactory =
                    new androidx.media3.exoplayer.DefaultRenderersFactory(this);
            renderersFactory.setExtensionRendererMode(
                    androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

            player = new ExoPlayer.Builder(this, renderersFactory).build();
            playerView.setPlayer(player);
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    // No cerrar, solo avisar; el usuario puede usar "Enviar a TV"
                    Toast.makeText(MainActivity.this, "No se pudo reproducir localmente. Probá con el botón Enviar a TV", Toast.LENGTH_LONG).show();
                }
            });
        }

        try {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .build();
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir el canal. Podés intentar transmitirlo a la TV", Toast.LENGTH_LONG).show();
        }
    }

    private void enviarAlCast() {
        if (canalActual == null) {
            Toast.makeText(this, "Primero reproducí un canal", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            CastSession castSession = (castContext != null) ? castContext.getSessionManager().getCurrentCastSession() : null;
            if (castSession == null || !castSession.isConnected()) {
                Toast.makeText(this, "Conectá primero al Chromecast con el botón de arriba", Toast.LENGTH_LONG).show();
                return;
            }

            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                Toast.makeText(this, "No se pudo conectar al Chromecast", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = canalActual.optString("url", "");
            String nombre = canalActual.optString("nombre", "Canal");
            String logo = canalActual.optString("logo", "");

            MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            metadata.putString(MediaMetadata.KEY_TITLE, nombre);
            if (!logo.isEmpty()) metadata.addImage(new WebImage(Uri.parse(logo)));

            MediaInfo mediaInfo = new MediaInfo.Builder(url)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("application/x-mpegurl")
                    .setMetadata(metadata)
                    .build();

            // Cargar el canal en la TV y ESPERAR la respuesta real
            remoteMediaClient.load(mediaInfo, true, 0)
                    .setResultCallback(new com.google.android.gms.common.api.ResultCallback<RemoteMediaClient.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaClient.MediaChannelResult result) {
                            runOnUiThread(() -> {
                                if (result != null && result.getStatus().isSuccess()) {
                                    Toast.makeText(MainActivity.this, "📺 Transmitiendo: " + nombre, Toast.LENGTH_SHORT).show();
                                    // Transmisión exitosa: cortar la reproducción local
                                    // y volver a la lista para elegir otro canal
                                    cerrarReproductor();
                                } else {
                                    String detalle = (result != null && result.getStatus().getStatusCode() != 0)
                                            ? " (" + result.getStatus().getStatusCode() + ")"
                                            : "";
                                    Toast.makeText(MainActivity.this,
                                            "La TV no pudo reproducir el canal" + detalle + ". Probá con otro canal o verificá que la URL sea accesible",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error al transmitir", Toast.LENGTH_SHORT).show();
        }
    }

    private void cerrarReproductor() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        playerView.setPlayer(null);
        reproductorContainer.setVisibility(View.GONE);
        btnEnviarCast.setVisibility(View.GONE);
        salirFullscreen();
    }

    // Expandir el video a pantalla completa horizontal
    private void toggleFullscreen() {
        if (!enFullscreen) {
            enFullscreen = true;
            btnFullscreen.setText("🗗");
            // Forzar orientación horizontal
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // Ocultar barras del sistema (modo inmersivo)
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            salirFullscreen();
        }
    }

    private void salirFullscreen() {
        if (!enFullscreen) return;
        enFullscreen = false;
        btnFullscreen.setText("⛶");
        // Restaurar orientación automática
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        // Mostrar barras del sistema de nuevo
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    // Botón "atrás": si el reproductor está abierto, primero cerrarlo
    // (sin esto, Android cierra toda la app al volver)
    @Override
    public void onBackPressed() {
        if (enFullscreen) {
            salirFullscreen();
        } else if (reproductorContainer != null && reproductorContainer.getVisibility() == View.VISIBLE) {
            cerrarReproductor();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
