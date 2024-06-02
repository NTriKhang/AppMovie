package com.example.appmovie.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appmovie.Model.FavourFilm;
import com.example.appmovie.Model.Movie;
import com.example.appmovie.Model.User;
import com.example.appmovie.Model.episode;
import com.example.appmovie.Model.episodes;
import com.example.appmovie.R;
import com.example.appmovie.View.Adapter.ActorRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.bundle.BundledQueryOrBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MovieDetail extends AppCompatActivity {
    ArrayList<String> lstActor = new ArrayList<>();
    episodes epis = new episodes();

    Movie movie = new Movie();
    RecyclerView rvActor;
    ActorRecyclerAdapter adapter;
    String slug;
    ImageView thumbImg;
    TextView tvInfoMovie, tvCategory, tvTime, tvTitle, tvContent, tvCountry, tvDirector, tvLastUpdate;
    Button btnEpisodeCurrent, btnShared, btnWatchNow;
    ToggleButton btnFavorite;
    String url = "https://phimapi.com/phim/cuoc-hen-song-con-phan-5";
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    CollectionReference node_ref = firestore.collection("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_movie_detail);
        addControls();
        getDataMovie(url);
        addEvents();

    }
    void loadAdapterActor() {
        rvActor.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        adapter = new ActorRecyclerAdapter(lstActor);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvActor.setLayoutManager(mLayoutManager);
        rvActor.setItemAnimator(new DefaultItemAnimator());
        rvActor.setAdapter(adapter);
    }
    void addControls() {
        rvActor = (RecyclerView)findViewById(R.id.rvActor);
        thumbImg = (ImageView) findViewById(R.id.thumbImg);
        tvInfoMovie = (TextView) findViewById(R.id.tvInfoMovie);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvCountry = (TextView) findViewById(R.id.tvCountry);
        tvDirector = (TextView) findViewById(R.id.tvDirector);
        tvLastUpdate = (TextView) findViewById(R.id.tvLastUpdate);
        btnEpisodeCurrent = (Button) findViewById(R.id.btnEpisodeCurrent);
        btnShared = (Button) findViewById(R.id.btnShared);
        btnWatchNow = (Button) findViewById(R.id.btnWatchNow);
        btnFavorite = (ToggleButton) findViewById(R.id.btnFavorite);
    }
    void addEvents() {
        btnShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Your link film");
                startActivity(Intent.createChooser(intent, "Share via"));
            }
        });
        btnFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    addFavourFilm();
                }
                else {
                    deleteFavourFilm();
                }
            }
        });
        btnWatchNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovieDetail.this, WatchMovie.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Episodes", epis);
                intent.putExtra("EpisodesPakage", bundle);
                startActivity(intent);
            }
        });
    }
    public void getDataMovie(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(MovieDetail.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            parseJsonData(response);
                        } catch (JSONException | ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MovieDetail.this, "Error when loading infor movie", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(stringRequest);
    }
    void addFavourFilm() {
        User user = new User(
                "caohieeu",
                "image",
                "caohieeu2@gmail.com",
                new ArrayList<FavourFilm>()
        );
        user.Favour_film.add(new FavourFilm(
                movie.id,
                movie.slug,
                movie.origin_name,
                movie.poster_url
        ));
        node_ref.document("user_id_1")
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MovieDetail.this, "Đã thêm vào phim yêu thích", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MovieDetail.this, "Fail", Toast.LENGTH_LONG).show();
                    }
                });
    }
    void deleteFavourFilm() {
        User user = new User(
                "caohieeu",
                "image",
                "caohieeu2@gmail.com",
                new ArrayList<FavourFilm>()
        );
        FavourFilm film = new FavourFilm(
                movie.id,
                movie.slug,
                movie.origin_name,
                movie.poster_url
        );
        user.Favour_film.remove(film);
        node_ref.document("user_id_1")
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MovieDetail.this, "Đã hủy phim yêu thích", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MovieDetail.this, "Fail", Toast.LENGTH_LONG).show();
                    }
                });
    }
    public void parseJsonData(String response) throws JSONException, ParseException {
        movie.category = new ArrayList<>();
        movie.actor = new ArrayList<>();
        movie.country = new ArrayList<>();
        movie.director = new ArrayList<>();
        epis.server_data = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(response);
        JSONArray episodeArray = jsonObject.getJSONArray("episodes");

        for(int i = 0; i < episodeArray.length(); i++) {
            JSONObject episodeObj = episodeArray.getJSONObject(i);
            epis.server_name = episodeObj.getString("server_name");
            JSONArray serverDataArray = new JSONArray(episodeObj.getString("server_data"));

            for (int j = 0; j < serverDataArray.length(); j++) {
                JSONObject episode = serverDataArray.getJSONObject(j);
                episode epi = new episode(
                        episode.getString("filename"),
                        episode.getString("link_embed"),
                        episode.getString("link_m3u8"),
                        episode.getString("name"),
                        episode.getString("slug")
                );
                epis.server_data.add(epi);
            }
        }

        JSONObject movieObj = jsonObject.getJSONObject("movie");
        movie.id = movieObj.getString("_id");
        movie.name = movieObj.getString("name");
        movie.slug = movieObj.getString("slug");
        movie.origin_name = movieObj.getString("origin_name");
        movie.content = movieObj.getString("content");

        JSONArray categoryArray = new JSONArray(movieObj.getString("category"));
        for(int i = 0; i < categoryArray.length(); i++) {
            JSONObject category = categoryArray.getJSONObject(i);
            movie.category.add(category.getString("name"));
        }

        movie.thumb_url = movieObj.getString("thumb_url");
        movie.poster_url = movieObj.getString("poster_url");
        movie.episode_total = movieObj.getInt("episode_total");
        movie.year = movieObj.getInt("year");
        movie.subtitle = movieObj.getString("lang");
        movie.time = movieObj.getString("time");
        movie.trailer_url = movieObj.getString("trailer_url");

        JSONArray actorArray = new JSONArray(movieObj.getString("actor"));
        for(int i = 0; i < actorArray.length(); i++) {
            String actor = actorArray.getString(i);
            lstActor.add(actor);
            movie.actor.add(actor);
        }

        JSONArray countryArr = new JSONArray(movieObj.getString("country"));
        for(int i = 0; i < countryArr.length(); i++) {
            movie.country.add(countryArr.getJSONObject(i).getString("name"));
        }

        JSONArray directorArr = new JSONArray(movieObj.getString("director"));
        for(int i = 0; i < directorArr.length(); i++) {
            movie.director.add(directorArr.getString(i));
        }

        JSONObject tObj = new JSONObject(movieObj.getString("modified"));
        SimpleDateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date thisDate = new Date();
        Date date = dateFormatInput.parse(tObj.getString("time"));
        DateFormat date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        movie.last_update = date1.parse(dateFormatOutput.format(thisDate));
        movie.episode_current = movieObj.getString("episode_current");

        asignData();
    }

    String convertToInfoMovie() {
        return movie.episode_total + " tập  |   " +
                movie.year + "  |   " +
                movie.subtitle;
    }
    String convertToCategoryList() {
        String result = "";
        for(String category : movie.category) {
            if(movie.category.indexOf(category) == (movie.category.size()-1)) {
                result += category;
            }
            else {
                result += (category + " • ");
            }
        }
        return result;
    }
    String convertToCountyList() {
        String result = "";
        for(String country : movie.country) {
            if(movie.country.indexOf(country) == (movie.country.size()-1)) {
                result += country;
            }
            else {
                result += (country + ", ");
            }
        }
        return result;
    }
    String convertToDirectorList() {
        String result = "";
        for(String country : movie.director) {
            if(movie.director.indexOf(country) == (movie.director.size()-1)) {
                result += country;
            }
            else {
                result += (country + ", ");
            }
        }
        return result;
    }
    void asignData() {
        loadAdapterActor();
        Picasso.with(MovieDetail.this).load(movie.thumb_url).into(thumbImg);
        tvInfoMovie.setText(convertToInfoMovie());
        tvCategory.setText(convertToCategoryList());
        tvTime.setText(movie.time);
        tvTitle.setText(movie.name);
        tvContent.setText(movie.content);
        btnEpisodeCurrent.setText(movie.episode_current);
        tvCountry.setText(convertToCountyList());
        tvDirector.setText(convertToDirectorList());
        tvLastUpdate.setText(movie.last_update.toString());
    }
}