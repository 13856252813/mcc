package com.txt.library.model;

import java.io.Serializable;

/**
 * Created by pc on 2018/1/31.
 */

public class ServiceRequest implements Serializable {


    /**
     * userInfo : {"customerId":"nono","name":"","email":"","phone":""}
     * userId : anonymousbdcacc@thomsoncs.txtechnology.com.cn
     * type : video
     * department : 5a548c0a20591c09062dde14
     * companyId : txtechnology
     * deviceType : andorid
     * channelType : andorid
     */

    private UserInfoBean userInfo;
    private String userId="";
    private String type="";
    private String department="";
    private String companyId="";
    private String deviceType="";
    private String channelType="";

    public UserInfoBean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoBean userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public static class UserInfoBean implements Serializable{
        /**
         * customerId : nono
         * name :
         * email :
         * phone :
         */
        private String customerId="";
        private String name="";
        private String email="";
        private String phone="";
        private String merchantId;
        private String storeId;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getStoreId() {
            return storeId;
        }

        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }

        @Override
        public String toString() {
            return "UserInfoBean{" +
                    "customerId='" + customerId + '\'' +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", merchantId='" + merchantId + '\'' +
                    ", storeId='" + storeId + '\'' +
                    '}';
        }
    }
}
