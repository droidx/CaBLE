// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.blebus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by priteshsankhe on 11/08/16.
 */
public class RegisterDialogFragment extends DialogFragment {

    private DialogButtonClickListener dialogButtonClickListener;

    private EditText mRegisterDeviceNameEt;
    private TextInputLayout mRegisterDeviceTl;

    public static RegisterDialogFragment newInstance(DialogButtonClickListener listener, Bundle args) {
        RegisterDialogFragment registerDialogFragment = new RegisterDialogFragment();
        registerDialogFragment.setArguments(args);
        registerDialogFragment.dialogButtonClickListener = listener;
        return registerDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.f_register_device, null);

        mRegisterDeviceTl = (TextInputLayout) view.findViewById(R.id.register_device_til);
        mRegisterDeviceNameEt = (EditText) view.findViewById(R.id.register_device_name_et);
        mRegisterDeviceTl.setErrorEnabled(true);


        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(
                        R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle bundle = new Bundle();
                                bundle.putString(BundleParams.CAB_NO, mRegisterDeviceNameEt.getText().toString().trim());
                                dialogButtonClickListener.doPositiveClick(bundle);
                            }
                        })
                .setNegativeButton(
                        R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialogButtonClickListener.doNegativeClick(null);
                            }
                        })
                .create();
    }

}
