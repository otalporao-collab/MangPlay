package com.mangplay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private WebView navegador;
    private EditText campoLink;
    private SharedPreferences dados;
    private String linkAtual = "https://www.google.com";

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);

        dados = getSharedPreferences("MangPlay", MODE_PRIVATE);

        criarInterface();

        linkAtual = dados.getString(
                "ultimo_link",
                "https://www.google.com"
        );

        navegador.loadUrl(linkAtual);
    }

    private void criarInterface() {

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setBackgroundColor(Color.WHITE);

        TextView titulo = new TextView(this);
        titulo.setText("♾️ MangPlay");
        titulo.setTextSize(24);
        titulo.setTextColor(Color.BLACK);
        titulo.setGravity(Gravity.CENTER);
        titulo.setPadding(8, 14, 8, 14);

        principal.addView(titulo);

        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setPadding(6, 4, 6, 4);

        campoLink = new EditText(this);
        campoLink.setSingleLine(true);
        campoLink.setTextSize(14);
        campoLink.setHint("Introduz um link ou pesquisa");
        campoLink.setImeOptions(EditorInfo.IME_ACTION_GO);

        linha.addView(campoLink,
                new LinearLayout.LayoutParams(0, 52, 1));

        Button abrir = new Button(this);
        abrir.setText("Abrir");

        linha.addView(abrir);

        principal.addView(linha);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);

        Button voltar = new Button(this);
        voltar.setText("Voltar");

        Button avancar = new Button(this);
        avancar.setText("Avançar");

        Button guardar = new Button(this);
        guardar.setText("★");

        Button biblioteca = new Button(this);
        biblioteca.setText("Biblioteca");

        botoes.addView(voltar,
                new LinearLayout.LayoutParams(0, 48, 1));

        botoes.addView(avancar,
                new LinearLayout.LayoutParams(0, 48, 1));

        botoes.addView(guardar,
                new LinearLayout.LayoutParams(0, 48, 1));

        botoes.addView(biblioteca,
                new LinearLayout.LayoutParams(0, 48, 1));

        principal.addView(botoes);

        navegador = new WebView(this);

        WebSettings configuracoes = navegador.getSettings();

        configuracoes.setJavaScriptEnabled(true);
        configuracoes.setDomStorageEnabled(true);
        configuracoes.setLoadsImagesAutomatically(true);
        configuracoes.setSupportZoom(true);
        configuracoes.setBuiltInZoomControls(true);
        configuracoes.setDisplayZoomControls(false);

        navegador.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    android.graphics.Bitmap favicon) {

                campoLink.setText(url);
                linkAtual = url;
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                linkAtual = url;

                dados.edit()
                        .putString("ultimo_link", url)
                        .apply();
            }
        });

        navegador.setWebChromeClient(new WebChromeClient());

        principal.addView(navegador,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        setContentView(principal);

        abrir.setOnClickListener(v -> abrirLink());

        campoLink.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        abrirLink();
                        return true;
                    }

                    return false;
                });

        voltar.setOnClickListener(v -> {
            if (navegador.canGoBack()) {
                navegador.goBack();
            }
        });

        avancar.setOnClickListener(v -> {
            if (navegador.canGoForward()) {
                navegador.goForward();
            }
        });

        guardar.setOnClickListener(v -> guardarLink());

        biblioteca.setOnClickListener(v -> mostrarBiblioteca());
    }

    private void abrirLink() {

        String texto = campoLink.getText().toString().trim();

        if (texto.isEmpty()) {
            Toast.makeText(
                    this,
                    "Introduz um link primeiro.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (texto.matches("(?i)^https?://.*")) {
            navegador.loadUrl(texto);
        } else if (texto.contains(".") && !texto.contains(" ")) {
            navegador.loadUrl("https://" + texto);
        } else {
            navegador.loadUrl(
                    "https://www.google.com/search?q="
                            + Uri.encode(texto)
            );
        }
    }

    private void guardarLink() {

        if (linkAtual == null || linkAtual.isEmpty()) {
            return;
        }

        Set<String> links = new HashSet<>(
                dados.getStringSet(
                        "links_guardados",
                        new HashSet<String>()
                )
        );

        links.add(linkAtual);

        dados.edit()
                .putStringSet("links_guardados", links)
                .apply();

        Toast.makeText(
                this,
                "Link guardado!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void mostrarBiblioteca() {

        Set<String> links = dados.getStringSet(
                "links_guardados",
                new HashSet<String>()
        );

        if (links.isEmpty()) {
            Toast.makeText(
                    this,
                    "A biblioteca ainda está vazia.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] lista = links.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Biblioteca MangPlay")
                .setItems(lista, (dialogo, posicao) -> {
                    campoLink.setText(lista[posicao]);
                    navegador.loadUrl(lista[posicao]);
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    @Override
    public void onBackPressed() {

        if (navegador != null && navegador.canGoBack()) {
            navegador.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
