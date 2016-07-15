package ru.imunit.maquiz.activities;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;

import ru.imunit.maquiz.R;
import ru.imunit.maquiz.fragments.StartFragment;
import ru.imunit.maquiz.managers.ExceptionNotifier;
import ru.imunit.maquiz.managers.MusicUpdater;

public class StartActivity extends AppCompatActivity
        implements StartFragment.OnFragmentInteractionListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private View mRootLayout;
    private ProgressDialog mProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRootLayout = findViewById(R.id.activity_start);
        if (checkStoragePermission()) {
            // TODO: maybe do music update only on first activity show
            startMusicUpdate();
        }
    }

    @Override
    public void onPlaylistOpen() {
        if (!checkStoragePermission())
            return;
        Intent playlistsIntent = new Intent(this,
                ActivityFactory.getActivity(ActivityFactory.PLAYLIST_ACTIVITY));
        startActivity(playlistsIntent);
    }

    @Override
    public void onPlay() {
        if (!checkStoragePermission())
            return;
        Intent gameIntent = new Intent(this,
                ActivityFactory.getActivity(ActivityFactory.GAME_ACTIVTY));
        startActivity(gameIntent);
    }

    @Override
    public void onStatsOpen() {
        if (!checkStoragePermission())
            return;
        Intent statsIntent = new Intent(this,
                ActivityFactory.getActivity(ActivityFactory.STATS_ACTIVTY));
        startActivity(statsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(mRootLayout, R.string.permission_storage_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(StartActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READ_STORAGE);
                    }
                }).show();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_STORAGE);
            }
            return false;
        }
        else {
            return true;
        }
    }

    private void startMusicUpdate() {
        mProgress = ProgressDialog.show(this, null,
                getResources().getString(R.string.updating_music_dialog), true, true);
        MusicUpdater updater = new MusicUpdater(this);
        updater.setListener(new MusicUpdater.MusicUpdateListener() {
            @Override
            public void onUpdateCompleted(int res) {
                if (StartActivity.this.mProgress != null) {
                    StartActivity.this.mProgress.dismiss();
                }
                if (res == MusicUpdater.RESULT_ERROR) {
                    ExceptionNotifier.make(mRootLayout,
                            getResources().getString(R.string.err_database_error)).show();
                } else if (res == MusicUpdater.RESULT_FEW_MUSIC) {
                    // TODO: This should be shown only once! Define some parameter to store...
                    ExceptionNotifier.make(mRootLayout,
                            getResources().getString(R.string.err_few_music)).
                            setActionListener(new ExceptionNotifier.ActionListener() {
                                @Override
                                public void onClick() {
                                    // added handler just to have dismiss button in snackbar
                                }
                            }).show();
                }
            }
        });
        updater.startUpdate();

    }

    private void noPermissionsExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.err_permissions_title).setMessage(R.string.err_permissions_text);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StartActivity.this.finishAffinity();
            }
        });
        AlertDialog d = builder.create();
        d.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                noPermissionsExit();
            } else {
                startMusicUpdate();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
