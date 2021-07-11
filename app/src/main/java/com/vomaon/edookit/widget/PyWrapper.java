package com.vomaon.edookit.widget;

import android.app.Activity;

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

        return obj.toString();
    }
}
