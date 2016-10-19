package me.pr3a.localweather;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyCustomFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // if token expired this will get refresh token.
        String newToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(newToken);
    }

    private void sendTokenToServer(String newToken) {
        // send new token to your server.
    }
}
