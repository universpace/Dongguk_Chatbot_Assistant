package com.kakao.sdk.newtone.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.impl.util.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class SpeechSampleActivity extends Activity implements View.OnClickListener, SpeechRecognizeListener {
   // ListView m_ListView;
    //CustomAdapter m_Adapter;
    private SpeechRecognizerClient client;
    private EditText mJsonText;
    private TextView mReceiveanswerText;

    private String uuid = UUID.randomUUID().toString();
    private LinearLayout chatLayout;
    private EditText queryEditText;


    private AIRequest aiRequest;
    private AIDataService aiDataService;
    private AIServiceContext customAIServiceContext;



    private static final String TAG = SpeechSampleActivity.class.getSimpleName();
    private static final int USER = 10001;
    private static final int BOT = 10002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechRecognizerManager.getInstance().initializeLibrary(this);
        findViewById(R.id.speechbutton).setOnClickListener(this);
        setButtonsStatus(true);
        final ScrollView scrollview = findViewById(R.id.chatScrollView);
        scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));

        chatLayout = findViewById(R.id.chatLayout);
        ImageView sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this::sendMessage);
        queryEditText = findViewById(R.id.queryEditText);
        queryEditText.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        sendMessage(sendBtn);
                        return true;
                    default:
                        break;
                }
            }
            return false;

        });
        /*
        m_Adapter = new CustomAdapter();
        m_ListView = (ListView) findViewById(R.id.listView1);
        m_ListView.setAdapter(m_Adapter);

        mJsonText = (EditText) findViewById(R.id.editText1);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String getTime = sdf.format(date);
        m_Adapter.add(getTime,2);

        m_Adapter.add("안녕하세요. 동국대 자동응답 챗봇입니다.",1);
        m_Adapter.add("음성인식 기능은 마이크 버튼을 터치하시면 사용 가능합니다.",1);
        m_Adapter.add("말을 안하시면 음성인식 기능이 자동 중단됩니다.",1);
        m_Adapter.add("챗봇을 사용해서 질문인식이 제대로 되지 않을 수 있으므로 정확한 단어로 질문해주세요.",1);
        m_Adapter.add("동국대에 궁금한 것을 물어보세요.",1);


*/
        initChatbot();
    }

    private void initChatbot(){
        final AIConfiguration config = new AIConfiguration("b5a055ab32ed4e3b8f4a1a5bb54701fb",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid);// helps to create new session whenever app restarts
        aiRequest = new AIRequest();
    }

    private void sendMessage(View view) {
        String msg = queryEditText.getText().toString();
        if (msg.trim().isEmpty()) {
            Toast.makeText(SpeechSampleActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
        } else {
            showTextView(msg, USER);
            queryEditText.setText("");
            // Android client
            aiRequest.setQuery(msg);
           RequestTask requestTask = new RequestTask(SpeechSampleActivity.this, aiDataService, customAIServiceContext);
            requestTask.execute(aiRequest);


        }
    }
    public void callback(AIResponse aiResponse) {
        if (aiResponse != null) {
            // process aiResponse here
            String botReply = aiResponse.getResult().getFulfillment().getSpeech();
            Log.d(TAG, "Bot Reply: " + botReply);
            showTextView(botReply, BOT);
        } else {
            Log.d(TAG, "Bot Reply: Null");
            showTextView("There was some communication issue. Please Try again!", BOT);
        }
    }


    private void sendObject(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("question", mJsonText.getText().toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
        receiveObject(jsonObject);
    }

    private void receiveObject(JSONObject data){
        try{
            mReceiveanswerText.setText(data.getString("answer"));
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    private void setButtonsStatus(boolean enabled) {
        findViewById(R.id.speechbutton).setEnabled(enabled);
        if(enabled==true) {
            findViewById(R.id.speechbutton).setVisibility(View.VISIBLE);
            findViewById(R.id.speechbutton1).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.speechbutton1).setVisibility(View.VISIBLE);
            findViewById(R.id.speechbutton).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;
        Log.i("SpeechSampleActivity", "serviceType : " + serviceType);
        if (id == R.id.speechbutton) {
            if(PermissionUtils.checkAudioRecordPermission(this)) {
                SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                        setServiceType(serviceType);
                client = builder.build();
                client.setSpeechRecognizeListener(this);
                client.startRecording(true);
                setButtonsStatus(false);
            }
        }
    }

    private void showTextView(String message, int type) {
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                break;
            case BOT:
                layout = getBotLayout();
                break;
            default:
                layout = getBotLayout();
                break;
        }
        layout.setFocusableInTouchMode(true);
        chatLayout.addView(layout); // move focus to text view to automatically make it scroll up if softfocus
        TextView tv = layout.findViewById(R.id.chatMsg);
        tv.setText(message);
        layout.requestFocus();
        queryEditText.requestFocus(); // change focus back to edit text to continue typing
    }

    FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(SpeechSampleActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.user_msg_layout, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(SpeechSampleActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.bot_msg_layout, null);
    }


    @Override
    public void onReady() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }
    @Override
    public void onBeginningOfSpeech() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }
    @Override
    public void onEndOfSpeech() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }
    @Override
    public void onError(int errorCode, String errorMsg) {
        //TODO implement interface DaumSpeechRecognizeListener method
        Log.e("SpeechSampleActivity", "onError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setButtonsStatus(true);
            }
        });
        client = null;
    }

    @Override
    public void onPartialResult(String text) {
        //TODO implement interface DaumSpeechRecognizeListener method
    }
    @Override
    public void onResults(Bundle results) {
        Log.i("SpeechSampleActivity", "onResults");
        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ((EditText) findViewById(R.id.queryEditText)).setText(texts.get(0));
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) return;
                setButtonsStatus(true);
            }
        });
        client = null;
    }

    @Override
    public void onAudioLevel(float v) {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    public void onFinished() {
        Log.i("SpeechSampleActivity", "onFinished");
    }
}