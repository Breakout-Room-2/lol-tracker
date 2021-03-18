package com.example.leagueoflegendstracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.leagueoflegendstracker.models.League;
import com.example.leagueoflegendstracker.models.Mastery;
import com.example.leagueoflegendstracker.models.MatchSummary;
import com.example.leagueoflegendstracker.models.Summoner;

import org.json.JSONException;

import java.util.ArrayList;

import okhttp3.Headers;

public class SummonerDetailsActivity extends AppCompatActivity {

    public static final String TAG      = "SummonerDetailsActivity";
    public static final String API_KEY  = BuildConfig.API_KEY;

    public static final String BASE_URL = "https://na1.api.riotgames.com%s";
    public static final String SUMMONER_ENDPOINT      = "/lol/summoner/v4/summoners/by-name/%s";
    public static final String MASTERIES_ENDPOINT     = "/lol/champion-mastery/v4/champion-masteries/by-summoner/%s";
    public static final String LEAGUE_ENDPOINT        = "/lol/league/v4/entries/by-summoner/%s";
    public static final String MATCH_LIST_ENDPOINT    = "/lol/match/v4/matchlists/by-account/%s";
    public static final String MATCH_DETAILS_ENDPOINT = "/lol/match/v4/matches/%s";

    private int counter = 0;
    
    AsyncHttpClient client = new AsyncHttpClient();
    RequestParams params = new RequestParams();

    Summoner summoner;
    ArrayList<Mastery> top_masteries;
    ArrayList<League> leagues;
    ArrayList<MatchSummary> matchList;
    RecyclerView rvMatches;
    MatchDetailsAdapter adapter;

    ImageView ivChampOne, ivChampTwo, ivChampThree, ivSummonerIcon;
    TextView tvSummonerName, tvSummonerLevel, tvChampOne, tvChampTwo, tvChampThree, tvRank;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_details);
//
        ivSummonerIcon  = findViewById(R.id.ivSummonerIcon);
        tvSummonerName  = findViewById(R.id.tvSummonerName);
        tvSummonerLevel = findViewById(R.id.tvLevel);
        tvRank          = findViewById(R.id.tvRank);

        ivChampOne      = findViewById(R.id.ivChampOne);
        ivChampTwo      = findViewById(R.id.ivChampTwo);
        ivChampThree    = findViewById(R.id.ivChampThree);
        tvChampOne      = findViewById(R.id.tvChampOne);
        tvChampTwo      = findViewById(R.id.tvChampTwo);
        tvChampThree    =  findViewById(R.id.tvChampThree);

        rvMatches = findViewById(R.id.rvMatchHistory);

        Intent i = getIntent();
        String summonerName = i.getStringExtra("summonerName");

        params.put("api_key", API_KEY);

        String url = String.format(BASE_URL, String.format(SUMMONER_ENDPOINT, summonerName));

        Log.i(TAG, "Making call to: " + url);

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "Success, with data: "+json);
                try {
                    summoner = new Summoner(json.jsonObject);
                    Log.i(TAG, "Successfully created summoner model w/ ref: "+summoner.toString());
                    setProfile();
                    getStats();
                } catch (JSONException e) {
                    Log.e(TAG, "Hit JsonException: "+e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Failure, with resp: "+response);
            }
        });
    }

    private void setProfile() {
        String level    = String.format(getString(R.string.summoner_level), summoner.getSummonerLevel());
        tvSummonerLevel.setText(level);
        tvSummonerName.setText(summoner.getSummonerName());
        IdConverter.loadSummonerIcon(this, ivSummonerIcon, summoner.getProfileIconId());
    }

    private void getStats() {
        Log.i(TAG, "Getting champion masteries for: " + summoner.getSummonerName());
        makeApiCall(MASTERIES_ENDPOINT, summoner.getEncryptedSummonerId(), null);

        Log.i(TAG, "Getting summoner league ranking for: " + summoner.getSummonerName());
        makeApiCall(LEAGUE_ENDPOINT, summoner.getEncryptedSummonerId(), null);

        Log.i(TAG, "Getting summoner matchlist for: " + summoner.getSummonerName());
        makeApiCall(MATCH_LIST_ENDPOINT, summoner.getEncryptedAccountId(), null);
    }

    private void getMatchDetails() {
        for(MatchSummary match:matchList) {
            long ID = match.getGameID();
            Log.i(TAG, "Getting match details for matchID: " + ID);
            makeApiCall(MATCH_DETAILS_ENDPOINT, String.valueOf(ID), match);
        }
    }

    private void makeApiCall(String endpoint, String data, MatchSummary match){
        String url = String.format(BASE_URL, String.format(endpoint, data));
        Log.i(TAG, "Making call to " + url);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "Succesful API call to: " + url);
                // Log.i(TAG, "Success, with data: " + json);
                try {
                    createModel(endpoint, json, match);
                } catch (JSONException e) {
                    Log.i(TAG, "Hit JsonException " + e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "Failure, with data: " + response);
            }
        });
    }

    private void createModel(String endpoint, JsonHttpResponseHandler.JSON json, MatchSummary match) throws JSONException {
        switch (endpoint){
            case MASTERIES_ENDPOINT:
                top_masteries = Mastery.fromJSONArray(json.jsonArray);
                Log.i(TAG, "Created list of top masteries model w/ refs: " + top_masteries);
                setMastery();
                break;
            case LEAGUE_ENDPOINT:
                leagues = League.fromJsonArray(json.jsonArray);
                Log.i(TAG, "Created list of league (ranking) models w/ ref: " + leagues);
                setRank();
                break;
            case MATCH_LIST_ENDPOINT:
                matchList = MatchSummary.fromJsonArray(json.jsonObject.getJSONArray("matches"));
                Log.i(TAG, "Created list of match summary models w/ ref: " + matchList);
                getMatchDetails();
                break;
            case MATCH_DETAILS_ENDPOINT:
                match.setMatchDetails(json.jsonObject, summoner.getSummonerName());
                if (++counter == 10) {
                    fillRecycler();
                }
                break;
        }

    }

    private void fillRecycler() {
        adapter = new MatchDetailsAdapter(this, matchList);
        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);
    }

    private void setRank() {
        int index = League.getHigherLeague(leagues);
        if (index == -1)
            tvRank.setText(R.string.unranked);
        else
            tvRank.setText(leagues.get(index).getRanking());
    }

    private void setMastery() {
        int champOneID = top_masteries.get(0).getChampionID();
        int champTwoID = top_masteries.get(1).getChampionID();
        int champThreeID = top_masteries.get(2).getChampionID();
        IdConverter.loadChampIcon(this, ivChampOne, champOneID);
        IdConverter.loadChampIcon(this, ivChampTwo, champTwoID);
        IdConverter.loadChampIcon(this, ivChampThree, champThreeID);
        IdConverter.loadChampName(this, tvChampOne, champOneID);
        IdConverter.loadChampName(this, tvChampTwo, champTwoID);
        IdConverter.loadChampName(this, tvChampThree, champThreeID);
    }
}
