package ru.imunit.maquiz.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import ru.imunit.maquiz.R;
import ru.imunit.maquiz.fragments.GameFragment;
import ru.imunit.maquiz.models.GameModel;
import ru.imunit.maquizdb.DataSourceFactory;
import ru.imunit.maquizdb.entities.DBTrack;

public class GameActivity extends AppCompatActivity
        implements GameFragment.OnFragmentInteractionListener {

    private GameFragment mFragment;
    private GameModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // TODO: refactor with dynamic adding (game result will be a separate fragment)
        mFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_game);
        initModel();
    }

    private void initModel() {
        mModel = new GameModel(DataSourceFactory.getDataSource(this));
        mFragment.setModel(mModel);
        mModel.subscribe(mFragment);
        // TODO: refactor options and rounds: should be in activity parameters
        mModel.initGame(5, 10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu here
        return super.onCreateOptionsMenu(menu);
    }

    // Fragment listener

    @Override
    public void onNextRound() {
        mModel.nextRound();
    }

    @Override
    public void onStartPlayback() {
        mModel.startPlayback();
    }

    @Override
    public void onMakeGuess(DBTrack track) {
        mModel.makeGuess(track);
    }
}
