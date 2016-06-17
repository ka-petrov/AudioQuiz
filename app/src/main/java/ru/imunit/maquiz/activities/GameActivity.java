package ru.imunit.maquiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import ru.imunit.maquiz.R;
import ru.imunit.maquiz.fragments.GameFragment;
import ru.imunit.maquiz.fragments.ModelRetainFragment;
import ru.imunit.maquiz.fragments.ResultsFragment;
import ru.imunit.maquiz.models.GameModel;
import ru.imunit.maquizdb.DataSourceFactory;
import ru.imunit.maquizdb.entities.DBTrack;

public class GameActivity extends AppCompatActivity
        implements GameFragment.GameFragmentListener,
        ResultsFragment.ResultsFragmentListener {

    private ModelRetainFragment mModelRetainFragment;
    private GameFragment mGameFragment;
    private ResultsFragment mResultsFragment;
    private GameModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // retain or create model
        mModelRetainFragment = (ModelRetainFragment)
                getSupportFragmentManager().findFragmentByTag("ModelRetain");
        if (mModelRetainFragment == null) {
            mModelRetainFragment = new ModelRetainFragment();
            mModelRetainFragment.setModel(new GameModel(DataSourceFactory.getDataSource(this)));
            getSupportFragmentManager().beginTransaction()
                    .add(mModelRetainFragment, "ModelRetain").commit();
        }
        mModel = mModelRetainFragment.getModel();

        if (mModel.isGameFinished()) {
            showResultsFragment();
        } else if (mModel.isGameRunning()) {
            showGameFragment();
        } else {
            // TODO: refactor options and rounds: should be in activity parameters
            mModel.initGame(5, 3);
            showGameFragment();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu here
        return super.onCreateOptionsMenu(menu);
    }

    private void showGameFragment() {
        // try to retain the game fragment before creating a new instance
        mGameFragment = (GameFragment)getSupportFragmentManager().findFragmentByTag("GameFragment");
        if (mGameFragment == null)
            mGameFragment = new GameFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder, mGameFragment, "GameFragment");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void showResultsFragment() {
        // unsubscribe game fragment from model if it is added
        GameFragment gf = (GameFragment)getSupportFragmentManager().findFragmentByTag("GameFragment");
        if (gf != null)
            mModel.unsubscribe(gf);

        if (mResultsFragment == null)
            mResultsFragment = new ResultsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder, mResultsFragment, "ResultsFragment");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    // GameFragment listener

    @Override
    public void onGameFragmentInitialized() {
        if (mGameFragment.getModel() == null) {
            mGameFragment.setModel(mModel);
            mModel.subscribe(mGameFragment);
        }
        if (!mModel.isGameRunning()) {
            mModel.nextRound();
        }
    }

    @Override
    public void onNextRound() {
        mModel.nextRound();
    }

    @Override
    public void onStartPlayback() {
        mModel.startPlayback();
    }

    @Override
    public void onMediaReady() {
        mModel.startTimer();
    }

    @Override
    public void onMakeGuess(DBTrack track) {
        mModel.makeGuess(track);
    }

    @Override
    public void onGameFinished() {
        showResultsFragment();
    }

    // ResultsFragment listener

    @Override
    public void onResultsFragmentInitialized() {
        mResultsFragment.setModel(mModel);
        mResultsFragment.updateResults();
    }

    @Override
    public void onRestartGame() {
        mModel.initGame(5, 3);
        showGameFragment();
    }

    @Override
    public void onShowStatistics() {

    }

    @Override
    public void onShowMenu() {
        Intent menuIntent = new Intent(this,
                ActivityFactory.getActivity(ActivityFactory.START_ACTIVITY));
        startActivity(menuIntent);
    }
}