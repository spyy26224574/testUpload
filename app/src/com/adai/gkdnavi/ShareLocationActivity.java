package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.bean.LocationBean;
import com.adai.gkdnavi.adapter.Addinfo;
import com.adai.gkdnavi.adapter.AutoComleteAdapter;
import com.adai.gkdnavi.adapter.LocationSearchAdapter;
import com.adai.gkdnavi.utils.InputUtils;
import com.adai.gkdnavi.utils.StringUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

public class ShareLocationActivity extends BaseActivity implements View.OnClickListener, OnGetGeoCoderResultListener, OnGetPoiSearchResultListener {
    private TextView mBack;
    private TextView mHeadTitle;
    private TextView mRightText;
    private MapView mBMapView;
    private ImageView mMove2location;
    private RecyclerView mRvSelectLocal;
    private TextView mSearchingTitle;
    private BaiduMap baiduMap;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private LatLng localLatLng;
    private boolean isFirstLoc = true;
    private MapStatusUpdate mMapStatusUpdate;
    private GeoCoder mSearch;
    private int mSearchNum = 5;
    private int loadIndex = 0;
    private LocationSearchAdapter mAdapter;
    private List<LocationBean> mData = new ArrayList<>();
    private LocationBean mCurrentLocationBean;
    InstantAutoComplete goalAddress;
    private String localcity;
    private PoiSearch mPoiSearch = null;
    private TextView localCity;
    private ImageButton search;
    private SuggestionSearch mSuggestionSearch;
    private List<SuggestionResult.SuggestionInfo> sugList = new ArrayList<>();
    private List<Addinfo> listString = new ArrayList<Addinfo>();
    private String endName = "";
    private boolean isGeoCode;

