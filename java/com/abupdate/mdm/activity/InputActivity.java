package com.abupdate.mdm.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.abupdate.mdm.R;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.RegisterManager;
import com.abupdate.mdm.utils.LogUtils;

public class InputActivity extends BaseActivity {
    EditText device_code;
    Button btn_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.register_title);
        }

        device_code = findViewById(R.id.device_code);
        device_code.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), inputFilter});

        btn_action =  findViewById(R.id.register_action);
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                RegisterManager.getInstance().doRegister(Const.EMM_REGISTER_TYPE_INPUT + "", device_code.getText().toString(), getIntent().getStringExtra(Const.EMM_NAME),null);
            }
        });

        if (device_code.getText().length() == 0) {
            btn_action.setEnabled(false);
        } else {
            btn_action.setEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dsStart, int dsEnd) {
//            LogUtils.d("src:" + source + "; " + start + "-" + end + "; dest:" + dest + "; " + dsStart + "-" + dsEnd);
            if (end > 0 && (dest.length() + end >= 8)) {
                btn_action.setEnabled(true);
            }

            if (end == 0 && (dest.length() - (dsEnd - dsStart) < 8)) {
                btn_action.setEnabled(false);
            }
            return null;
        }
    };
}
