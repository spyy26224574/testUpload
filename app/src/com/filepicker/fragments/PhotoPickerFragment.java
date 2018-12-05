package com.filepicker.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkdnavi.R;
import com.filepicker.FilePickerConst;
import com.filepicker.adapters.PhotoDirsPopAdapter;
import com.filepicker.adapters.PhotoGridAdapter;
import com.filepicker.cursors.loadercallbacks.FileResultCallback;
import com.filepicker.models.Photo;
import com.filepicker.models.PhotoDirectory;
import com.filepicker.utils.ImageCaptureManager;
import com.filepicker.utils.MediaStoreHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;




public class PhotoPickerFragment extends BaseFragment {

    private static final String TAG = PhotoPickerFragment.class.getSimpleName();
    RecyclerView recyclerView;

    TextView emptyView;
    TextView photodirectory;

    private PhotoPickerFragmentListener mListener;
    private PhotoGridAdapter photoGridAdapter;
    private ArrayList<String> selectedPaths;
    private List<PhotoDirectory> dirs;
    private PhotoDirectory currentDir;
    private ImageCaptureManager imageCaptureManager;

    private PopupWindow popupWindow;
    private View bottom_layout;

    public PhotoPickerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_picker, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoPickerFragmentListener) {
            mListener = (PhotoPickerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PhotoPickerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static PhotoPickerFragment newInstance(ArrayList<String> selectedPaths) {
        PhotoPickerFragment photoPickerFragment = new PhotoPickerFragment();
        photoPickerFragment.selectedPaths = selectedPaths;
        return  photoPickerFragment;
    }

    public interface PhotoPickerFragmentListener {
        // TODO: Update argument type and name

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews(view);
        initView();
    }

    private void setViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
        photodirectory=(TextView)view.findViewById(R.id.photodirectory);
        bottom_layout=view.findViewById(R.id.bottom_layout);
    }

    private void initView() {
        imageCaptureManager = new ImageCaptureManager(getActivity());
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getDataFromMedia();
    }

    private void getDataFromMedia() {
        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF, false);

        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new FileResultCallback<PhotoDirectory>() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        updateList(dirs);
                    }
                });
    }

    private void updateList(List<PhotoDirectory> dirs) {
        ArrayList<Photo> allphotos = new ArrayList<>();
        for (int i = 0; i < dirs.size(); i++) {
            Log.i(TAG,"dirname="+dirs.get(i).getName()+",id="+dirs.get(i).getId());
            allphotos.addAll(dirs.get(i).getPhotos());
        }

//        if(allphotos.size()>0) {
//            emptyView.setVisibility(View.GONE);
//        }
//        else {
//            emptyView.setVisibility(View.VISIBLE);
//        }

        if(allphotos.size()>0){
            PhotoDirectory all=new PhotoDirectory();
            all.setPhotos(allphotos);
            all.setId("allphotos");
            all.setName(getString(R.string.all_photo));
            dirs.add(0,all);
        }
        this.dirs=dirs;
        if(dirs!=null&&dirs.size()>0){
            loadDir(dirs.get(0));
            initPopupWindow();
            photodirectory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(popupWindow==null)return;
                    if(popupWindow.isShowing()){
                        popupWindow.dismiss();
                    }else{
                        popupWindow.showAsDropDown(bottom_layout);
                    }
                }
            });
        }
    }

//    private void initPopupWindow(){
//        Display dis = getActivity().getWindowManager().getDefaultDisplay();
//        int sWidth=dis.getWidth();
//        int sHeight=dis.getHeight();
//        popupWindow=new ListPopupWindow(getActivity());
//        popupWindow.setForceIgnoreOutsideTouch(true);
//        popupWindow.setAnchorView(bottom_layout);
//        popupWindow.setHeight(sHeight-250);
//        popupWindow.setListSelector(getActivity().getResources().getDrawable(R.drawable.bg_line_press));
//        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        popupWindow.setAdapter(new PhotoDirsPopAdapter(dirs));
//        popupWindow.setVerticalOffset(1);
//        popupWindow.setAnimationStyle(R.style.PopupwindowAnimBottom);
//    }

    private void initPopupWindow(){
        Display dis = getActivity().getWindowManager().getDefaultDisplay();
        int sWidth=dis.getWidth();
        int sHeight=dis.getHeight();
        popupWindow=new PopupWindow(getActivity());
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(sHeight-250);
        ListView listView=new ListView(getActivity());
        listView.setAdapter(new PhotoDirsPopAdapter(dirs));
        popupWindow.setContentView(listView);
    }

    private void loadDir(PhotoDirectory dir){
        if(dir==null)return;
        photodirectory.setText(dir.getName());
        currentDir=dir;
        ArrayList<Photo> photos = new ArrayList<>();
        photos.addAll(dir.getPhotos());
        if(photoGridAdapter!=null)
        {
            photoGridAdapter.setData(photos);
            photoGridAdapter.notifyDataSetChanged();
        }
        else
        {
            photoGridAdapter = new PhotoGridAdapter(getActivity(), photos,selectedPaths);
            recyclerView.setAdapter(photoGridAdapter);
            photoGridAdapter.setCameraListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = imageCaptureManager.dispatchTakePictureIntent();
                        if(intent!=null)
                            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                        else
                            Toast.makeText(getActivity(), "No Application exists for camera!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                if(resultCode== Activity.RESULT_OK)
                {
                    imageCaptureManager.galleryAddPic(getActivity());
                    getDataFromMedia();
                }
                break;
        }
    }
}
