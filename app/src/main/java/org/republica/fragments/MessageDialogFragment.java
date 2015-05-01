package org.republica.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * A generic class to display a simple message in a dialog box.
 *
 * @author Christophe Beyls
 */
public class MessageDialogFragment extends DialogFragment {

    public static MessageDialogFragment newInstance(@StringRes int titleResId, @StringRes int messageResId) {
        MessageDialogFragment f = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("titleResId", titleResId);
        args.putInt("messageResId", messageResId);
        f.setArguments(args);
        return f;
    }

    public static MessageDialogFragment newInstance(@StringRes int titleResId, CharSequence message) {
        MessageDialogFragment f = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("titleResId", titleResId);
        args.putCharSequence("message", message);
        f.setArguments(args);
        return f;
    }

    public static MessageDialogFragment newInstance(CharSequence title, CharSequence message) {
        MessageDialogFragment f = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putCharSequence("message", message);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int titleResId = args.getInt("titleResId", -1);
        CharSequence title = (titleResId != -1) ? getText(titleResId) : args.getCharSequence("title");
        int messageResId = args.getInt("messageResId", -1);
        CharSequence message = (messageResId != -1) ? getText(messageResId) : args.getCharSequence("message");

        return new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, null).create();
    }

    public void show(FragmentManager manager) {
        show(manager, "message");
    }
}
