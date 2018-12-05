package com.filepicker.imagebrowse;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.bumptech.glide.Glide;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class


ImageFragment extends Fragment {


    private static final String IMAGE_URL = "image";
    PhotoView image;
    private String imageUrl;
    private AlertDialog mAlertDialog;
    private PagerAdapter mPagerAdapter;
    private List<String> mDatas;

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
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        image = (PhotoView) view.findViewById(R.id.image);
        image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                Log.e("bbbb", "cccccccccccccccccc");
                FragmentActivity activity = getActivity();
                if (activity instanceof PictureBrowseActivity) {
                    ((PictureBrowseActivity) activity).toggleFrame();
                } else if (activity instanceof RemotePictureBrowseActivity) {
                    ((RemotePictureBrowseActivity) activity).toggleFrame();
                }
            }
        });
        if (mDatas != null && mPagerAdapter != null) {
            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getActivity() instanceof PictureBrowseActivity) {
                        PictureBrowseActivity pictureBrowseActivity = (PictureBrowseActivity) getActivity();
                        if (pictureBrowseActivity.getCurrentMode() == PictureBrowseActivity.MODE_NETWORK)
                            return true;
                    }
                    mAlertDialog = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.wheter_delete_file)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    FragmentActivity activity = getActivity();
//                                    if (activity instanceof PictureBrowseActivity) {
//                                        File file = new File(imageUrl);
//                                        if (file.exists()) {
//                                            if (file.delete()) {
//                                                String where = MediaStore.Images.Media.DATA + "='" + imageUrl + "'";
//                                                getActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
//                                                mDatas.remove(imageUrl);
//                                                if (mDatas.size() > 0) {
//                                                    mPagerAdapter.notifyDataSetChanged();
//                                                } else {
//                                                    getActivity().setResult(Activity.RESULT_OK);
//                                                    getActivity().finish();
//                                                }
//                                                Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
//                                                intent.putExtra("isVideo", false);
//                                                VLCApplication.getAppContext().sendBroadcast(intent);
//                                            }
//                                        }
//                                    } else if (activity instanceof RemotePictureBrowseActivity) {
//                                        String cameraUrl = imageUrl.replace(Contacts.BASE_HTTP_IP, "A:").replace("/", "\\");
//                                        CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + cameraUrl, new CameraUtils.CmdListener() {
//                                            @Override
//                                            public void onResponse(String response) {
//                                                InputStream is;
//                                                try {
//                                                    is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                                                    DomParseUtils domParseUtils = new DomParseUtils();
//                                                    MovieRecord record = domParseUtils.getParserXml(is);
//                                                    if (record != null && record.getStatus().equals("0")) {
//                                                        mDatas.remove(imageUrl);
//                                                        Iterator<FileDomain> iterator = FileRepository.ALL_PHOTO_LIST.iterator();
//                                                        while (iterator.hasNext()) {
//                                                            if (imageUrl.equals(iterator.next().getDownloadPath())) {
//                                                                iterator.remove();
//                                                                break;
//                                                            }
//                                                        }
//                                                        if (mDatas.size() > 0) {
//                                                            mPagerAdapter.notifyDataSetChanged();
//                                                        } else {
//                                                            getActivity().setResult(Activity.RESULT_OK);
//                                                            getActivity().finish();
//                                                        }
//                                                    } else {
//                                                        Toast.makeText(getActivity(), getString(R.string.deleted_failure), Toast.LENGTH_SHORT).show();
//                                                    }
//                                                } catch (UnsupportedEncodingException e) {
//                                                    e.printStackTrace();
//                                                }
//
//                                            }
//
//                                            @Override
//                                            public void onErrorResponse(Exception volleyError) {
//                                                Toast.makeText(getActivity(), getString(R.string.deleted_failure), Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    }
                                }
                            }).setNegativeButton(R.string.cancel, null)
                            .create();
                    mAlertDialog.show();
                    return true;
                }
            });
        }
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
        Glide.clear(image);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void setAdapter(PagerAdapter viewpagerAdapter, List<String> datas) {
        mPagerAdapter = viewpagerAdapter;
        mDatas = datas;
    }

//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
