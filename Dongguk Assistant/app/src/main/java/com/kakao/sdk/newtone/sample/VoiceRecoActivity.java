package com.kakao.sdk.newtone.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.kakao.sdk.newtoneapi.SpeechRecognizerActivity;
import java.util.ArrayList;
import java.util.List;

public class VoiceRecoActivity extends SpeechRecognizerActivity {
    public static String EXTRA_KEY_RESULT_ARRAY = "result_array";
    public static String EXTRA_KEY_MARKED = "marked";
    public static String EXTRA_KEY_ERROR_CODE = "error_code";
    public static String EXTRA_KEY_ERROR_MESSAGE = "error_msg";

    protected void putStringFromId(RES_STRINGS key, int id) {
        putString(key, getResources().getString(id));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.com_kakao_sdk_asr_grow_height_from_top, android.R.anim.fade_in);
        boolean resourcePassed = isValidResourceMappings();
        Log.i("VoiceRecoActivity", "resource pass : " + resourcePassed);
        if (!resourcePassed) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_out, R.anim.com_kakao_sdk_asr_shrink_height_from_bottom);
    }

    @Override
    protected void onRecognitionSuccess(List<String> result, boolean marked) {
        Intent intent = new Intent().
                putStringArrayListExtra(EXTRA_KEY_RESULT_ARRAY, new ArrayList<String>(result)).
                putExtra(EXTRA_KEY_MARKED, marked);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onRecognitionFailed(int errorCode, String errorMsg) {
        Intent intent = new Intent().
                putExtra(EXTRA_KEY_ERROR_CODE, errorCode).
                putExtra(EXTRA_KEY_ERROR_MESSAGE, errorMsg);
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}