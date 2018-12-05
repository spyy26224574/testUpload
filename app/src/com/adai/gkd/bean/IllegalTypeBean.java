package com.adai.gkd.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by huangxy on 2017/1/17.
 */

public class IllegalTypeBean extends BasePageBean{

    private static final long serialVersionUID = 13116875568122013L;
    /**
     * data : {"items":[{"violation_code":"w001","violation_name":"杩濆弽鎷夐摼寮忛\u20ac氳","price_time":"50鍏�/5娆�"},{"violation_code":"w002","violation_name":"閬撹矾鎷ユ尋鎴栧悎閬撴椂鏈緷娆¤椹�","price_time":"100鍏�/1娆�"},{"violation_code":"w003","violation_name":"涔樿溅浜哄悜杞﹀鎶涙磼鐗╁搧","price_time":"100鍏�/1娆�"},{"violation_code":"w004","violation_name":"浜ら\u20ac氳倗浜嬮\u20ac冮\u20ac�","price_time":"50鍏�/5娆�"},{"violation_code":"w005","violation_name":"杩濇硶鍗犵敤鍏氦杞﹂亾琛岄┒","price_time":"100鍏�/1娆�"},{"violation_code":"w006","violation_name":"涓嶆寜瑙勫畾杞﹂亾琛岄┒","price_time":"50鍏�/5娆�"},{"violation_code":"w007","violation_name":"閫嗗悜琛岄┒","price_time":"50鍏�/5娆�"}],"_meta":{"totalCount":7,"pageCount":1,"currentPage":1,"perPage":10},"_links":{"self":{"href":"http://192.168.1.10:8181/rest/attention?&page=1&per_page=10"}}}
     */

    public DataBean data;

    public static class DataBean {
        /**
         * items : [{"violation_code":"w001","violation_name":"杩濆弽鎷夐摼寮忛\u20ac氳","price_time":"50鍏�/5娆�"},{"violation_code":"w002","violation_name":"閬撹矾鎷ユ尋鎴栧悎閬撴椂鏈緷娆¤椹�","price_time":"100鍏�/1娆�"},{"violation_code":"w003","violation_name":"涔樿溅浜哄悜杞﹀鎶涙磼鐗╁搧","price_time":"100鍏�/1娆�"},{"violation_code":"w004","violation_name":"浜ら\u20ac氳倗浜嬮\u20ac冮\u20ac�","price_time":"50鍏�/5娆�"},{"violation_code":"w005","violation_name":"杩濇硶鍗犵敤鍏氦杞﹂亾琛岄┒","price_time":"100鍏�/1娆�"},{"violation_code":"w006","violation_name":"涓嶆寜瑙勫畾杞﹂亾琛岄┒","price_time":"50鍏�/5娆�"},{"violation_code":"w007","violation_name":"閫嗗悜琛岄┒","price_time":"50鍏�/5娆�"}]
         * _meta : {"totalCount":7,"pageCount":1,"currentPage":1,"perPage":10}
         * _links : {"self":{"href":"http://192.168.1.10:8181/rest/attention?&page=1&per_page=10"}}
         */

        public MetaBean _meta;
        public LinksBean _links;
        public ArrayList<ItemsBean> items;

        public static class MetaBean {
            /**
             * totalCount : 7
             * pageCount : 1
             * currentPage : 1
             * perPage : 10
             */

            public int totalCount;
            public int pageCount;
            public int currentPage;
            public int perPage;
        }

        public static class LinksBean {
            /**
             * self : {"href":"http://192.168.1.10:8181/rest/attention?&page=1&per_page=10"}
             */

            public SelfBean self;

            public static class SelfBean {
                /**
                 * href : http://192.168.1.10:8181/rest/attention?&page=1&per_page=10
                 */

                public String href;
            }
        }

        public static class ItemsBean implements Serializable{
            private static final long serialVersionUID = 6800961256219040529L;
            /**
             * violation_code : w001
             * violation_name : 杩濆弽鎷夐摼寮忛€氳
             * price_time : 50鍏�/5娆�
             */
            public boolean isSelected;
            public String violation_code;
            public String violation_name;
            public String price_time;
        }
    }

}
