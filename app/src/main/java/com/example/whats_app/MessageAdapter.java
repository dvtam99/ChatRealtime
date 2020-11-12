package com.example.whats_app;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessasgeViewHolder> {



    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }


    public class MessasgeViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public MessasgeViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }


    @NonNull
    @Override
    public MessasgeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();
        return new MessasgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessasgeViewHolder messasgeViewHolder, int i) {
            String messageSenderID = mAuth.getCurrentUser().getUid();
            Messages messages = userMessagesList.get(i);

            String fromUserID = messages.getFrom();
            String fromMessageType = messages.getType();

            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("image"))
                    {
                        String receiverImage = dataSnapshot.child("image").getValue().toString();

                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                                .into(messasgeViewHolder.receiverProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if(fromMessageType.equals("text"))
            {
                messasgeViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
                messasgeViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

                if(fromUserID.equals(messageSenderID)){
                    messasgeViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                    messasgeViewHolder.senderMessageText.setTextColor(Color.BLACK);
                    messasgeViewHolder.senderMessageText.setText(messages.getMessage());
                }
                else
                {
                    messasgeViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

                    messasgeViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messasgeViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                    messasgeViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                    messasgeViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                    messasgeViewHolder.receiverMessageText.setText(messages.getMessage());
                }
            }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
