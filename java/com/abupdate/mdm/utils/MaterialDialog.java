package com.abupdate.mdm.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Trace;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.abupdate.mdm.R;

/**
 * Create custom Dialog windows for your application
 * Custom dialogs rely on custom layouts wich allow you to
 * create and use your own look & feel.
 * <p>
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 *
 * @author antoine vianey
 */
public class MaterialDialog extends Dialog {

    static TextView title;

    static Builder mBuilder;

    public MaterialDialog(Context context, int theme) {
        super(context, theme);
    }

    public MaterialDialog(Context context) {
        super(context);
    }

    public static MaterialDialog materialDialog(final Builder builder) {

        mBuilder = builder;

        LayoutInflater inflater = (LayoutInflater) builder.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // instantiate the dialog with the custom Theme
        final MaterialDialog dialog = new MaterialDialog(builder.context,
                R.style.Dialog);
        View layout = inflater.inflate(R.layout.dialog, null);
        dialog.addContentView(layout, new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        // set the dialog title
        title = layout.findViewById(R.id.title);
        title.setText(builder.title);
        title.setGravity(builder.titleGravity);
        // set the bottom button
        RelativeLayout relativeLayout = layout.findViewById(R.id.bottom);
        relativeLayout.setGravity(builder.bottomGravity);

        if (builder.positiveButtonText != null) {
            ((TextView) layout.findViewById(R.id.positiveButton)).setText(builder.positiveButtonText);
            layout.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (builder.onPositiveCallback != null) {
                        builder.onPositiveCallback.onClick(
                                dialog,
                                DialogAction.POSITIVE);
                    }
//                    dialog.dismiss();
                }
            });
        } else {
            layout.findViewById(R.id.positiveButton).setVisibility(
                    View.GONE);
        }

        // set the cancel button
        if (builder.negativeButtonText != null) {
            ((TextView) layout.findViewById(R.id.negativeButton)).setText(builder.negativeButtonText);
            layout.findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (builder.onNegativeCallback != null) {
                        builder.onNegativeCallback.onClick(
                                dialog,
                                DialogAction.NEGATIVE);
                    }
                    dialog.dismiss();
                }
            });
        } else {
            layout.findViewById(R.id.negativeButton).setVisibility(
                    View.GONE);
        }

        // set the content message
        if (builder.message != null) {
            ((TextView) layout.findViewById(
                    R.id.message)).setText(builder.message);
        } else if (builder.contentView != null) {
            // if no message set
            // add the contentView to the dialog body
            ((LinearLayout) layout.findViewById(R.id.content))
                    .removeAllViews();
            ((LinearLayout) layout.findViewById(R.id.content))
                    .addView(builder.contentView,
                            new LayoutParams(
                                    LayoutParams.FILL_PARENT,
                                    LayoutParams.FILL_PARENT));

        }

        //ymh add
        if (builder.positiveButtonText == null && builder.negativeButtonText == null) {
            layout.findViewById(R.id.bottom).setVisibility(View.GONE);
        }

        if (builder.title == null) {
            layout.findViewById(R.id.title).setVisibility(View.GONE);
        }

        if (builder.onKeyListener != null) {
            dialog.setOnKeyListener(builder.onKeyListener);
        }
        if (builder.onCancelListener != null) {
            dialog.setOnCancelListener(builder.onCancelListener);
        }
        if (builder.dismissListener != null) {
            dialog.setOnDismissListener(builder.dismissListener);
        }


        //end

        dialog.setCancelable(builder.cancelable);
        dialog.setContentView(layout);
        WindowManager windowManager = (WindowManager)
                builder.context.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (d.getWidth() * 0.65);
        dialog.getWindow().setAttributes(p);
        return dialog;
    }

    public final void setTitle(@NonNull CharSequence newTitle) {
        title.setText(newTitle);
    }

    public final View getCustomView() {
        return mBuilder.getContentView();
    }


    /**
     * An alternate way to define a single callback.
     */
    public interface SingleButtonCallback {

        void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        int titleGravity = Gravity.START;
        int bottomGravity = Gravity.END;
        String title;
        String message;
        String positiveButtonText;
        String negativeButtonText;
        View contentView;
        boolean cancelable = true, wrapInScrollView;
        SingleButtonCallback onPositiveCallback, onNegativeCallback;
        OnKeyListener onKeyListener = null;
        OnCancelListener onCancelListener = null;
        OnDismissListener dismissListener = null;
        private Context context;


        public Builder(Context context) {
            this.context = context;
        }


        public Builder dismissListener(OnDismissListener listener) {
            this.dismissListener = listener;
            return this;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message
         * @return
         */
        public Builder content(String message) {
            this.message = message;
            return this;
        }

        public View getContentView() {
            return contentView;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message
         * @return
         */
        public Builder content(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder title(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder titleGravity(int gravity) {
            this.titleGravity = gravity;
            return this;
        }

        public Builder buttonsGravity(int gravity) {
            this.bottomGravity = gravity;
            return this;
        }

        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         *
         * @param layoutRes
         * @return
         */
        public Builder customView(int layoutRes, boolean wrapInScrollView) {
            LayoutInflater li = LayoutInflater.from(context);
            this.contentView = li.inflate(layoutRes, null);
            this.wrapInScrollView = wrapInScrollView;
            return this;
        }


        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder positiveText(int positiveButtonText) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            return this;
        }


        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder positiveText(String positiveButtonText) {
            this.positiveButtonText = positiveButtonText;
            return this;
        }

        public Builder negativeText(@StringRes int negativeRes) {
            if (negativeRes == 0) return this;
            return negativeText(this.context.getText(negativeRes));
        }

        public Builder negativeText(@NonNull CharSequence message) {
            this.negativeButtonText = (String) message;
            return this;
        }


        public Builder onPositive(@NonNull SingleButtonCallback callback) {
            this.onPositiveCallback = callback;
            return this;
        }

        public Builder onNegative(@NonNull SingleButtonCallback callback) {
            this.onNegativeCallback = callback;
            return this;
        }

        /**
         * Create the custom dialog
         */
        public MaterialDialog build() {
            return materialDialog(this);
        }

        public MaterialDialog show() {
            MaterialDialog dialog = build();
            dialog.show();
            return dialog;
        }

    }

}