    protected void initView() {
        search = (ImageButton) findViewById(R.id.search);
        localCity = (TextView) findViewById(R.id.localCity);
        mBack = (TextView) findViewById(R.id.back);
        mHeadTitle = (TextView) findViewById(R.id.head_title);
        mRightText = (TextView) findViewById(R.id.right_text);
        mBMapView = (MapView) findViewById(R.id.bMapView);
        mMove2location = (ImageView) findViewById(R.id.move2location);
        mRvSelectLocal = (RecyclerView) findViewById(R.id.rv_select_local);
        mSearchingTitle = (TextView) findViewById(R.id.searching_title);
        goalAddress = (InstantAutoComplete) findViewById(R.id.etSearch);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_share_location);
        initView();
        initLocation();
        initGeoSearch();//地址解析
        initPoiSearch();//搜索周边
        initSuggestSearch();
        initEvent();

    }

    private void initSuggestSearch() {
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                    return;
                }
                sugList.clear();
                listString.clear();
                for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
                    if (info.key != null)
                        listString.add(new Addinfo(info.key, "", "", ""));
                }
                sugList.addAll(suggestionResult.getAllSuggestions());

                AutoComleteAdapter aAdapter = new AutoComleteAdapter(listString, getApplicationContext());

                goalAddress.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();
                try {
                    String firstcity = sugList.get(0).city;
                    if (!StringUtils.isEmpty(firstcity)) {
                        localcity = firstcity;
                        localCity.setText(localcity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // sugAdapter = new ArrayAdapter<String>(this, R.layout.route_inputs);

		/*
         * 在这里判断是哪个方法调用
		 */
        goalAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int start, int count, int after) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() <= 0) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                if (!TextUtils.isEmpty(localcity))
                    mSuggestionSearch
                            .requestSuggestion((new SuggestionSearchOption())
                                    .keyword(editable.toString()).city(localcity));
            }
        });
        goalAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position,
                                    long id) {
                //点击某一项，然后得到其地址和地址的地理坐标，然后将endName转化为一个Latlng
//                endName = listString.get(position).getName();
                SuggestionResult.SuggestionInfo item = sugList.get(position);
                endName = item.key;
                goalAddress.setText(endName);
                goalAddress.setSelection(endName.length());
                LatLng latLng = item.pt;
                if (latLng != null) {
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                    baiduMap.animateMapStatus(mapStatusUpdate);//当前位置的Latlng坐标
                } else {
                    mSearch.geocode(new GeoCodeOption().city(StringUtils.isEmpty(item.city) ? localcity : item.city).address(item.key));
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mBMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBMapView.onPause();
    }

    private void initEvent() {
        search.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRightText.setOnClickListener(this);
        mMove2location.setOnClickListener(this);
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                LatLng mCenterLatLng = mapStatus.target;
                mSearchingTitle.setText(getString(R.string.searching));
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(mCenterLatLng));
            }
        });
        mRvSelectLocal.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LocationSearchAdapter(this, mData);
        mRvSelectLocal.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new LocationSearchAdapter.OnItemClickListener() {
            @Override
            public void onClick(LocationBean locationBean) {
                Log.e(_TAG_, "onClick: " + locationBean.address);
                mCurrentLocationBean = locationBean;
            }
        });
    }

    private void initGeoSearch() {
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    private void initLocation() {
        // TODO Auto-generated method stub
        // 地图初始化
        baiduMap = mBMapView.getMap();
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));//设置定位模式以及一些marker的属性
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.right_text:
                if (mCurrentLocationBean != null) {
                    Intent intent = new Intent();
                    intent.putExtra("location", mCurrentLocationBean);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.move2location:
                baiduMap.animateMapStatus(mMapStatusUpdate);//当前位置的Latlng坐标
                break;
            case R.id.search:
                if (InputUtils.KeyBoard(goalAddress)) {
                    InputUtils.HideKeyboard(goalAddress);
                }
                search();
                break;
        }
    }

    private void search() {
        if (!TextUtils.isEmpty(localcity)) {
            String s = goalAddress.getText().toString();
            Log.e(_TAG_, "search: localcity=" + localcity + ":s=" + s);
            mSearch.geocode(new GeoCodeOption().city(localcity).address(s));
        } else {
            ToastUtil.showShortToast(this, getString(R.string.navi_locationFail));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result.error != GeoCodeResult.ERRORNO.NO_ERROR) {
            mData.clear();
            mAdapter.notifyDataSetChanged();
            mCurrentLocationBean = null;
            return;
        }
        Log.e(_TAG_, "onGetGeoCodeResult: " + result.getAddress());
        LatLng latLng = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
        //poi城市搜索
        //mPoiSearch.searchInCity(new PoiCitySearchOption().city(localcity).keyword(endName).pageCapacity(10).pageNum(1));
//        LatLngBounds.Builder builder = new LatLngBounds.Builder().include(latLng);
//        LatLngBounds latLngBounds = builder.build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(mapStatusUpdate);//当前位置的Latlng坐标
//        mPoiSearch.searchInBound(new PoiBoundSearchOption().keyword(goalAddress.getText().toString()).pageCapacity(mSearchNum).bound(latLngBounds));
    }

    private void initPoiSearch() {
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult.error != ReverseGeoCodeResult.ERRORNO.NO_ERROR) {
            mData.clear();
            mAdapter.notifyDataSetChanged();
            mCurrentLocationBean = null;
            return;
        }
        Log.e(_TAG_, "onGetReverseGeoCodeResult: " + reverseGeoCodeResult.getAddress());
        mSearchingTitle.setText(getString(R.string.search_result));
        ReverseGeoCodeResult.AddressComponent addressDetail = reverseGeoCodeResult.getAddressDetail();
        String city = addressDetail.city;
        String province = addressDetail.province;
        String district = addressDetail.district;
        if (!TextUtils.isEmpty(city)) {
            localcity = city;
            localCity.setText(localcity);
        }
        List<PoiInfo> poiList = reverseGeoCodeResult.getPoiList();
        mData.clear();
        if (poiList != null) {
            for (PoiInfo poiInfo : poiList) {
                String name = poiInfo.name;
                String address = poiInfo.address;
                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(name)) {
                    continue;
                }
                LocationBean locationBean = new LocationBean();
                locationBean.province = province;
                locationBean.city = city;
                locationBean.district = district;
                locationBean.name = name;
                locationBean.address = address;
                locationBean.lat = poiInfo.location.latitude;
                locationBean.lng = poiInfo.location.longitude;
                mData.add(locationBean);
            }
            if (mData.size() > 0) {
                mData.get(0).isCheck = true;
                mCurrentLocationBean = mData.get(0);
            } else {
                mCurrentLocationBean = null;
            }
        }
        mAdapter.notifyDataSetChanged();
        mRvSelectLocal.smoothScrollToPosition(0);
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult.error != PoiResult.ERRORNO.NO_ERROR) {
            mData.clear();
            mAdapter.notifyDataSetChanged();
            mCurrentLocationBean = null;
            return;
        }
        mData.clear();
        List<PoiInfo> allPoi = poiResult.getAllPoi();
        for (PoiInfo poiInfo : allPoi) {
            String city = poiInfo.city;
            String address = poiInfo.address;
            String name = poiInfo.name;
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(name)) {
                continue;
            }
            LocationBean locationBean = new LocationBean();
            locationBean.city = city;
            locationBean.address = address;
            locationBean.name = name;
            locationBean.lat = poiInfo.location.latitude;
            locationBean.lng = poiInfo.location.longitude;
            mData.add(locationBean);
        }
        if (mData.size() > 0) {
            String city = mData.get(0).city;
            mCurrentLocationBean = mData.get(0);
            if (!TextUtils.isEmpty(city)) {
                localcity = city;
                localCity.setText(localcity);
            }
            mData.get(0).isCheck = true;
        } else {
            mCurrentLocationBean = null;
        }
        mAdapter.notifyDataSetChanged();
        mRvSelectLocal.smoothScrollToPosition(0);
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {//当前位置的location
            // map view 销毁后不在处理新接收的位置
            if (location == null || mBMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            localLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mMapStatusUpdate = MapStatusUpdateFactory.newLatLng(localLatLng);
            if (isFirstLoc) {//只有第一次自动定位到当前的位置，之后的通过主动点击跳转到当前的位置
                localcity = location.getCity();
                localCity.setText(localcity);
                mSearchingTitle.setText(getString(R.string.search_result));
                isFirstLoc = false;
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(localLatLng, 16);
                baiduMap.setMapStatus(msu);
                baiduMap.animateMapStatus(mMapStatusUpdate);//当前位置的Latlng坐标
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(localLatLng));
            }
            mBMapView.showZoomControls(false);

        }

    }

    @Override
    protected void onDestroy() {
        mLocClient.stop();
        BaiduMapNavigation.finish(mContext);
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        mBMapView.onDestroy();
        mSearch.destroy();
        mBMapView = null;
        super.onDestroy();
    }
}
