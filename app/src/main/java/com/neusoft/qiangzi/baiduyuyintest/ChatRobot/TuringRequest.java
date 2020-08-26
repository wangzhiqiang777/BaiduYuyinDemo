package com.neusoft.qiangzi.baiduyuyintest.ChatRobot;

public class TuringRequest {

    private static final String API_KEY = "98a475503efa4d709d44dba91f3eedd5";
    private static final String USER_ID = "18742016198";
    static final String DEFAULT_CITY = "大连";
    static final String DEFAULT_PROVINCE = "辽宁";
    static final String DEFAULT_STREET = "软件园路";

    /**
     * reqType : 0
     * perception : {"inputText":{"text":"附近的酒店"},"selfInfo":{"location":{"city":"北京","province":"北京","street":"信息路"}}}
     * userInfo : {"apiKey":"","userId":""}
     */

    private int reqType;
    private PerceptionBean perception;
    private UserInfoBean userInfo;

    public TuringRequest() {
        reqType = 0;
        perception = new PerceptionBean();
        userInfo = new UserInfoBean();
    }

    public void  setInputText(String text){
        this.perception.inputText.setText(text);
    }
    public int getReqType() {
        return reqType;
    }

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    public PerceptionBean getPerception() {
        return perception;
    }

    public void setPerception(PerceptionBean perception) {
        this.perception = perception;
    }

    public UserInfoBean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoBean userInfo) {
        this.userInfo = userInfo;
    }

    public static class PerceptionBean {
        /**
         * inputText : {"text":"附近的酒店"}
         * selfInfo : {"location":{"city":"北京","province":"北京","street":"信息路"}}
         */

        private InputTextBean inputText;
        private SelfInfoBean selfInfo;

        public PerceptionBean() {
            inputText = new InputTextBean();
            selfInfo = new SelfInfoBean();
        }

        public InputTextBean getInputText() {
            return inputText;
        }

        public void setInputText(InputTextBean inputText) {
            this.inputText = inputText;
        }

        public SelfInfoBean getSelfInfo() {
            return selfInfo;
        }

        public void setSelfInfo(SelfInfoBean selfInfo) {
            this.selfInfo = selfInfo;
        }

        public static class InputTextBean {
            /**
             * text : 附近的酒店
             */

            private String text;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }

        public static class SelfInfoBean {
            /**
             * location : {"city":"北京","province":"北京","street":"信息路"}
             */

            private LocationBean location;

            public SelfInfoBean() {
                location = new LocationBean();
            }

            public LocationBean getLocation() {
                return location;
            }

            public void setLocation(LocationBean location) {
                this.location = location;
            }

            public static class LocationBean {
                /**
                 * city : 北京
                 * province : 北京
                 * street : 信息路
                 */
                private String city = DEFAULT_CITY;
                private String province = DEFAULT_PROVINCE;
                private String street = DEFAULT_STREET;

                public String getCity() {
                    return city;
                }

                public void setCity(String city) {
                    this.city = city;
                }

                public String getProvince() {
                    return province;
                }

                public void setProvince(String province) {
                    this.province = province;
                }

                public String getStreet() {
                    return street;
                }

                public void setStreet(String street) {
                    this.street = street;
                }
            }
        }
    }

    public static class UserInfoBean {
        /**
         * apiKey :
         * userId :
         */

        private String apiKey = API_KEY;
        private String userId = USER_ID;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
