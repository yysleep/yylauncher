package com.yanhuahealth.healthlauncher.ui.note;

import android.os.Bundle;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.sys.SmsMgr;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.utils.PhoneStatus;

/**
 * 一键查话费
 */
public class QueryCostActivity extends YHBaseActivity{
    @Override
    protected String tag() {
        return QueryCostActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_cost);

        // 检测手机是否有sim卡
        if (!PhoneStatus.checkPhoneSimCard(QueryCostActivity.this)) {
            finish();
           return;
        }

        SmsMgr.getInstance().sendSmsForBalance(this);
        finish();
    }
}
