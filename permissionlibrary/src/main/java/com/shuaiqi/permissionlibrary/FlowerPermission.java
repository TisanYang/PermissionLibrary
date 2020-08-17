package com.shuaiqi.permissionlibrary;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FlowerPermission {

    private volatile static FlowerPermission mPermission;


    private Activity mContext;

    public static final int PERMISSON_REQUESTCODE = 3039;


    private FlowerPermission() {
    }

    public static FlowerPermission getInstance() {
       /* if (mPermission == null){
            synchronized (FlowerPermission.class){
                if (mPermission == null){
                    mPermission = new FlowerPermission();
                }
            }
        }
        return mPermission;*/
        return FlowerPermissionHolder.INSTANCE;
    }

    private static class FlowerPermissionHolder {

        private static FlowerPermission INSTANCE = new FlowerPermission();

    }

    private void init(Activity context) {
        this.mContext = context;
    }

    private void checkPermission(String[] needPermissions) {
        try {

            List<String> needRequestPermissonList = findDeniedPermissions(needPermissions);

            if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
                String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                Method method = mContext.getClass().getMethod("requestPermissions", String[].class, int.class);
                method.invoke(mContext, array, PERMISSON_REQUESTCODE);
            } else {
                Toast.makeText(mContext, "用户同意了权限-继续操作", Toast.LENGTH_SHORT).show();
                if (mLister != null) {
                    mLister.onPermissionGetted();
                }
            }
            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        //if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
        //如果是低版本的手机，手动拒绝了，进不到这里来 -- 这种基于反射判断权限是否获取到的方式在低版本上不合适
        try {
            for (String perm : permissions) {
                Method checkSelfMethod = mContext.getClass().getMethod("checkSelfPermission", String.class);
                Method shouldShowRequestPermissionRationaleMethod = mContext.getClass().getMethod("shouldShowRequestPermissionRationale", String.class);
                if ((Integer) checkSelfMethod.invoke(mContext, perm) != PackageManager.PERMISSION_GRANTED
                        || (Boolean) shouldShowRequestPermissionRationaleMethod.invoke(mContext, perm)) {
                    needRequestPermissonList.add(perm);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //}
        return needRequestPermissonList;
    }


    private void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case FlowerPermission.PERMISSON_REQUESTCODE:
                int perLength = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        perLength++;
                    }
                }

                if (grantResults.length > 0 && perLength == grantResults.length) {
                    Toast.makeText(mContext, "用户同意了权限", Toast.LENGTH_SHORT).show();
                    if (mLister != null) {
                        mLister.onPermissionGetted();
                    }
                } else {
                    // 没有获取到权限，做特殊处理
                    //showMissingPermissionDialog();
                    Toast.makeText(mContext, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
                    if (mLister != null) {
                        mLister.onPermissionNoGetted();
                    }
                }
                break;


        }
    }

    private void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == SETTING_FOR_PERMISSON) {


        }*/
    }


    private PermissionLister mLister;

    public void setPermissonRequestLister(PermissionLister lister) {
        mLister = lister;
    }

    public interface PermissionLister {

        void onPermissionGetted();

        void onPermissionNoGetted();
    }


    /**
     * Builder创建类,对showcaseView创建并进行一些配置
     */
    public static class Builder {

        private FlowerPermission mPermission;

        public Builder() {
            mPermission = FlowerPermission.getInstance();
        }

        public Builder init(Activity context) {
            mPermission.init(context);
            return this;
        }

        public Builder setPermissionListener(PermissionLister listener) {
            mPermission.setPermissonRequestLister(listener);
            return this;

        }

        public Builder checkPermission(String[] needPermissions) {
            mPermission.checkPermission(needPermissions);
            return this;
        }

        public Builder onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

            mPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return this;
        }

        public Builder onActivityResult(int requestCode, int resultCode, Intent data) {

            mPermission.onActivityResult(requestCode, resultCode, data);
            return this;
        }

        public FlowerPermission build() {
            return mPermission;
        }
    }
}
