package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_ENABLED;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_RECORD_KEY;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jueyuantech.glasses.bean.SpeechSession;
import com.jueyuantech.glasses.db.DBHelper;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SpeechSessionListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;

    private DBHelper dbHelper;

    private List<SpeechSession> speechSessions = new ArrayList<>();
    private ListView mSessionLv;
    private ListAdapter mSessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_session_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        dbHelper = new DBHelper(this);

        speechSessions.clear();
        mSessionAdapter = new ListAdapter(this, speechSessions);
        mSessionLv = findViewById(R.id.lv_session);
        mSessionLv.setAdapter(mSessionAdapter);

        registerForContextMenu(mSessionLv);

        mSessionLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpeechSession speechSession = (SpeechSession) parent.getItemAtPosition(position);
                toMessageListAct(speechSession);
            }
        });

        speechSessions.addAll(dbHelper.getSessions());
        LogUtil.i("sessions size " + speechSessions.size());
        mSessionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            default:
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lv_session) {
            getMenuInflater().inflate(R.menu.menu_session_item, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        SpeechSession sessionToDelete = speechSessions.get(position);

        if (item.getItemId() == R.id.delete_session) {
            // 从数据库中删除
            dbHelper.deleteSession(sessionToDelete.getId());
            // 从列表中删除
            speechSessions.remove(position);
            // 通知适配器数据已更改
            mSessionAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private boolean isAudioRecordEnabled() {
        return AUDIO_RECORD_ENABLED == (int) MmkvUtil.decode(MMKV_AUDIO_RECORD_KEY, AUDIO_RECORD_DEFAULT);
    }

    private void toMessageListAct(SpeechSession speechSession) {
        Intent messageListIntent = new Intent(this, SpeechMessageListActivity.class);
        messageListIntent.putExtra("speech_session", speechSession);
        startActivity(messageListIntent);
    }

    class ListAdapter extends BaseAdapter {
        private Context context;
        private List<SpeechSession> items;

        public ListAdapter(Context context, List<SpeechSession> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
                holder = new ViewHolder();
                holder.titleTv = convertView.findViewById(R.id.tv_title);
                holder.audioFileNameTv = convertView.findViewById(R.id.tv_audio_file_name);
                holder.timeTv = convertView.findViewById(R.id.tv_time);
                holder.funcIv = convertView.findViewById(R.id.iv_func);
                holder.audioSourceIv = convertView.findViewById(R.id.iv_audio_source);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SpeechSession item = items.get(position);
            holder.titleTv.setText(item.getTitle());
            if (isAudioRecordEnabled()) {
                holder.audioFileNameTv.setText(item.getAudioFileName());
            }
            holder.timeTv.setText(Utils.getTimeStr(item.getCreateDate()));
            if ("phone".equals(item.getAudioSource())) {
                holder.audioSourceIv.setImageResource(R.drawable.baseline_phone_iphone_24);
            } else if ("sco".equals(item.getAudioSource())) {
                holder.audioSourceIv.setImageResource(R.drawable.baseline_glasses_alt_24);
            }

            if ("transcribe".equals(item.getFuncType())) {
                holder.funcIv.setImageResource(R.drawable.ic_transcribe);
            } else if ("translate".equals(item.getFuncType())) {
                holder.funcIv.setImageResource(R.drawable.ic_translate);
            }

            return convertView;
        }
    }

    class ViewHolder {
        TextView titleTv;
        TextView audioFileNameTv;
        TextView timeTv;
        ImageView funcIv;
        ImageView audioSourceIv;
    }
}