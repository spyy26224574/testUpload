package com.adai.gkdnavi.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkdnavi.FileGridActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.square.LineDecoration;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileGridFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TYPE = "type";
    private static final String ARG_FILE_LIST="file_list";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    private int type = 0;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<String> files;
    private FileGridRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileGridFragment() {
    }

    /**
     *
     * @param columnCount
     * @param type 类型 0为图片，1为视频
     * @return
     */
    public static FileGridFragment newInstance(int columnCount,int type,ArrayList<String> files) {
        FileGridFragment fragment = new FileGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_TYPE,type);
        args.putStringArrayList(ARG_FILE_LIST,files);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            type=getArguments().getInt(ARG_TYPE);
            files=getArguments().getStringArrayList(ARG_FILE_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_grid, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.addItemDecoration(new LineDecoration(5));
            adapter = new FileGridRecyclerViewAdapter(getActivity(), mColumnCount, files, mListener);
            adapter.setFileType(type);
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new FileGridRecyclerViewAdapter.onItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Log.e("9527", "FileGridFragment onItemClick position = " + position);
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Log.e("9527", "FileGridFragment onItemLongClick position = " + position);
                    adapter.notifyDataSetChanged();
                    FileGridActivity parentActivity = (FileGridActivity) getActivity();
//                    parentActivity.displaybutton();
                }

                @Override
                public void onButtonStatus(boolean display) {
                    FileGridActivity parentActivity = (FileGridActivity) getActivity();
//                    parentActivity.hidebutton();


                }
            });

            Log.e(getClass().getSimpleName(),"aaaaaaaa");
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(String item);
    }

    public void DeleteItem() {
        adapter.DeleteItem();
    }

    public void ShareItem() {
        adapter.ShareItem();

    }


}
