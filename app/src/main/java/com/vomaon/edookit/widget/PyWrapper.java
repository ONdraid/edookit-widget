package com.vomaon.edookit.widget;

import android.app.Activity;
import android.util.Log;
import android.webkit.CookieManager;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PyWrapper {
    Activity activity;

    PyWrapper (Activity activity) {
        this.activity = activity;
    }

    public String getData(String username, String password, String schoolID) {
        if(!Python.isStarted())
            Python.start(new AndroidPlatform(activity));
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("gethtmltable");
        PyObject obj = pyObj.callAttr("main", username, password, schoolID);

        String[] data = obj.toJava(String[].class);
        updateCookies(schoolID, data[1], data[2]);

        return data[0];
    }

    public String getFullname(String username, String password, String schoolID) {
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(activity));
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("gethtmltable");
        PyObject obj = pyObj.callAttr("getFullname", username, password, schoolID);

        String[] data = obj.toJava(String[].class);
        updateCookies(schoolID, data[1], data[2]);

        return data[0];
    }

    private void updateCookies(String schoolID, String PHPSESSID, String XLoginIdPortal) {
        CookieManager.getInstance().setCookie((schoolID + ".edookit.net"), ("PHPSESSID=" + PHPSESSID));
        CookieManager.getInstance().setCookie((schoolID + ".edookit.net"), ("X-LoginId-Portal=" + XLoginIdPortal));
        CookieManager.getInstance().setCookie((schoolID + ".edookit.net"), ("_nss=1"));

        String cookies = CookieManager.getInstance().getCookie("https://gymunicov.edookit.net/");
        Log.d("cookies are: ", cookies);
    }
}
