package com.example.guftagoo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, RootRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if(snapshot.exists())
                {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause()
    {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }

    private void InitializeFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    private void DisplayMessages(DataSnapshot snapshot)
    {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}