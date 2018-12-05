package com.photopicker.preview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;

import uk.co.senab.photoview.PhotoView;

public class ImageFragment extends Fragment {


    private static final String IMAGE_URL = "image";
    PhotoView image;
    private String imageUrl;

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance(String param1) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview, container, false);
        image = (PhotoView) view.findViewById(R.id.image);
//        image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
//            @Override
//            public void onViewTap(View view, float x, float y) {
//                Log.e("bbbb","cccccccccccccccccc");
//                FragmentActivity activity = getActivity();
//                if(activity instanceof PictureBrowseActivity){
//                    ((PictureBrowseActivity)activity).toggleFrame();
//                }
//            }
//        });
//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentActivity activity = getActivity();
//                if(activity instanceof PhotoPreviewActivity){
//                    ((PhotoPreviewActivity)activity).toggleFrame();
//                }
//            }
//        });

        if (imageUrl.endsWith("head.jpg")) {
            ImageLoaderUtil.getInstance().loadImageWithoutCache(getContext(), imageUrl, R.drawable.default_image_holder, image);
        } else {
            ImageLoaderUtil.getInstance().loadImage(getContext(), imageUrl, R.drawable.default_image_holder, image);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
