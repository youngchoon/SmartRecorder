package com.example.lks.www.smartrecorder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lks.www.smartrecorder.dummy.DummyContent;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class RecordFileListFragment extends ListFragment implements AbsListView.OnItemClickListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, Runnable  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String BROADCAST_CURRENT_POSITION = "PLAY";

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    private RecordFileListAdapter audioListAdapter;
    ArrayList<RecordDataModel> audioFileList;
    private TextView tvTimeReached, tvTimeRemaining;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;
    private SeekBar sbPlayProgress;
    private MediaPlayer mp = new MediaPlayer();

    // TODO: Rename and change types of parameters
    public static RecordFileListFragment newInstance(String param1, String param2) {
        RecordFileListFragment fragment = new RecordFileListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecordFileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_file_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        sbPlayProgress = (SeekBar) view.findViewById(R.id.activity_audio_file_list_pb);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);


        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            audioFileList = new ArrayList<RecordDataModel>();
            setAdapterForAudios();

        } else {
            Toast.makeText(getActivity(), "No sd card found.", Toast.LENGTH_LONG).show();
        }

        sbPlayProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && mp.isPlaying()) {
                    mp.seekTo(progress);
                }

            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public interface OnHeadlineSelectedListener {
        public void onArticleSelected(int position);
    }

    public void onFragmentInteraction(String id) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
    }

    /**
     * Set adapter for audio file in list and play
     */
    private void setAdapterForAudios() {

        // fetch data for audio files available in sd card
//		Cursor mCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
//				MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE }, AudioColumns.IS_MUSIC + "!=0", null, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");


        CursorLoader cursorLoader=new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE }, MediaStore.Audio.AudioColumns.IS_MUSIC + "!=0", null, null);
        Cursor mCursor=cursorLoader.loadInBackground();

        // set data in model
        RecordDataModel recordDataModel;

        while (mCursor.moveToNext()) {


            recordDataModel = new RecordDataModel();
            recordDataModel.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            recordDataModel.setFileDuration(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            recordDataModel.setFileId(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            recordDataModel.setFilePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            recordDataModel.setFileSize(getFileSize(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.SIZE))));
            recordDataModel.setSelected(false);

            audioFileList.add(recordDataModel);

        }

        // if audio data found than visible the list view
        if (audioFileList.size() > 0) {

            audioListAdapter = new RecordFileListAdapter(getActivity(), audioFileList);
            mListView.setAdapter(audioListAdapter);

        } else {
            Toast.makeText(getActivity(), "No recorded files found.", Toast.LENGTH_LONG).show();
        }

        // Register receiver
        IntentFilter intentFilter = new IntentFilter(BROADCAST_CURRENT_POSITION);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    /**
     * Convert file size for attachment from bytes to kb or mb
     *
     * @param fileSize
     *            = file size in string
     * @return file size in kb or mb
     */
    public static String getFileSize(String fileSize) {
        int DIVIDER = 1024;
        String result = null;
        DecimalFormat format;
        float size = Float.parseFloat(fileSize);
        if ((size / DIVIDER) > DIVIDER) {
            format = new DecimalFormat(".0");
            result = format.format(((size / DIVIDER)) / DIVIDER) + " MB";
            Log.e("File size in MB", "" + ((size / DIVIDER)) / DIVIDER);
        } else {
            format = new DecimalFormat(".00");
            result = format.format((size / DIVIDER)) + " KB";
            Log.e("File size in KB", "" + (size / DIVIDER));
        }
        return result;
    }

    /**
     * BroadCastReceiver for showing progress of audio file
     *
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null) {
                int currentProgress = intent.getIntExtra("play_position", 0);
                tvTimeReached.setText(mediaTime((long) currentProgress));
                sbPlayProgress.setProgress(currentProgress);
            }

        }
    };

    /**
     * Method for converting milliseconds to minute
     *
     * @param milliSecs
     * @return
     */
    public static String mediaTime(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

    /**
     * Play Audio file from list
     *
     * @param filePath
     *            = path of file
     * @param playFile
     *            = flag for pause and play
     *
     */
    public void playAudioFile(String filePath, boolean playFile) {

        mp.setOnErrorListener(this);
        mp.setOnCompletionListener(this);
        try {
            Log.e("File Path", filePath);
            if (mp.isPlaying()) {
                mp.stop();
                mp.reset();
            }
            if (filePath != null && playFile) {
                mp.reset();
                mp.setDataSource(getActivity(), Uri.parse(filePath));
                mp.prepare();
                mp.start();

                // set total time to view
                tvTimeRemaining.setText(mediaTime((long) mp.getDuration()));

                // set max progress to seek bar
                sbPlayProgress.setMax(mp.getDuration());

                // start thread for updating current progress in seek bar
                new Thread(this).start();

            } else if (filePath.equals("") && !playFile) {

                // stop playing audio file
                tvTimeRemaining.setText("00:00");
                tvTimeReached.setText("00:00");
                if (mp.isPlaying()) {
                    mp.stop();
                    new Thread(this).stop();
                }

            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    /*
    @Override
    public void onBackPressed() {
        // todo: handle when user has back
        //super.onBackPressed();
        if (mp.isPlaying()) {
            mp.stop();
        }
    }
    */

    /**
     * (non-Javadoc)
     *
     * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer,
     *      int, int)
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        audioListAdapter.changeFileAfterCompletion();
        Toast.makeText(getActivity(), "File not supported.", Toast.LENGTH_LONG).show();
        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
     */
    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.e("Playing Complete", "Playing Complete");
        audioListAdapter.changeFileAfterCompletion();
        // new Thread(this).stop();
        tvTimeRemaining.setText("00:00");
        tvTimeReached.setText("00:00");

    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        int currentPosition = 0;
        int total = mp.getDuration();

        while (mp.isPlaying() && currentPosition < total) {
            currentPosition = mp.getCurrentPosition();

            // broadcast the current progress
            callBroadcast(currentPosition);
        }

    }

    /**
     * Call to BroadCast the audio files current position
     *
     * @param currentPosition
     */
    private void callBroadcast(int currentPosition) {

        Intent intent = new Intent();
        intent.putExtra("play_position", currentPosition);
        intent.setAction(BROADCAST_CURRENT_POSITION);
        getActivity().sendBroadcast(intent);

    }

}
