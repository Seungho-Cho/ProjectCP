package com.example.wearable.datalayerexample;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by imac_13 on 2015-05-05.
 */
public class TTSAdapter implements TextToSpeech.OnInitListener
{
    private TextToSpeech tts;
    private Activity acticity;
    public TTSAdapter(Activity act)
    {
        this.acticity = act;
        tts = new TextToSpeech(acticity,this);
    }


    @Override
    public void onInit(int status)
    {

        // TextToSpeech 엔진의 초기화가 완료되어 사용할 수 있도록 준비된 상태인 경우
        if (status == TextToSpeech.SUCCESS)
        {
            // 음성 합성하여 출력하기위한 언어를 Locale.US 로 설정한다.
            // 안드로이드 시스템의 환경 설정에서도 동일한 언어가 선택되어 있어야만
            // 해당 언어의 문장이 음성변환 될 수 있다.
            int result = tts.setLanguage(Locale.KOREA);


            // 해당 언어에 대한 데이터가 없거나 지원하지 않는 경우
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED)
            {

                // 해당 언어는 사용할 수 없음을 알린다.

                Toast.makeText(acticity, "Language is not available.", Toast.LENGTH_SHORT).show();
                // TTS 엔진이 해당 언어를 지원하며 데이터도 가지고 있는 경우
            } else
            {
                // TTS 엔진이 성공적으로 초기화된 경우
                // EditText 에 쓰여지는 문장을 음성 변환할 수 있도록 버튼을 활성화한다.
                //btn.setEnabled(true);
                tts.setLanguage(Locale.KOREA);
                tts.setSpeechRate(1.5f);
                tts.setPitch(1.5f);
            }

            // TextToSpeech 엔진 초기화에 실패하여 엔진이 TextToSpeech.ERROR 상태인 경우
        } else
        {
            // TextToSpeech 엔진이 초기화되지 못했음을 알린다
            Toast.makeText(acticity, "Could not initialize TextToSpeech.", Toast.LENGTH_SHORT).show();
        }
    }

    public void speak(String msg)
    {
        tts.speak(msg,TextToSpeech.QUEUE_ADD,null);
    }
    public void speak_f(String msg)
    {
        tts.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
    }


    public void destroy()
    {
        tts.stop();
        tts.shutdown();
    }

    public void speak_delay(final String msg, int msec)
    {
        Thread thred = new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                speak(msg);
            }
        };
        thred.start();

    }

}
