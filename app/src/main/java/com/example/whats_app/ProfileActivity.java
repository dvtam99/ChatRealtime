package com.example.whats_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, currentState, senderUserId;
    private CircleImageView userProfileImageView;
    private TextView userProfileName, userProfileStatus;
    private Button sendRequestMessage, declineRequest;

    private DatabaseReference userRef, chatRequestRef, contactRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        userProfileImageView = findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_profile_username);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendRequestMessage = findViewById(R.id.send_request_button);
        declineRequest = findViewById(R.id.decline_request_button);
        currentState = "new";
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && snapshot.hasChild("image")) {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImageView);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                } else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ManageChatRequest() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserId)) {
                            String requestType = snapshot.child(receiverUserId)
                                    .child("request_type").getValue().toString();
                            if (requestType.equals("sent")) {
                                currentState = "request_sent";
                                sendRequestMessage.setText("Cancel chat request");
                            } else if (requestType.equals("received")) {
                                currentState = "request_received";
                                sendRequestMessage.setText("Accept Chat");
                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);

                                declineRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else {
                            contactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(receiverUserId)){
                                                currentState= "friend";
                                                sendRequestMessage.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserId.equals(receiverUserId)) {
            sendRequestMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestMessage.setEnabled(false);
                    if (currentState.equals("new")) {
                        SenChatRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friend")) {
                        RemoveContact();
                    }
                }
            });
        } else {
            sendRequestMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {
        contactRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequestMessage.setEnabled(true);
                                                currentState = "new";
                                                sendRequestMessage.setText("Send message");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        contactRef.child(senderUserId)
                .child(receiverUserId)
                .child("Contacts")
                .setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .child("Contacts")
                                    .setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatRequestRef.child(senderUserId)
                                                        .child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                chatRequestRef.child(receiverUserId)
                                                                        .child(senderUserId)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                sendRequestMessage.setEnabled(true);
                                                                                currentState="friend";
                                                                                sendRequestMessage.setText("Remove this contact");
                                                                                declineRequest.setVisibility(View.INVISIBLE);
                                                                                declineRequest.setEnabled(false);
                                                                            }
                                                                        });
                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequestMessage.setEnabled(true);
                                                currentState = "new";
                                                sendRequestMessage.setText("Send message");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SenChatRequest() {
        chatRequestRef.child(senderUserId)
                .child(receiverUserId)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendRequestMessage.setEnabled(true);
                                                currentState = "request_sent";
                                                sendRequestMessage.setText("Cancel chat");


                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}